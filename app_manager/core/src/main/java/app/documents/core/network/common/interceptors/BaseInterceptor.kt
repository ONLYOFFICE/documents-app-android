package app.documents.core.network.common.interceptors

import android.content.Context
import app.documents.core.network.common.contracts.ApiContract
import app.documents.core.network.common.exceptions.NoConnectivityException
import lib.toolkit.base.managers.utils.NetworkUtils.isOnline
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

class BaseInterceptor(private val token: String?, private val context: Context) : Interceptor {

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
            chain.request()
                .newBuilder().apply {
                    if (chain.request().headers()[ApiContract.HEADER_AUTHORIZATION] == null)
                        addHeader(ApiContract.HEADER_AUTHORIZATION, token.orEmpty())
                }
                .build()
        )
    }

    @Throws(NoConnectivityException::class)
    private fun checkConnection() {
        if (!isOnline(context)) {
            throw NoConnectivityException()
        }
    }
}