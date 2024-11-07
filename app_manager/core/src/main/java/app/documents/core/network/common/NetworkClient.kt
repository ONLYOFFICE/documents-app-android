package app.documents.core.network.common

import android.annotation.SuppressLint
import app.documents.core.model.cloud.PortalSettings
import app.documents.core.network.common.interceptors.RequestInterceptor
import okhttp3.CipherSuite
import okhttp3.ConnectionSpec
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.TlsVersion
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.security.SecureRandom
import java.util.concurrent.TimeUnit
import javax.net.ssl.HostnameVerifier
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager


object NetworkClient {

    object ClientSettings {
        const val READ_TIMEOUT = 60L
        const val WRITE_TIMEOUT = 60L
        const val CONNECT_TIMEOUT = 60L
    }

    inline fun <reified V>getRetrofit(url: String, token: String): V {
        return Retrofit.Builder()
            .baseUrl(url)
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .client(getOkHttpBuilder(PortalSettings(), RequestInterceptor(token)).build())
            .build()
            .create(V::class.java)
    }

    private val trustAllCerts: Array<TrustManager> = arrayOf(
        @SuppressLint("CustomX509TrustManager")
        object : X509TrustManager {

            @SuppressLint("TrustAllX509TrustManager")
            override fun checkClientTrusted(chain: Array<out java.security.cert.X509Certificate>?, authType: String?) {
            }

            @SuppressLint("TrustAllX509TrustManager")
            override fun checkServerTrusted(chain: Array<out java.security.cert.X509Certificate>?, authType: String?) {
            }

            override fun getAcceptedIssuers(): Array<java.security.cert.X509Certificate> = arrayOf()

        }
    )

    @JvmStatic
    fun getOkHttpBuilder(isSslOn: Boolean = false, isCiphers: Boolean = false): OkHttpClient.Builder {
        val builder = OkHttpClient.Builder()

        if (isCiphers) {
            getOkHttpSpecs(builder)
        }

        if (!isSslOn) {
            val sslContext = SSLContext.getInstance("TLS").apply {
                init(null, trustAllCerts, SecureRandom())
            }
            val sslSocketFactory = sslContext.socketFactory
            builder.sslSocketFactory(sslSocketFactory, trustAllCerts[0] as X509TrustManager)
                .hostnameVerifier(HostnameVerifier { _, _ -> return@HostnameVerifier true })
        }
        return builder
    }

    fun getOkHttpBuilder(portalSettings: PortalSettings?, vararg interceptors: Interceptor): OkHttpClient.Builder {
        val builder = portalSettings?.let {
            getOkHttpBuilder(
                portalSettings.isSslState,
                portalSettings.isSslCiphers
            )
        }

        return (builder ?: OkHttpClient.Builder())
            .readTimeout(ClientSettings.READ_TIMEOUT, TimeUnit.SECONDS)
            .writeTimeout(ClientSettings.WRITE_TIMEOUT, TimeUnit.SECONDS)
            .connectTimeout(ClientSettings.CONNECT_TIMEOUT, TimeUnit.SECONDS)
            .apply { interceptors.forEach(::addInterceptor) }
    }

    private fun getOkHttpSpecs(okHttpClient: OkHttpClient.Builder): OkHttpClient.Builder {
        return okHttpClient.connectionSpecs(getConnectionSpec())
    }

    private fun getConnectionSpec(): List<ConnectionSpec?> {
        val spec = ConnectionSpec.Builder(ConnectionSpec.MODERN_TLS)
            .tlsVersions(TlsVersion.TLS_1_2)
            .cipherSuites(
                CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256,
                CipherSuite.TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256,
                CipherSuite.TLS_DHE_RSA_WITH_AES_128_GCM_SHA256 // <-- Worked with 7.0
            )
            .build()
        return listOf(spec)
    }

}