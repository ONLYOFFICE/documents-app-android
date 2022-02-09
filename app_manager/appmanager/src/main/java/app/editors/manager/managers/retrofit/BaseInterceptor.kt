package app.editors.manager.managers.retrofit

import android.os.Build
import app.documents.core.network.ApiContract
import app.editors.manager.BuildConfig
import app.editors.manager.app.App
import app.editors.manager.managers.exceptions.NoConnectivityException
import lib.toolkit.base.managers.utils.NetworkUtils.isOnline
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

class BaseInterceptor(val token: String?) : Interceptor {

    companion object {
        private const val KEY_AUTH = "Bearer "
    }

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        checkConnection()
        val token = if (chain.request().url().host().contains(ApiContract.PERSONAL_HOST)) {
            token
        } else {
            KEY_AUTH + token
        }
        return chain.proceed(
            chain.request().newBuilder().addHeader(
                ApiContract.HEADER_AUTHORIZATION,
                token ?: ""
            )
                .addHeader(
                    ApiContract.HEADER_AGENT,
                    "Android ONLYOFFICE Documents (id = ${BuildConfig.APPLICATION_ID}, SDK = ${Build.VERSION.SDK_INT}, build = ${BuildConfig.VERSION_CODE}, appName = ${BuildConfig.VERSION_NAME}"
                )
                .build()
        )
    }

    @Throws(NoConnectivityException::class)
    private fun checkConnection() {
        if (!isOnline(App.getApp())) {
            throw NoConnectivityException()
        }
    }
}