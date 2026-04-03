package com.qcp.aioverlay.data.ai

import org.json.JSONArray
import org.json.JSONObject

private const val RETRY_INFO_TYPE = "type.googleapis.com/google.rpc.RetryInfo"
private const val QUOTA_FAILURE_TYPE = "type.googleapis.com/google.rpc.QuotaFailure"

enum class GeminiErrorKind {
    Authentication,
    ModelUnavailable,
    QuotaExceeded,
    Network,
    Unknown
}

class GeminiException(
    val kind: GeminiErrorKind,
    message: String,
    val statusCode: Int? = null,
    val apiStatus: String? = null,
    val retryAfterSeconds: Int? = null,
    val isRetryable: Boolean = false,
    val shouldRefreshModel: Boolean = false,
) : Exception(message)

internal fun parseGeminiError(
    statusCode: Int,
    rawBody: String,
    model: String,
): GeminiException {
    val error = runCatching { JSONObject(rawBody).getJSONObject("error") }.getOrNull()
    val apiStatus = error?.optString("status").takeUnless { it.isNullOrBlank() }
    val apiMessage = error?.optString("message").takeUnless { it.isNullOrBlank() }
        ?: rawBody.ifBlank { "Unknown Gemini API error." }
    val details = error?.optJSONArray("details")
    val retryAfterSeconds = details?.extractRetryDelaySeconds()
    val quotaMetrics = details?.extractQuotaMetrics().orEmpty()
    val quotaLimitIsZero = Regex("""limit:\s*0""").containsMatchIn(apiMessage)
    val userMessage = buildUserMessage(
        statusCode = statusCode,
        apiStatus = apiStatus,
        apiMessage = apiMessage,
        model = model,
        retryAfterSeconds = retryAfterSeconds,
        quotaLimitIsZero = quotaLimitIsZero
    )

    val kind = when {
        statusCode == 403 || apiStatus == "PERMISSION_DENIED" -> GeminiErrorKind.Authentication
        statusCode == 404 || apiStatus == "NOT_FOUND" -> GeminiErrorKind.ModelUnavailable
        statusCode == 429 || apiStatus == "RESOURCE_EXHAUSTED" -> GeminiErrorKind.QuotaExceeded
        else -> GeminiErrorKind.Unknown
    }
    val shouldRefreshModel = kind == GeminiErrorKind.ModelUnavailable &&
        apiMessage.contains("not supported for generateContent", ignoreCase = true)
    val isRetryable = kind == GeminiErrorKind.QuotaExceeded &&
        !quotaLimitIsZero &&
        quotaMetrics.none { it.contains("PerDay", ignoreCase = true) } &&
        (retryAfterSeconds ?: Int.MAX_VALUE) <= 5

    return GeminiException(
        kind = kind,
        message = userMessage,
        statusCode = statusCode,
        apiStatus = apiStatus,
        retryAfterSeconds = retryAfterSeconds,
        isRetryable = isRetryable,
        shouldRefreshModel = shouldRefreshModel,
    )
}

private fun buildUserMessage(
    statusCode: Int,
    apiStatus: String?,
    apiMessage: String,
    model: String,
    retryAfterSeconds: Int?,
    quotaLimitIsZero: Boolean,
): String {
    return when {
        statusCode == 403 || apiStatus == "PERMISSION_DENIED" -> {
            "Gemini authentication failed. Verify GEMINI_API_KEY and Google AI Studio project access."
        }
        statusCode == 404 || apiStatus == "NOT_FOUND" -> {
            "Gemini model '$model' is unavailable for ${GeminiApiConfig.apiVersion}. The app will try another supported text model."
        }
        statusCode == 429 || apiStatus == "RESOURCE_EXHAUSTED" -> {
            val retryPart = retryAfterSeconds?.let { " Retry after about ${it}s." }.orEmpty()
            val quotaPart = if (quotaLimitIsZero) {
                " Gemini quota is exhausted or not enabled for this project."
            } else {
                " Gemini quota is temporarily exhausted."
            }
            "$quotaPart Check billing, plan, and API project quota.$retryPart"
        }
        else -> "Gemini request failed (HTTP $statusCode): $apiMessage"
    }
}

private fun JSONArray.extractRetryDelaySeconds(): Int? {
    for (index in 0 until length()) {
        val item = optJSONObject(index) ?: continue
        if (item.optString("@type") != RETRY_INFO_TYPE) continue
        return parseRetryDelaySeconds(item.optString("retryDelay"))
    }
    return null
}

private fun JSONArray.extractQuotaMetrics(): List<String> {
    val metrics = mutableListOf<String>()
    for (index in 0 until length()) {
        val item = optJSONObject(index) ?: continue
        if (item.optString("@type") != QUOTA_FAILURE_TYPE) continue
        val violations = item.optJSONArray("violations") ?: continue
        for (violationIndex in 0 until violations.length()) {
            val violation = violations.optJSONObject(violationIndex) ?: continue
            metrics += violation.optString("quotaId")
            metrics += violation.optString("quotaMetric")
        }
    }
    return metrics.filter { it.isNotBlank() }
}

private fun parseRetryDelaySeconds(retryDelay: String?): Int? {
    if (retryDelay.isNullOrBlank()) return null
    val match = Regex("""(\d+)s""").find(retryDelay) ?: return null
    return match.groupValues[1].toIntOrNull()
}
