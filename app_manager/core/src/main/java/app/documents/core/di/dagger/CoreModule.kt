package app.documents.core.di.dagger

import android.content.Context
import app.documents.core.di.dagger.storages.OneDriveScope
import app.documents.core.network.common.interceptors.BaseInterceptor
import app.documents.core.storage.account.CloudAccount
import app.documents.core.storage.preference.NetworkSettings
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import lib.toolkit.base.managers.http.NetworkClient
import lib.toolkit.base.managers.utils.AccountUtils
import lib.toolkit.base.managers.utils.TimeUtils
import okhttp3.OkHttpClient
import okhttp3.Protocol
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Qualifier
import javax.inject.Scope

@Scope
@Retention(AnnotationRetention.RUNTIME)
annotation class CoreScope

@Qualifier
annotation class Token

@Module(includes = [LoginModule::class, ManagerModule::class, ShareModule::class, WebDavModule::class])
object CoreModule {

    val json = Json {
        isLenient = true
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

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
                .create()
        )
    }

    @Provides
    @Token
    fun provideToken(context: Context, account: CloudAccount?): String = runBlocking {
        account?.let { cloudAccount ->
            return@runBlocking AccountUtils.getToken(context = context, cloudAccount.getAccountName())
                ?: throw RuntimeException("Token null")
        } ?: throw RuntimeException("Account null")
    }

}