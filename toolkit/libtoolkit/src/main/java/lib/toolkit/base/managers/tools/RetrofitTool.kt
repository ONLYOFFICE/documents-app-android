package lib.toolkit.base.managers.tools

import android.content.Context
import com.google.gson.GsonBuilder
import okhttp3.*
import retrofit2.Converter
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.util.concurrent.TimeUnit
import javax.net.ssl.*

class RetrofitTool<T>(private val context: Context) {

    companion object {
        const val OUTPUT_PATTERN_DEFAULT = "yyyy-MM-dd'T'HH:mm:ss.SSSSSSSZ"
        const val READ_TIMEOUT = 60
        const val WRITE_TIMEOUT = 60
        const val CONNECT_TIMEOUT = 60
    }

    private val mConverterFactory: Converter.Factory
    private var mOkHttpClient: OkHttpClient.Builder
    private var mBuilder: Retrofit.Builder

    private lateinit var mUrl: String
    private lateinit var mRetrofit: Retrofit

    private var mApi: T? = null
    private var mIsSslOn: Boolean = true
    private var mIsCiphers: Boolean = false

    init {
        mConverterFactory = mGsonConverterFactory
        mOkHttpClient = mOkHttp
        mBuilder = retrofitBuilder(mOkHttpClient!!)
        mIsSslOn = true
        mIsCiphers = false
    }

    private val mGsonConverterFactory: Converter.Factory
        get() = GsonConverterFactory.create(GsonBuilder()
                .setLenient()
                .setPrettyPrinting()
                .setDateFormat(OUTPUT_PATTERN_DEFAULT)
                .serializeNulls()
                .create())

    private val mOkHttp: OkHttpClient.Builder
        get() = OkHttpClient.Builder()
                .readTimeout(READ_TIMEOUT.toLong(), TimeUnit.SECONDS)
                .writeTimeout(WRITE_TIMEOUT.toLong(), TimeUnit.SECONDS)
                .connectTimeout(CONNECT_TIMEOUT.toLong(), TimeUnit.SECONDS)
                .addInterceptor(BaseInterceptor())

    private val mUnsafeOkHttp: OkHttpClient.Builder
        get() {
            val x509TrustManager = mUnsafeX509TrustManager
            return mOkHttp
                    .sslSocketFactory(getSocketFactory(x509TrustManager), x509TrustManager)
                    .hostnameVerifier(mHostnameVerifier)
        }


    /*
    * Lost encryption suites for lollipop
    * https://www.google.ru/search?q=android+7.0+retrofit2+Cipher+Suite+site:stackoverflow.com&newwindow=1&rlz=1C1CHZL_ruRU777RU777&sa=X&ved=0ahUKEwjG9e3j6OnaAhVp0aYKHU1qCosQrQIIRigEMAE&biw=1680&bih=829
    * https://github.com/square/okhttp/issues/3648
    * https://www.ssllabs.com/ssltest/analyze.html?d=nct.onlyoffice.com
    * */
    private val mConnectionSpec: List<ConnectionSpec>
        get() {
            val spec = ConnectionSpec.Builder(ConnectionSpec.MODERN_TLS)
                    .tlsVersions(TlsVersion.TLS_1_2)
                    .cipherSuites(
                            CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256,
                            CipherSuite.TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256,
                            CipherSuite.TLS_DHE_RSA_WITH_AES_128_GCM_SHA256
                    )
                    .build()
            return listOf(spec)
        }

    private val mUnsafeX509TrustManager: X509TrustManager
        get() = object : X509TrustManager {

            override fun checkClientTrusted(chain: Array<java.security.cert.X509Certificate>, authType: String) {}

            override fun checkServerTrusted(chain: Array<java.security.cert.X509Certificate>, authType: String) {}

            override fun getAcceptedIssuers(): Array<java.security.cert.X509Certificate> = arrayOf()

        }

    /*
    * Off hostname verifier check, always true
    * */
    private val mHostnameVerifier = HostnameVerifier { hostname, session ->
        HttpsURLConnection.getDefaultHostnameVerifier().run {
            true
        }
    }

    /*
    * Off certificate check
    * */
    private fun getSocketFactory(trustManager: X509TrustManager): SSLSocketFactory {
        try {
            // Create a trust manager that does not validate certificate chains
            val trustAllCerts = arrayOf<TrustManager>(trustManager)
            // Install the all-trusting trust manager
            val sslContext = SSLContext.getInstance("TLS")
            sslContext.init(null, trustAllCerts, java.security.SecureRandom())
            // Create an ssl socket factory with our all-trusting manager
            return sslContext.socketFactory
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }


    /*
    * Main objects
    * */
    private fun retrofitBuilder(okHttpBuilder: OkHttpClient.Builder): Retrofit.Builder {
        return Retrofit.Builder()
                .addConverterFactory(mConverterFactory)
                .client(okHttpBuilder.build())
    }

    private fun getOkHttpSpecs(okHttpClient: OkHttpClient.Builder): OkHttpClient.Builder {
        return okHttpClient.connectionSpecs(mConnectionSpec)
    }

    /*
    * User interface
    * */
    fun init(clazz: Class<T>, url: String): RetrofitTool<T> {
        if (!mIsSslOn) {
            mOkHttpClient = mUnsafeOkHttp
            mBuilder = retrofitBuilder(mOkHttpClient)
        } else if (mIsCiphers) {
            mOkHttpClient = getOkHttpSpecs(mOkHttp)
            mBuilder = retrofitBuilder(mOkHttpClient)
        } else {
            mOkHttpClient = mOkHttp
            mBuilder = retrofitBuilder(mOkHttpClient)
        }

        mUrl = url
        mRetrofit = mBuilder.baseUrl(mUrl).build()
        mApi = mRetrofit.create<T>(clazz)
        return this
    }

    fun getApi(): T {
       return mApi?.let { it } ?: throw RuntimeException("The \"init\" function must be called before using this function...")
    }

    fun isSslOn(): Boolean = mIsSslOn

    fun setSslOn(sslOn: Boolean): RetrofitTool<T> {
        mIsSslOn = sslOn
        return this
    }

    fun isCiphers(): Boolean = mIsCiphers

    fun setCiphers(ciphers: Boolean): RetrofitTool<T> {
        mIsCiphers = ciphers
        return this
    }

}


class BaseInterceptor : Interceptor {

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val builder = chain.request().newBuilder()
        return chain.proceed(builder.build())
    }

}