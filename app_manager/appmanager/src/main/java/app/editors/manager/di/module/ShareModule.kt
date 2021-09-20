package app.editors.manager.di.module

import app.documents.core.settings.NetworkSettings
import app.documents.core.share.ShareService
import app.editors.manager.managers.retrofit.BaseInterceptor
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import dagger.Module
import dagger.Provides
import kotlinx.serialization.json.Json
import lib.toolkit.base.managers.http.NetworkClient
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Protocol
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Scope

@Scope
@Retention(AnnotationRetention.RUNTIME)
annotation class ShareScope

@Module
class ShareModule(val token: String) {

    @Provides
    @ShareScope
    fun provideShareService(okHttpClient: OkHttpClient, settings: NetworkSettings): ShareService = Retrofit.Builder()
        .client(okHttpClient)
        .baseUrl(settings.getBaseUrl())
        .addConverterFactory(Json {
            isLenient = true
            ignoreUnknownKeys = true
        }.asConverterFactory(MediaType.get("application/json")))
        .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
        .build()
        .create(ShareService::class.java)

    @Provides
    @ShareScope
    fun provideOkHttpClient(settings: NetworkSettings): OkHttpClient {
        val builder = NetworkClient.getOkHttpBuilder(settings.getSslState(), settings.getCipher())
        builder.protocols(listOf(Protocol.HTTP_1_1))
            .readTimeout(NetworkClient.ClientSettings.READ_TIMEOUT, TimeUnit.SECONDS)
            .writeTimeout(NetworkClient.ClientSettings.READ_TIMEOUT, TimeUnit.SECONDS)
            .connectTimeout(NetworkClient.ClientSettings.READ_TIMEOUT, TimeUnit.SECONDS)
            .addInterceptor(BaseInterceptor(token))
        return builder.build()
    }

}