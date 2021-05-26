package app.documents.core.di.module

import app.documents.core.settings.NetworkSettings
import app.documents.core.settings.WebDavInterceptor
import app.documents.core.webdav.ConverterFactory
import app.documents.core.webdav.WebDavApi
import dagger.Module
import dagger.Provides
import lib.toolkit.base.managers.http.NetworkClient
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Scope

@Scope
@Retention(AnnotationRetention.RUNTIME)
annotation class WebDavScope

@Module
class WebDavApiModule(val login: String? = null, val password: String? = null) {

    @Provides
    @WebDavScope
    fun provideWebDAvApi(client: OkHttpClient, settings: NetworkSettings): WebDavApi {
        return Retrofit.Builder()
            .baseUrl(settings.getBaseUrl())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .addConverterFactory(ConverterFactory())
            .client(client)
            .build()
            .create(WebDavApi::class.java)
    }

    @Provides
    @WebDavScope
    fun provideClient(settings: NetworkSettings) : OkHttpClient {
        val builder = NetworkClient.getOkHttpBuilder(settings.getSslState(), settings.getCipher())
        return builder
            .readTimeout(NetworkClient.ClientSettings.READ_TIMEOUT, TimeUnit.SECONDS)
            .writeTimeout(NetworkClient.ClientSettings.WRITE_TIMEOUT, TimeUnit.SECONDS)
            .connectTimeout(NetworkClient.ClientSettings.CONNECT_TIMEOUT, TimeUnit.SECONDS)
            .addInterceptor(WebDavInterceptor(login, password))
            .build()
    }

}