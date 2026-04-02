package com.qcp.aioverlay.data.ai

import com.qcp.aioverlay.BuildConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GeminiClient @Inject constructor() {

    private val apiKey = BuildConfig.GEMINI_API_KEY
    private val baseUrl = "https://generativelanguage.googleapis.com/v1beta/models"
    private val model = "gemini-2.0-flash"

    fun streamResponse(prompt: String): Flow<String> = flow {
        val url = URL("$baseUrl/$model:streamGenerateContent?key=$apiKey&alt=sse")
        val connection = url.openConnection() as HttpURLConnection

        connection.apply {
            requestMethod = "POST"
            setRequestProperty("Content-Type", "application/json")
            doOutput = true
            connectTimeout = 15_000
            readTimeout = 30_000
        }

        val body = JSONObject().apply {
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
            })
        }.toString()

        connection.outputStream.use { it.write(body.toByteArray()) }

        val responseCode = connection.responseCode
        if (responseCode != HttpURLConnection.HTTP_OK) {
            val error = connection.errorStream?.bufferedReader()?.readText() ?: "Unknown error"
            throw GeminiException("HTTP $responseCode: $error")
        }

        // Parse SSE stream
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
                    if (text.isNotEmpty()) emit(text)
                } catch (e: Exception) {
                    // skip malformed chunk
                }
            }
        }

        connection.disconnect()
    }

    suspend fun generateResponse(prompt: String): String {
        val builder = StringBuilder()
        streamResponse(prompt).collect { builder.append(it) }
        return builder.toString().takeIf { it.isNotBlank() }
            ?: throw GeminiException("Empty response")
    }
}

class GeminiException(message: String) : Exception(message)