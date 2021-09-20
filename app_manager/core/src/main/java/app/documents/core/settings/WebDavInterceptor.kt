package app.documents.core.settings

import app.documents.core.webdav.WebDavApi
import okhttp3.Credentials
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import java.io.IOException

class WebDavInterceptor(val login: String?, val password: String?) : Interceptor {

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        if (login == null && password == null) {
            return chain.proceed(chain.request())
        } else {
            val auth = chain.request().header(WebDavApi.HEADER_AUTHORIZATION)
            val request: Request = if (auth == null) {
                chain.request().newBuilder()
                    .addHeader(
                        WebDavApi.HEADER_AUTHORIZATION,
                        Credentials.basic(login ?: "", password ?: "")
                    )
                    .build()
            } else {
                chain.request()
            }

            return chain.proceed(request)
        }
    }

}