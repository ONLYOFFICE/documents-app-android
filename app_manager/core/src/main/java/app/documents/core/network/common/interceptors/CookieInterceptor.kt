package app.documents.core.network.common.interceptors

import okhttp3.Interceptor
import okhttp3.Response

class CookieInterceptor : Interceptor {

    companion object {

        private const val PREFIX_SHARE_LINK = "sharelink"
        private const val KEY_HEADER_SET_COOKIE = "set-cookie"
    }

    private val cookie: MutableMap<String, String> = mutableMapOf()

    override fun intercept(chain: Interceptor.Chain): Response {
        val newBuilder = chain.request().newBuilder().apply {
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
}