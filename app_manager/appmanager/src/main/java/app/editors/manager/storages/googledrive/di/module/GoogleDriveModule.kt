package app.editors.manager.storages.googledrive.di.module

import android.content.Context
import app.documents.core.storage.account.CloudAccount
import app.editors.manager.storages.googledrive.googledrive.api.GoogleDriveService
import app.editors.manager.storages.googledrive.googledrive.api.GoogleDriveServiceProvider
import app.editors.manager.storages.googledrive.googledrive.api.IGoogleDriveServiceProvider
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
annotation class GoogleDriveScope

@Module
class GoogleDriveModule {

    @Provides
    @GoogleDriveScope
    fun provideGoogleDrive(googleDriveService: GoogleDriveService): IGoogleDriveServiceProvider = GoogleDriveServiceProvider(googleDriveService)


    @Provides
    @GoogleDriveScope
    fun provideGoogleDriveService(okHttpClient: OkHttpClient): GoogleDriveService = Retrofit.Builder()
        .client(okHttpClient)
        .baseUrl("https://www.googleapis.com/")
        .addConverterFactory(Json {
            isLenient = true
            ignoreUnknownKeys = true
        }.asConverterFactory(MediaType.get("application/json")))
        .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
        .build()
        .create(GoogleDriveService::class.java)

    @Provides
    @GoogleDriveScope
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
    @GoogleDriveScope
    @Named("token")
    fun provideToken(context: Context, account: CloudAccount?): String = runBlocking {
        account?.let { cloudAccount ->
            return@runBlocking AccountUtils.getToken(context = context, cloudAccount.getAccountName())
                ?: throw RuntimeException("Token null")
        } ?: throw RuntimeException("Account null")
    }

}