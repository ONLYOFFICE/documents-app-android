package app.editors.manager.storages.dropbox.di.module

import android.content.Context
import app.documents.core.account.CloudAccount
import app.documents.core.settings.NetworkSettings
import app.editors.manager.storages.dropbox.dropbox.api.DropboxService
import app.editors.manager.storages.dropbox.dropbox.api.DropboxServiceProvider
import app.editors.manager.storages.dropbox.dropbox.api.IDropboxServiceProvider
import app.editors.manager.managers.retrofit.BaseInterceptor
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import lib.toolkit.base.managers.http.NetworkClient
import lib.toolkit.base.managers.utils.AccountUtils
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Protocol
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Scope


@Scope
@Retention(AnnotationRetention.RUNTIME)
annotation class DropboxScope

@Module
class DropboxModule() {

    @Provides
    @DropboxScope
    fun provideDropbox(dropboxService: DropboxService): IDropboxServiceProvider = DropboxServiceProvider(dropboxService)


    @Provides
    @DropboxScope
    fun provideDropboxService(okHttpClient: OkHttpClient, settings: NetworkSettings): DropboxService = Retrofit.Builder()
        .client(okHttpClient)
        .baseUrl(settings.getBaseUrl())
        .addConverterFactory(Json {
            isLenient = true
            ignoreUnknownKeys = true
        }.asConverterFactory(MediaType.get("application/json")))
        .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
        .build()
        .create(DropboxService::class.java)

    @Provides
    @DropboxScope
    fun provideOkHttpClient(@Named("token") token: String): OkHttpClient {
        val builder = NetworkClient.getOkHttpBuilder(true, false)
        builder.protocols(listOf(Protocol.HTTP_1_1))
            .readTimeout(NetworkClient.ClientSettings.READ_TIMEOUT, TimeUnit.SECONDS)
            .writeTimeout(NetworkClient.ClientSettings.WRITE_TIMEOUT, TimeUnit.SECONDS)
            .connectTimeout(NetworkClient.ClientSettings.CONNECT_TIMEOUT, TimeUnit.SECONDS)
            .addInterceptor(BaseInterceptor(token))
        return builder.build()
    }

    @Provides
    @DropboxScope
    @Named("token")
    fun provideToken(context: Context, account: CloudAccount?): String = runBlocking {
        account?.let { cloudAccount ->
            return@runBlocking AccountUtils.getToken(context = context, cloudAccount.getAccountName())
                ?: throw RuntimeException("Token null")
        } ?: throw RuntimeException("Account null")
    }
}