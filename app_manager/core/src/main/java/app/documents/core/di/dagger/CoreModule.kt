package app.documents.core.di.dagger

import android.content.Context
import app.documents.core.database.di.DatabaseModule
import app.documents.core.network.common.NetworkClient
import app.documents.core.network.common.interceptors.BaseInterceptor
import app.documents.core.network.di.NetworkModule
import app.documents.core.network.manager.models.explorer.PathPart
import app.documents.core.network.manager.models.explorer.PathPartTypeAdapter
import app.documents.core.storage.preference.NetworkSettings
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import kotlinx.serialization.json.Json
import lib.toolkit.base.managers.utils.TimeUtils
import okhttp3.OkHttpClient
import okhttp3.Protocol
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Qualifier

@Qualifier
annotation class Token

@Module(
    includes = [
        LoginModule::class, ManagerModule::class,
        ShareModule::class, WebDavModule::class,
        AccountModule::class, NetworkModule::class,
        DatabaseModule::class
    ]
)
object CoreModule {

    val json = Json {
        isLenient = true
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    @Provides
    @Named("baseUrl")
    fun provideBaseUrl(networkSettings: NetworkSettings): String {
        return networkSettings.getBaseUrl()
    }

    @Provides
    fun provideJson(): Json = json

    @Provides
    fun provideOkHttpClient(@Token token: String, settings: NetworkSettings, context: Context): OkHttpClient {
        val builder = NetworkClient.getOkHttpBuilder(settings.getSslState(), settings.getCipher())
        builder.protocols(listOf(Protocol.HTTP_1_1))
            .readTimeout(NetworkClient.ClientSettings.READ_TIMEOUT, TimeUnit.SECONDS)
            .writeTimeout(NetworkClient.ClientSettings.WRITE_TIMEOUT, TimeUnit.SECONDS)
            .connectTimeout(NetworkClient.ClientSettings.CONNECT_TIMEOUT, TimeUnit.SECONDS)
            .addInterceptor(BaseInterceptor(token, context))
        return builder.build()
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