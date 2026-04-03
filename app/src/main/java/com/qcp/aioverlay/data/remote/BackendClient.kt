package com.qcp.aioverlay.data.remote

import com.qcp.aioverlay.BuildConfig
import com.qcp.aioverlay.data.model.BackendAiRequest
import com.qcp.aioverlay.data.model.BackendAiResponse
import com.qcp.aioverlay.data.model.BackendErrorResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.serialization.json.Json
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import javax.net.ssl.HttpsURLConnection

sealed interface BackendResult {
    data class Success(val response: BackendAiResponse) : BackendResult
    data class Error(val code: String, val message: String) : BackendResult
}

@Singleton
class BackendClient @Inject constructor() {
    private val baseUrl = BuildConfig.BACKEND_BASE_URL
    private val apiKey = BuildConfig.BACKEND_API_KEY

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    suspend fun processAi(request: BackendAiRequest): BackendResult =
        withContext(Dispatchers.IO) {
            val url = URL("$baseUrl/api/v1/ai/process")
            val connection = url.openConnection() as HttpURLConnection

            try {
                connection.apply {
                    requestMethod = "POST"
                    setRequestProperty("Content-Type", "application/json")
                    setRequestProperty("X-API-Key", apiKey)
                    doOutput = true
                    connectTimeout = 15_000
                    readTimeout = 30_000
                }

                val body = json.encodeToString(request)
                OutputStreamWriter(connection.getOutputStream()).use { writer ->
                    writer.write(body)
                    writer.flush()
                }

                val respondCode = connection.responseCode

                if(respondCode == HttpsURLConnection.HTTP_OK) {
                    val respondBody = connection.getInputStream().bufferedReader().readText()
                    val response = json.decodeFromString<BackendAiResponse>(respondBody)
                    BackendResult.Success(response)
                } else {
                    val errorBody = connection.errorStream?.bufferedReader()?.readText()
                        ?: """{"code":"UNKNOWN","message":"HTTP $respondCode"}"""
                    val error = runCatching {
                        json.decodeFromString<BackendErrorResponse>(errorBody)
                    }.getOrElse {
                        BackendErrorResponse(code = "UNKNOWN",message = "HTTP $respondCode")
                    }
                    BackendResult.Error(error.code, error.message)
                }
            } catch (e: Exception) {
                BackendResult.Error("NETWORK_ERROR", e.message ?: "Network failure")
            } finally {
                connection.disconnect()
            }
        }
}
