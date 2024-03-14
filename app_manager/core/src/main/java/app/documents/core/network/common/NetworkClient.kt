package app.documents.core.network.common

import android.annotation.SuppressLint
import app.documents.core.model.cloud.CloudAccount
import app.documents.core.network.common.interceptors.WebDavInterceptor
import okhttp3.CipherSuite
import okhttp3.ConnectionSpec
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.TlsVersion
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

    private val trustAllCerts: Array<TrustManager> = arrayOf(
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

    fun getOkHttpBuilder(cloudAccount: CloudAccount, vararg interceptors: Interceptor): OkHttpClient.Builder {
        return getOkHttpBuilder(
            cloudAccount.portal.settings.isSslState,
            cloudAccount.portal.settings.isSslCiphers
        ).readTimeout(NetworkClient.ClientSettings.READ_TIMEOUT, TimeUnit.SECONDS)
            .writeTimeout(NetworkClient.ClientSettings.WRITE_TIMEOUT, TimeUnit.SECONDS)
            .connectTimeout(NetworkClient.ClientSettings.CONNECT_TIMEOUT, TimeUnit.SECONDS)
            .apply { interceptors.forEach(::addInterceptor) }
    }

    private fun getOkHttpSpecs(okHttpClient: OkHttpClient.Builder): OkHttpClient.Builder? {
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