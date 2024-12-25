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

        private const val PREFIX_SHARE_LINK = "sharelink"
        private const val KEY_HEADER_SET_COOKIE = "set-cookie"
        private const val KEY_AUTH = "Bearer "
    }

    private val cookie: MutableMap<String, String> = mutableMapOf()

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
            cookie.forEach { (key, value) ->
                if (key.contains(PREFIX_SHARE_LINK)) {
                    addHeader(key, value)
                }
            }
        }
        val response = chain.proceed(newBuilder.build())
        saveCookie(response)
        return response
    }

    private fun saveCookie(response: Response) {
        val responseCookie = response.networkResponse()
            ?.headers()
            ?.get(KEY_HEADER_SET_COOKIE)
            .orEmpty()
            .split(";")
            .mapNotNull {
                val pairs = it.split ("=")
                val key = pairs.getOrNull(0) ?: return@mapNotNull null
                val value = pairs.getOrNull(1)  ?: return@mapNotNull null
                key to value
            }

        cookie.putAll(responseCookie)
    }

    @Throws(NoConnectivityException::class)
    private fun checkConnection() {
        if (!isOnline(context)) {
            throw NoConnectivityException()
        }
    }
}