package app.documents.core.di.dagger

import android.content.Context
import app.documents.core.database.di.DatabaseModule
import app.documents.core.model.cloud.CloudAccount
import app.documents.core.model.exception.CloudAccountNotFoundException
import app.documents.core.network.common.NetworkClient
import app.documents.core.network.common.interceptors.BaseInterceptor
import app.documents.core.network.common.interceptors.CookieInterceptor
import app.documents.core.network.login.LoginInterceptor
import app.documents.core.network.login.LoginOkHttpClient
import app.documents.core.network.manager.models.explorer.PathPart
import app.documents.core.network.manager.models.explorer.PathPartTypeAdapter
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import kotlinx.serialization.json.Json
import lib.toolkit.base.managers.utils.TimeUtils
import okhttp3.OkHttpClient
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Qualifier
import javax.inject.Singleton

@Qualifier
annotation class Token

@Module(
    includes = [
        ManagerModule::class, ShareModule::class,
        AccountModule::class, DatabaseModule::class
    ]
)
object CoreModule {

    val json = Json {
        isLenient = true
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    @Provides
    fun provideJson(): Json = json

    @Provides
    @Singleton
    fun provideCookieInterceptor(): CookieInterceptor {
        return CookieInterceptor()
    }

    @Provides
    fun provideBaseInterceptor(
        @Token token: String,
        context: Context,
    ): BaseInterceptor {
        return BaseInterceptor(token, context)
    }

    @Provides
    fun provideOkHttpClient(
        cloudAccount: CloudAccount?,
        baseInterceptor: BaseInterceptor,
        cookieInterceptor: CookieInterceptor
    ): OkHttpClient {
        return NetworkClient
            .getOkHttpBuilder(
                cloudAccount?.portal?.settings ?: throw CloudAccountNotFoundException,
                baseInterceptor,
                cookieInterceptor
            )
            .build()
    }

    @Provides
    @LoginOkHttpClient
    fun provideLoginOkHttpClient(context: Context, cloudAccount: CloudAccount?): OkHttpClient {
        return NetworkClient.getOkHttpBuilder(
            cloudAccount?.portal?.settings,
            LoginInterceptor(context)
        ).build()
    }

    @Provides
    fun provideConverterFactory(): GsonConverterFactory {
        return GsonConverterFactory.create(
            GsonBuilder()
                .setLenient()
                .setPrettyPrinting()
                .setDateFormat(TimeUtils.OUTPUT_PATTERN_DEFAULT)
                .serializeNulls()
                .registerTypeAdapter(PathPart::class.java, PathPartTypeAdapter())
                .create()
        )
    }
}