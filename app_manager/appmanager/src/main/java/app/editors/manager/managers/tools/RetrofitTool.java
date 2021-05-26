package app.editors.manager.managers.tools;


import android.content.Context;

import androidx.annotation.NonNull;

import com.google.gson.GsonBuilder;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import app.documents.core.settings.NetworkSettings;
import app.editors.manager.app.Api;
import app.editors.manager.managers.exceptions.ApiInitException;
import app.editors.manager.managers.exceptions.UrlSyntaxMistake;
import app.editors.manager.managers.retrofit.BaseInterceptor;
import lib.toolkit.base.managers.utils.StringUtils;
import lib.toolkit.base.managers.utils.TimeUtils;
import okhttp3.CipherSuite;
import okhttp3.ConnectionSpec;
import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import okhttp3.TlsVersion;
import retrofit2.Converter;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

@Deprecated
public class RetrofitTool {

    public static final String TAG = RetrofitTool.class.getSimpleName();

    private static final int READ_TIMEOUT = 60;
    private static final int WRITE_TIMEOUT = 60;
    private static final int CONNECT_TIMEOUT = 60;

    private Converter.Factory mConverterFactory;
    private OkHttpClient.Builder mOkHttpClient;
    private Retrofit.Builder mBuilder;
    private Retrofit mRetrofit;
    private Api mApi;
    private boolean mIsSslOn;
    private boolean mIsCiphers;
    private NetworkSettings mNetworkSettings;

    public RetrofitTool(final Context context) {
        mConverterFactory = getGsonConverterFactory();
        mOkHttpClient = getOkHttp();
        mBuilder = retrofitBuilder(mOkHttpClient);
        mIsSslOn = true;
        mIsCiphers = false;
    }

    /*
    * Main objects
    * */
    private Retrofit.Builder retrofitBuilder(OkHttpClient.Builder okHttpBuilder) {
        return new Retrofit.Builder()
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(mConverterFactory)
                .client(okHttpBuilder.build());
    }

    private Converter.Factory getGsonConverterFactory() {
        return GsonConverterFactory.create(new GsonBuilder()
                .setLenient()
                .setPrettyPrinting()
                .setDateFormat(TimeUtils.OUTPUT_PATTERN_DEFAULT)
                .serializeNulls()
                .create());
    }

    private OkHttpClient.Builder getOkHttp() {
        final OkHttpClient.Builder builder = new OkHttpClient.Builder();
        return builder.protocols(Arrays.asList(Protocol.HTTP_1_1))
                .readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
                .writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS)
                .connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)
                .addInterceptor(new BaseInterceptor(""));
    }

    private OkHttpClient.Builder getCustomOkHttp() {
        final OkHttpClient.Builder builder = getOkHttp();

        if (!mIsSslOn) {
            getUnsafeOkHttp(builder);
        }

        if (mIsCiphers) {
            getOkHttpSpecs(builder);
        }

        return builder;
    }

    private OkHttpClient.Builder getOkHttpSpecs(OkHttpClient.Builder okHttpClient) {
        return okHttpClient.connectionSpecs(getConnectionSpec());
    }

    private OkHttpClient.Builder getUnsafeOkHttp(OkHttpClient.Builder okHttpClient) {
        final X509TrustManager x509TrustManager = getUnsafeX509TrustManager();
        return okHttpClient
                .sslSocketFactory(getSocketFactory(x509TrustManager), x509TrustManager)
                .hostnameVerifier(getHostnameVerifier());
    }

    /*
    * Init api
    * */
    public RetrofitTool init(@NonNull final String url) throws UrlSyntaxMistake {
        try {
            mBuilder = retrofitBuilder(getCustomOkHttp());
            mRetrofit = mBuilder.baseUrl(url).build();
            mApi = mRetrofit.create(Api.class);
            return this;
        } catch (IllegalArgumentException e) {
            throw new UrlSyntaxMistake(e);
        }
    }

    public RetrofitTool initWithPreferences() throws UrlSyntaxMistake {
        return init(getPreferenceUrl());
    }

    /*
    * Get api
    * */
    public Api getApi(@NonNull final String url) {
        if (mApi == null) {
            try {
                init(url);
            } catch (UrlSyntaxMistake e) {
                throw new ApiInitException(e);
            }
        }

        return mApi;
    }

    public Api getApiWithPreferences() {
        return getApi(getPreferenceUrl());
    }

    public RetrofitTool setPreferenceTool(NetworkSettings preferenceTool) {
        mNetworkSettings = preferenceTool;
        return this;
    }

    @NonNull
    private String getPreferenceUrl() {
        if (mNetworkSettings != null) {
            mIsCiphers = mNetworkSettings.getCipher();
            mIsSslOn = mNetworkSettings.getSslState();
            return mNetworkSettings.getScheme() + StringUtils.getEncodedString(mNetworkSettings.getPortal());
        } else {
            throw new ApiInitException("Use method setPreferenceTool(PreferenceTool) or use " +
                    PreferenceTool.class.getSimpleName() + " - setPortal(portal_name)");
        }
    }

    public boolean isSslOn() {
        return mIsSslOn;
    }

    public RetrofitTool setSslOn(boolean sslOn) {
        mIsSslOn = sslOn;
        return this;
    }

    public boolean isCiphers() {
        return mIsCiphers;
    }

    public RetrofitTool setCiphers(boolean ciphers) {
        mIsCiphers = ciphers;
        return this;
    }

    /*
    * Lost encryption suites for lollipop
    * https://www.google.ru/search?q=android+7.0+retrofit2+Cipher+Suite+site:stackoverflow.com&newwindow=1&rlz=1C1CHZL_ruRU777RU777&sa=X&ved=0ahUKEwjG9e3j6OnaAhVp0aYKHU1qCosQrQIIRigEMAE&biw=1680&bih=829
    * https://github.com/square/okhttp/issues/3648
    * https://www.ssllabs.com/ssltest/analyze.html?d=nct.onlyoffice.com
    * */
    private List<ConnectionSpec> getConnectionSpec() {
        final ConnectionSpec spec = new ConnectionSpec.Builder(ConnectionSpec.MODERN_TLS)
                .tlsVersions(TlsVersion.TLS_1_2)
                .cipherSuites(
                        CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256,
                        CipherSuite.TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256,
                        CipherSuite.TLS_DHE_RSA_WITH_AES_128_GCM_SHA256 // <-- Worked with 7.0
                )
                .build();
        return Collections.singletonList(spec);
    }

    /*
    * Off certificate check
    * */
    private SSLSocketFactory getSocketFactory(final X509TrustManager trustManager) {
        try {
            // Create a trust manager that does not validate certificate chains
            final TrustManager[] trustAllCerts = new TrustManager[] { trustManager };
            // Install the all-trusting trust manager
            final SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
            // Create an ssl socket factory with our all-trusting manager
            return sslContext.getSocketFactory();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private X509TrustManager getUnsafeX509TrustManager() {
        return new X509TrustManager() {

            @Override
            public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) {
            }

            @Override
            public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) {
            }

            @Override
            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                return new java.security.cert.X509Certificate[]{};
            }
        };
    }

    /*
    * Off hostname verifier check, always true
    * */
    private HostnameVerifier getHostnameVerifier() {
        return (hostname, session) -> true;
    }
    
}
