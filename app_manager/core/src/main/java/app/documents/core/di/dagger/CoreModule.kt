package app.documents.core.di.dagger

import android.content.Context
import app.documents.core.account.AccountManager
import app.documents.core.database.di.DatabaseModule
import app.documents.core.model.cloud.CloudAccount
import app.documents.core.model.exception.CloudAccountNotFoundException
import app.documents.core.network.common.NetworkClient
import app.documents.core.network.common.interceptors.BaseInterceptor
import app.documents.core.network.manager.models.explorer.PathPart
import app.documents.core.network.manager.models.explorer.PathPartTypeAdapter
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import kotlinx.serialization.json.Json
import lib.toolkit.base.managers.utils.TimeUtils
import okhttp3.OkHttpClient
import okhttp3.Protocol
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Qualifier

@Qualifier
annotation class Token

@Module(
    includes = [
        LoginModule::class, ManagerModule::class,
        ShareModule::class, WebDavModule::class,
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
    fun provideOkHttpClient(
        context: Context,
        cloudAccount: CloudAccount?,
        accountManager: AccountManager
    ): OkHttpClient {
        if (cloudAccount == null) throw CloudAccountNotFoundException
        val token = accountManager.getToken(cloudAccount.accountName)
        val builder = NetworkClient.getOkHttpBuilder(
            cloudAccount.portal.settings.isSslState,
            cloudAccount.portal.settings.isSslCiphers,
        )

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