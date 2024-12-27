package app.documents.core.network.common.interceptors

import android.content.Context
import app.documents.core.network.common.contracts.ApiContract
import app.documents.core.network.common.exceptions.NoConnectivityException
import lib.toolkit.base.managers.utils.NetworkUtils.isOnline
import okhttp3.Interceptor
import okhttp3.Response
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
    }

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        checkConnection()
        val token = if (chain.request().url().host().contains(ApiContract.PERSONAL_HOST)) {
            token
        } else {
            if (type == HeaderType.AUTHORIZATION) {
                KEY_AUTH + token.orEmpty()
            } else {
                token
            }
        }
        val newBuilder = chain.request().newBuilder().apply {
            if (chain.request().headers()[ApiContract.HEADER_AUTHORIZATION] == null) {
                addHeader(type.header, token.orEmpty())
            }
        }
        val response = chain.proceed(newBuilder.build())
        return response
    }

    @Throws(NoConnectivityException::class)
    private fun checkConnection() {
        if (!isOnline(context)) {
            throw NoConnectivityException()
        }
    }
}