package app.documents.core.network.common

import okhttp3.Request
import okhttp3.Response
import okio.Buffer
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class NetworkRequest(
    val method: String,
    val url: String,
    val headers: Map<String, String>,
    val requestBody: String?,
    val responseCode: Int,
    val responseMessage: String,
    val responseBody: String?,
    val timestamp: Long,
    val duration: Long
) {
    fun getFormattedTime(): String {
        return SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault())
            .format(Date(timestamp))
    }
}

object RequestsCollector {
    private const val MAX_REQUESTS = 50
    private val requests = mutableListOf<NetworkRequest>()
    private val lock = Any()

    private var isDeveloperMode = false

    private val tokenHeaders = listOf(
        "Authorization",
        "Request-Token",
        "Bearer"
    )

    private fun maskSensitiveHeaders(headers: Map<String, String>): Map<String, String> {
        return headers.mapValues { (key, value) ->
            if (tokenHeaders.any { key.contains(it, ignoreCase = true) } && value.isNotEmpty()) {
                if (value.length > 10) {
                    val prefix = value.take(4)
                    val suffix = value.takeLast(4)
                    val maskedPart = "*".repeat(value.length - 8)
                    "$prefix$maskedPart$suffix"
                } else {
                    "****${if (value.length > 4) value.takeLast(2) else ""}"
                }
            } else {
                value
            }
        }
    }

    fun setDeveloperMode(enabled: Boolean) {
        isDeveloperMode = enabled
    }


    /**
     * Logs a request with OkHttp objects
     * @param request OkHttp request object
     * @param response OkHttp response object
     * @param responseBodyString response body as a string
     * @param startTime request start time in milliseconds
     * @return response body as a string (for reuse)
     */
    fun logRequest(
        request: Request,
        response: Response,
        responseBodyString: String?,
        startTime: Long
    ): String? {
        if (!isDeveloperMode) {
            return responseBodyString
        }

        val duration = System.currentTimeMillis() - startTime

        val requestHeaders = request.headers.toMultimap().mapValues { it.value.firstOrNull() ?: "" }

        val requestBody = request.body?.let {
            val buffer = Buffer()
            it.writeTo(buffer)
            buffer.readUtf8()
        }

        logRequest(
            method = request.method,
            url = request.url.toString(),
            headers = requestHeaders,
            requestBody = requestBody,
            responseCode = response.code,
            responseMessage = response.message,
            responseBody = responseBodyString,
            timestamp = startTime,
            duration = duration
        )

        return responseBodyString
    }

    private fun logRequest(
        method: String,
        url: String,
        headers: Map<String, String>,
        requestBody: String?,
        responseCode: Int,
        responseMessage: String,
        responseBody: String?,
        timestamp: Long,
        duration: Long
    ) {
        if (!isDeveloperMode) {
            return
        }

        synchronized(lock) {
            if (requests.size >= MAX_REQUESTS) {
                requests.removeAt(0)
            }
            requests.add(
                NetworkRequest(
                    method = method,
                    url = url,
                    headers = maskSensitiveHeaders(headers),
                    requestBody = requestBody,
                    responseCode = responseCode,
                    responseMessage = responseMessage,
                    responseBody = responseBody,
                    timestamp = timestamp,
                    duration = duration
                )
            )
        }
    }

    fun getRequests(): List<NetworkRequest> {
        return synchronized(lock) {
            requests.toList()
        }
    }

    fun clearRequests() {
        synchronized(lock) {
            requests.clear()
        }
    }
}
