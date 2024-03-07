package app.documents.core.di.dagger

import android.content.Context
import app.documents.core.model.cloud.CloudAccount
import app.documents.core.network.common.NetworkClient
import app.documents.core.network.common.interceptors.WebDavInterceptor
import app.documents.core.network.webdav.ConverterFactory
import app.documents.core.network.webdav.WebDavService
import app.documents.core.storage.preference.NetworkSettings
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.runBlocking
import lib.toolkit.base.managers.utils.AccountUtils
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Named

@Module
class WebDavModule {

    @Provides
    fun provideWebDavService(@Named("webdav") client: OkHttpClient, settings: NetworkSettings): WebDavService {
        return Retrofit.Builder()
            .baseUrl(settings.getBaseUrl())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .addConverterFactory(ConverterFactory())
            .client(client)
            .build()
            .create(WebDavService::class.java)
    }

    @Provides
    @Named("webdav")
    fun provideClient(
        @Named("login") login: String?,
        @Named("password") password: String?,
        settings: NetworkSettings
    ): OkHttpClient {
        val builder = NetworkClient.getOkHttpBuilder(settings.getSslState(), settings.getCipher())
        return builder
            .readTimeout(NetworkClient.ClientSettings.READ_TIMEOUT, TimeUnit.SECONDS)
            .writeTimeout(NetworkClient.ClientSettings.WRITE_TIMEOUT, TimeUnit.SECONDS)
            .connectTimeout(NetworkClient.ClientSettings.CONNECT_TIMEOUT, TimeUnit.SECONDS)
            .addInterceptor(WebDavInterceptor(login, password))
            .build()
    }

    @Provides
    @Named("password")
    fun providePassword(context: Context, cloudAccount: CloudAccount?): String? = runBlocking {
        return@runBlocking AccountUtils.getPassword(
            context = context,
            name = cloudAccount?.accountName ?: return@runBlocking null
        )
    }

    @Provides
    @Named("login")
    fun provideLogin(cloudAccount: CloudAccount?): String? = runBlocking {
        return@runBlocking cloudAccount?.login
    }

}