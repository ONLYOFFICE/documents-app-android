package app.documents.core.network.common.interceptors

import android.content.Context
import app.documents.core.network.common.RequestsCollector
import app.documents.core.network.common.contracts.ApiContract
import app.documents.core.network.common.exceptions.NoConnectivityException
import lib.toolkit.base.managers.utils.NetworkUtils.isOnline
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody
import java.io.IOException

enum class HeaderType(val header: String) {
    AUTHORIZATION("Authorization"),
    REQUEST_TOKEN("Request-Token")
}

class BaseInterceptor(
    private val token: String?,
    private val context: Context,
    private val type: HeaderType = HeaderType.AUTHORIZATION
) : Interceptor {

    companion object {
        private const val KEY_AUTH = "Bearer "
        private const val PEEK_BODY_LIMIT = 1 * 512 * 512L
    }

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        try {
            checkConnection()

            val token = if (chain.request().url.host.contains(ApiContract.PERSONAL_HOST)) {
                token
            } else {
                if (type == HeaderType.AUTHORIZATION) {
                    KEY_AUTH + token.orEmpty()
                } else {
                    token
                }
            }

            val newBuilder = chain.request().newBuilder().apply {
                if (chain.request().headers[ApiContract.HEADER_AUTHORIZATION] == null) {
                    addHeader(type.header, token.orEmpty())
                }
            }

            val request = newBuilder.build()

            val startTime = System.currentTimeMillis()
            val response = chain.proceed(request)

            val responseBody = response.peekBody(PEEK_BODY_LIMIT).string()

            RequestsCollector.logRequest(
                request = request,
                response = response,
                responseBodyString = responseBody,
                startTime = startTime
            )

            return response


        } catch (_: NoConnectivityException) {
            val responseBody = ResponseBody.create(
                "application/json".toMediaType(),
                "{\"error\":\"No internet connection\"}"
            )

            return Response.Builder()
                .request(chain.request())
                .protocol(Protocol.HTTP_1_1)
                .code(599)
                .message("No Internet Connection")
                .body(responseBody)
                .build()
        }
    }

    @Throws(NoConnectivityException::class)
    private fun checkConnection() {
        if (!isOnline(context)) {
            throw NoConnectivityException()
        }
    }
}