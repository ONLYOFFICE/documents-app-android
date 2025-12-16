package app.documents.core.network.common.interceptors

import app.documents.core.network.webdav.WebDavService
import okhttp3.Credentials
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import java.io.IOException

class WebDavInterceptor(
    private val login: String?,
    private val password: String?,
    private val accessTokenProvider: () -> String?
) : Interceptor {

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val accessToken = accessTokenProvider.invoke()
        if (accessToken == null && login == null && password == null) {
            return chain.proceed(chain.request())
        } else {
            val auth = chain.request().header(WebDavService.HEADER_AUTHORIZATION)
            val header = if (accessToken.isNullOrEmpty()) {
                Credentials.basic(login ?: "", password ?: "")
            } else {
                "Bearer $accessToken"
            }
            val request: Request = if (auth == null) {
                chain.request().newBuilder()
                    .addHeader(
                        WebDavService.HEADER_AUTHORIZATION,
                        header
                    )
                    .build()
            } else {
                chain.request()
            }

            return chain.proceed(request)
        }
    }

}