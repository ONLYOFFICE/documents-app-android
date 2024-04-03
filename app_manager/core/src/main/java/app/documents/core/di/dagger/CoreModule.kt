package app.documents.core.di.dagger

import android.content.Context
import app.documents.core.account.AccountManager
import app.documents.core.database.di.DatabaseModule
import app.documents.core.model.cloud.CloudAccount
import app.documents.core.model.exception.CloudAccountNotFoundException
import app.documents.core.network.common.NetworkClient
import app.documents.core.network.common.interceptors.BaseInterceptor
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
    fun provideOkHttpClient(
        context: Context,
        cloudAccount: CloudAccount?,
        accountManager: AccountManager
    ): OkHttpClient {
        if (cloudAccount == null) throw CloudAccountNotFoundException
        val token = accountManager.getToken(cloudAccount.accountName)
        return NetworkClient.getOkHttpBuilder(cloudAccount.portal.settings, BaseInterceptor(token, context)).build()
    }

    @Provides
    @LoginOkHttpClient
    fun provideLoginOkHttpClient(context: Context, cloudAccount: CloudAccount?): OkHttpClient {
        return NetworkClient.getOkHttpBuilder(cloudAccount?.portal?.settings, LoginInterceptor(context)).build()
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