package com.qcp.aioverlay.data.ai

import com.qcp.aioverlay.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import java.net.URL
import java.nio.charset.StandardCharsets
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GeminiClient @Inject constructor() {

    private val apiKey = BuildConfig.GEMINI_API_KEY
    private val modelMutex = Mutex()
    @Volatile
    private var resolvedModel: String? = null

    fun streamResponse(prompt: String): Flow<String> = flow {
        require(apiKey.isNotBlank()) {
            "Missing Gemini API key. Configure GEMINI_API_KEY in local.properties, gradle.properties, or the GEMINI_API_KEY environment variable."
        }

        val initialModel = resolveModel()
        try {
            executeStreamingRequest(prompt = prompt, model = initialModel, emitChunk = ::emit)
        } catch (error: GeminiException) {
            if (error.shouldRefreshModel) {
                val fallbackModel = refreshModelResolution(failedModel = initialModel)
                if (fallbackModel != null && fallbackModel != initialModel) {
                    executeStreamingRequest(prompt = prompt, model = fallbackModel, emitChunk = ::emit)
                    return@flow
                }
            }
            throw error
        }
    }.flowOn(Dispatchers.IO)

    suspend fun generateResponse(prompt: String): String {
        val builder = StringBuilder()
        streamResponse(prompt).collect { builder.append(it) }
        return builder.toString().takeIf { it.isNotBlank() }
            ?: throw GeminiException(
                kind = GeminiErrorKind.Unknown,
                message = "Gemini returned an empty response."
            )
    }

    private suspend fun executeStreamingRequest(
        prompt: String,
        model: String,
        emitChunk: suspend (String) -> Unit,
    ) {
        val body = buildBody(prompt, model)
        var attempt = 0

        while (true) {
            attempt++
            val connection = openStreamConnection(model)
            try {
                connection.outputStream.use {
                    it.write(body.toByteArray(StandardCharsets.UTF_8))
                }

                val responseCode = connection.responseCode
                if (responseCode != HttpURLConnection.HTTP_OK) {
                    val errorBody = connection.errorStream?.bufferedReader()?.readText().orEmpty()
                    val exception = parseGeminiError(
                        statusCode = responseCode,
                        rawBody = errorBody,
                        model = model
                    )
                    if (exception.isRetryable && attempt < 2) {
                        delay(((exception.retryAfterSeconds ?: 1).coerceAtLeast(1)) * 1000L)
                        continue
                    }
                    throw exception
                }

                parseSse(connection, emitChunk)
                return
            } catch (timeout: SocketTimeoutException) {
                throw GeminiException(
                    kind = GeminiErrorKind.Network,
                    message = "Gemini request timed out. Check network connectivity and try again.",
                )
            } finally {
                connection.disconnect()
            }
        }
    }

    private fun openStreamConnection(model: String): HttpURLConnection {
        val connection = URL(GeminiApiConfig.streamEndpoint(model)).openConnection() as HttpURLConnection
        return connection.apply {
            requestMethod = "POST"
            setRequestProperty("Content-Type", "application/json; charset=utf-8")
            setRequestProperty("Accept", "text/event-stream")
            setRequestProperty("x-goog-api-key", apiKey)
            doOutput = true
            connectTimeout = 15_000
            readTimeout = 30_000
        }
    }

    private fun buildBody(prompt: String, model: String): String = JSONObject().apply {
        put("contents", JSONArray().apply {
            put(JSONObject().apply {
                put("parts", JSONArray().apply {
                    put(JSONObject().apply {
                        put("text", prompt)
                    })
                })
            })
        })
        put("generationConfig", JSONObject().apply {
            put("temperature", 0.7)
            put("maxOutputTokens", 1024)
            if (model.startsWith("gemini-2.5")) {
                put("thinkingConfig", JSONObject().apply {
                    put("thinkingBudget", 0)
                })
            }
        })
    }.toString()

    private suspend fun resolveModel(): String {
        resolvedModel?.let { return it }
        return modelMutex.withLock {
            resolvedModel ?: selectBestModel().also { resolvedModel = it }
        }
    }

    private suspend fun refreshModelResolution(failedModel: String): String? {
        return modelMutex.withLock {
            val freshModel = selectBestModel(excludedModels = setOf(failedModel))
            resolvedModel = freshModel
            freshModel
        }
    }

    private fun selectBestModel(excludedModels: Set<String> = emptySet()): String {
        val availableModels = runCatching { fetchAvailableModels() }.getOrDefault(emptyList())
        if (availableModels.isEmpty()) {
            return GeminiApiConfig.preferredModels.firstOrNull { it !in excludedModels }
                ?: throw GeminiException(
                    kind = GeminiErrorKind.ModelUnavailable,
                    message = "No Gemini text model is configured. Set GEMINI_MODEL or verify the Gemini API project.",
                )
        }

        val preferred = GeminiApiConfig.preferredModels.firstOrNull { candidate ->
            candidate !in excludedModels && availableModels.any { it.supportsTextGeneration(candidate) }
        }
        if (preferred != null) return preferred

        return availableModels.firstOrNull { model ->
            model.name !in excludedModels && model.isTextGenerationModel()
        }?.name ?: throw GeminiException(
            kind = GeminiErrorKind.ModelUnavailable,
            message = "No Gemini text model supporting generateContent is available for ${GeminiApiConfig.apiVersion}.",
        )
    }

    private fun fetchAvailableModels(): List<GeminiModelInfo> {
        val connection = URL(GeminiApiConfig.listModelsEndpoint()).openConnection() as HttpURLConnection
        return connection.run {
            try {
                requestMethod = "GET"
                setRequestProperty("Accept", "application/json")
                setRequestProperty("x-goog-api-key", apiKey)
                connectTimeout = 15_000
                readTimeout = 30_000

                val body = when (responseCode) {
                    HttpURLConnection.HTTP_OK -> inputStream.bufferedReader().readText()
                    else -> {
                        val errorBody = errorStream?.bufferedReader()?.readText().orEmpty()
                        throw parseGeminiError(responseCode, errorBody, GeminiApiConfig.preferredModels.first())
                    }
                }

                val models = JSONObject(body).optJSONArray("models") ?: JSONArray()
                buildList {
                    for (index in 0 until models.length()) {
                        val modelJson = models.optJSONObject(index) ?: continue
                        val name = modelJson.optString("name")
                            .removePrefix("models/")
                            .trim()
                        if (name.isBlank()) continue
                        val supportedMethods = modelJson.optJSONArray("supportedGenerationMethods")
                            ?.toStringList()
                            .orEmpty()
                        add(
                            GeminiModelInfo(
                                name = name,
                                supportedMethods = supportedMethods
                            )
                        )
                    }
                }
            } finally {
                disconnect()
            }
        }
    }

    private suspend fun parseSse(
        connection: HttpURLConnection,
        emitChunk: suspend (String) -> Unit,
    ) {
        BufferedReader(InputStreamReader(connection.inputStream)).use { reader ->
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                val data = line?.removePrefix("data: ")?.trim() ?: continue
                if (data.isEmpty() || data == "[DONE]") continue

                try {
                    val json = JSONObject(data)
                    val text = json
                        .getJSONArray("candidates")
                        .getJSONObject(0)
                        .getJSONObject("content")
                        .getJSONArray("parts")
                        .getJSONObject(0)
                        .optString("text", "")
                    if (text.isNotEmpty()) {
                        emitChunk(text)
                    }
                } catch (_: Exception) {
                    // Skip malformed SSE chunks instead of failing the whole stream.
                }
            }
        }
    }
}

private data class GeminiModelInfo(
    val name: String,
    val supportedMethods: List<String>,
) {
    fun supportsTextGeneration(candidate: String): Boolean {
        return name == candidate && isTextGenerationModel()
    }

    fun isTextGenerationModel(): Boolean {
        return supportedMethods.isEmpty() || supportedMethods.any {
            it.equals("generateContent", ignoreCase = true) ||
                it.equals("streamGenerateContent", ignoreCase = true)
        }
    }
}

private fun JSONArray.toStringList(): List<String> = buildList {
    for (index in 0 until length()) {
        val value = optString(index)
        if (value.isNotBlank()) add(value)
    }
}
