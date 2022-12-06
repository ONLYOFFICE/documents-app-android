package app.documents.core.di.dagger.storages

import android.content.Context
import app.documents.core.di.dagger.CoreModule.json
import app.documents.core.di.dagger.Token
import app.documents.core.network.common.interceptors.BaseInterceptor
import app.documents.core.network.common.utils.OneDriveUtils
import app.documents.core.network.storages.onedrive.api.OneDriveProvider
import app.documents.core.network.storages.onedrive.api.OneDriveService
import app.documents.core.storage.account.CloudAccount
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.ExperimentalSerializationApi
import lib.toolkit.base.managers.http.NetworkClient
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
annotation class OneDriveScope

@Module
class OneDriveModule {

    @Provides
    @OneDriveScope
    fun provideOneDrive(oneDriveService: OneDriveService): OneDriveProvider = OneDriveProvider(oneDriveService)

    @Provides
    @OneDriveScope
    @OptIn(ExperimentalSerializationApi::class)
    fun provideOneDriveService(okHttpClient: OkHttpClient): OneDriveService = Retrofit.Builder()
        .client(okHttpClient)
        .baseUrl(OneDriveUtils.ONEDRIVE_BASE_URL)
        .addConverterFactory(json.asConverterFactory(MediaType.get(OneDriveUtils.REQUEST_MEDIA_TYPE)))
        .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
        .build()
        .create(OneDriveService::class.java)

    @Provides
    @OneDriveScope
    fun provideOkHttpClient(@Token token: String?, context: Context): OkHttpClient {
        val builder = NetworkClient.getOkHttpBuilder(isSslOn = true, isCiphers = false)
        builder.protocols(listOf(Protocol.HTTP_1_1))
            .readTimeout(NetworkClient.ClientSettings.READ_TIMEOUT, TimeUnit.SECONDS)
            .writeTimeout(NetworkClient.ClientSettings.WRITE_TIMEOUT, TimeUnit.SECONDS)
            .connectTimeout(NetworkClient.ClientSettings.CONNECT_TIMEOUT, TimeUnit.SECONDS)
            .addInterceptor(BaseInterceptor(token, context))
        return builder.build()
    }

    @Provides
    @OneDriveScope
    @Token
    fun provideToken(context: Context, account: CloudAccount?): String? = runBlocking {
        return@runBlocking AccountUtils.getToken(
            context = context,
            accountName = account?.getAccountName() ?: return@runBlocking null
        )
    }
}