package app.editors.manager.di.module

import app.documents.core.settings.NetworkSettings
import app.editors.manager.app.Api
import app.editors.manager.managers.retrofit.BaseInterceptor
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import lib.toolkit.base.managers.http.NetworkClient
import lib.toolkit.base.managers.utils.TimeUtils
import okhttp3.OkHttpClient
import okhttp3.Protocol
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Scope


@Scope
@Retention(AnnotationRetention.RUNTIME)
annotation class ApiScope

@Module
class ApiModule(val token: String) {

    companion object {
        private const val READ_TIMEOUT = 60L
        private const val WRITE_TIMEOUT = 60L
        private const val CONNECT_TIMEOUT = 60L
    }

    @Provides
    @ApiScope
    fun provideApi(factory: GsonConverterFactory, client: OkHttpClient, settings: NetworkSettings): Api  {
        return Retrofit.Builder()
            .baseUrl(settings.getBaseUrl())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .addConverterFactory(factory)
            .client(client)
            .build().create(Api::class.java)
    }

    @Provides
    @ApiScope
    fun provideConverterFactory(): GsonConverterFactory {
        return GsonConverterFactory.create(
            GsonBuilder()
                .setLenient()
                .setPrettyPrinting()
                .setDateFormat(TimeUtils.OUTPUT_PATTERN_DEFAULT)
                .serializeNulls()
                .create()
        )
    }

    @Provides
    @ApiScope
    fun provideOkHttpClient(settings: NetworkSettings): OkHttpClient {
        val builder = NetworkClient.getOkHttpBuilder(settings.getSslState(), settings.getCipher())
        builder.protocols(listOf(Protocol.HTTP_1_1))
            .readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
            .writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS)
            .connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)
            .addInterceptor(BaseInterceptor(token))
        return builder.build()
    }

}