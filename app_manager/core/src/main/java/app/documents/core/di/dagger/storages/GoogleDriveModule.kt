package app.documents.core.di.dagger.storages

import android.content.Context
import app.documents.core.di.dagger.CoreModule.json
import app.documents.core.di.dagger.Token
import app.documents.core.network.common.contracts.ApiContract
import app.documents.core.network.common.interceptors.BaseInterceptor
import app.documents.core.network.common.utils.GoogleDriveUtils
import app.documents.core.network.storages.googledrive.api.GoogleDriveProvider
import app.documents.core.network.storages.googledrive.api.GoogleDriveService
import app.documents.core.storage.account.CloudAccount
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.ExperimentalSerializationApi
import app.documents.core.network.common.NetworkClient
import lib.toolkit.base.managers.utils.AccountUtils
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Protocol
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Scope

@Scope
@Retention(AnnotationRetention.RUNTIME)
annotation class GoogleDriveScope

@Module
class GoogleDriveModule {

    @Provides
    @GoogleDriveScope
    fun provideGoogleDrive(googleDriveService: GoogleDriveService): GoogleDriveProvider =
        GoogleDriveProvider(googleDriveService)


    @Provides
    @GoogleDriveScope
    @OptIn(ExperimentalSerializationApi::class)
    fun provideGoogleDriveService(okHttpClient: OkHttpClient): GoogleDriveService = Retrofit.Builder()
        .client(okHttpClient)
        .baseUrl(GoogleDriveUtils.GOOGLE_DRIVE_BASE_URL)
        .addConverterFactory(json.asConverterFactory(MediaType.get(ApiContract.VALUE_CONTENT_TYPE)))
        .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
        .build()
        .create(GoogleDriveService::class.java)

    @Provides
    @GoogleDriveScope
    fun provideOkHttpClient(@Token token: String, context: Context): OkHttpClient {
        val builder = NetworkClient.getOkHttpBuilder(isSslOn = true, isCiphers = false)
        builder.protocols(listOf(Protocol.HTTP_1_1))
            .readTimeout(NetworkClient.ClientSettings.READ_TIMEOUT, TimeUnit.SECONDS)
            .writeTimeout(NetworkClient.ClientSettings.WRITE_TIMEOUT, TimeUnit.SECONDS)
            .connectTimeout(NetworkClient.ClientSettings.CONNECT_TIMEOUT, TimeUnit.SECONDS)
            .addInterceptor(BaseInterceptor(token, context))
        return builder.build()
    }

    @Provides
    @GoogleDriveScope
    @Token
    fun provideToken(context: Context, account: CloudAccount?): String = runBlocking {
        account?.let { cloudAccount ->
            return@runBlocking AccountUtils.getToken(context = context, cloudAccount.getAccountName())
                ?: ""
        } ?: ""
    }
}