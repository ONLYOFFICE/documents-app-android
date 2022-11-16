package app.documents.core.di.dagger

import android.content.Context
import app.documents.core.storage.account.AccountDao
import app.documents.core.storage.preference.NetworkSettings
import app.documents.core.network.common.interceptors.WebDavInterceptor
import app.documents.core.network.webdav.ConverterFactory
import app.documents.core.network.webdav.WebDavApi
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.runBlocking
import lib.toolkit.base.managers.http.NetworkClient
import lib.toolkit.base.managers.utils.AccountUtils
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Scope

@Scope
@Retention(AnnotationRetention.RUNTIME)
annotation class WebDavScope

@Module
class WebDavApiModule {

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
    @WebDavScope
    @Named("password")
    fun providePassword(context: Context, accountDao: AccountDao): String? = runBlocking {
        accountDao.getAccountOnline()?.let { cloudAccount ->
            return@runBlocking AccountUtils.getPassword(context, cloudAccount.getAccountName())
        } ?: return@runBlocking null
    }

    @Provides
    @WebDavScope
    @Named("login")
    fun provideLogin(accountDao: AccountDao): String? = runBlocking {
        accountDao.getAccountOnline()?.let { cloudAccount ->
            return@runBlocking cloudAccount.login
        } ?: return@runBlocking null
    }

}