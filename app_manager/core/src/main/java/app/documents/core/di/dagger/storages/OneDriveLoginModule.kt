package app.documents.core.di.dagger.storages

import app.documents.core.di.dagger.CoreModule.json
import app.documents.core.network.common.utils.OneDriveUtils
import app.documents.core.network.storages.onedrive.login.OneDriveLoginProvider
import app.documents.core.network.storages.onedrive.login.OneDriveLoginService
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import dagger.Module
import dagger.Provides
import kotlinx.serialization.ExperimentalSerializationApi
import app.documents.core.network.common.NetworkClient
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Protocol
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Qualifier

@Qualifier
annotation class OneDriveLoginClient

@Module
class OneDriveLoginModule {

    @Provides
    @OneDriveScope
    fun provideOneDriveLogin(oneDriveLoginService: OneDriveLoginService): OneDriveLoginProvider =
        OneDriveLoginProvider(oneDriveLoginService)

    @Provides
    @OneDriveScope
    @OptIn(ExperimentalSerializationApi::class)
    fun provideOneDriveLoginService(@OneDriveLoginClient okHttpClient: OkHttpClient): OneDriveLoginService =
        Retrofit.Builder()
            .client(okHttpClient)
            .baseUrl(OneDriveUtils.ONEDRIVE_AUTH_URL)
            .addConverterFactory(json.asConverterFactory(MediaType.get(OneDriveUtils.REQUEST_MEDIA_TYPE)))
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .build()
            .create(OneDriveLoginService::class.java)

    @Provides
    @OneDriveScope
    @OneDriveLoginClient
    fun provideOkHttpClient(): OkHttpClient {
        val builder = NetworkClient.getOkHttpBuilder(isSslOn = true, isCiphers = false)
        builder.protocols(listOf(Protocol.HTTP_1_1))
            .readTimeout(NetworkClient.ClientSettings.READ_TIMEOUT, TimeUnit.SECONDS)
            .writeTimeout(NetworkClient.ClientSettings.WRITE_TIMEOUT, TimeUnit.SECONDS)
            .connectTimeout(NetworkClient.ClientSettings.CONNECT_TIMEOUT, TimeUnit.SECONDS)
        return builder.build()
    }
}