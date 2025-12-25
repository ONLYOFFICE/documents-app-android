package app.documents.core.network.login

import android.content.Context
import android.os.Build
import app.documents.core.network.HEADER_AGENT
import app.documents.core.network.common.RequestsCollector
import okhttp3.Interceptor
import okhttp3.Response
import okhttp3.ResponseBody

class LoginInterceptor(val context: Context) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val info = context.packageManager.getPackageInfo(context.packageName, 0)
        val request = chain.request()

        val newRequest = request.newBuilder()
            .addHeader(HEADER_AGENT, "Android ONLYOFFICE Documents (" +
                    "id = com.onlyoffice.documents, " +
                    "SDK = ${Build.VERSION.SDK_INT}, " +
                    "build = ${info.versionCode}, " +
                    "appName = ${info.versionName}")
            .build()

        val startTime = System.currentTimeMillis()
        val response = chain.proceed(newRequest)

        val responseBody = response.body?.string()
        val contentType = response.body?.contentType()

        val loggedResponseBody = RequestsCollector.logRequest(
            request = newRequest,
            response = response,
            responseBodyString = responseBody,
            startTime = startTime
        )

        return response.newBuilder()
            .body(ResponseBody.create(contentType, loggedResponseBody ?: ""))
            .build()
    }
}