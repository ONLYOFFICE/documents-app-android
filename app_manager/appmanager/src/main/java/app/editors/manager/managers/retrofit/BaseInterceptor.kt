package app.editors.manager.managers.retrofit

import app.documents.core.network.ApiContract
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
        return chain.proceed(
            chain.request().newBuilder().addHeader(
                ApiContract.HEADER_AUTHORIZATION,
                KEY_AUTH + token
            ).build()
        )
    }

    @Throws(NoConnectivityException::class)
    private fun checkConnection() {
        if (!isOnline(App.getApp())) {
            throw NoConnectivityException()
        }
    }
}