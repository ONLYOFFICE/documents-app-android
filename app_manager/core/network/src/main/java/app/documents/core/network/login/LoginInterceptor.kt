package app.documents.core.network.login

import android.content.Context
import android.os.Build
import app.documents.core.network.common.contracts.ApiContract
import okhttp3.Interceptor
import okhttp3.Response

class LoginInterceptor(val context: Context) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val info = context.packageManager.getPackageInfo(context.packageName, 0)
        return chain.proceed(
            chain.request().newBuilder()
                .addHeader(ApiContract.HEADER_AGENT, "Android ONLYOFFICE Documents (id = com.onlyoffice.documents, SDK = ${Build.VERSION.SDK_INT}, build = ${info.versionCode}, appName = ${info.versionName}")
                .build()
        )
    }
}