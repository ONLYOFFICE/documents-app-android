package app.documents.core.di.dagger.storages

import app.documents.core.di.dagger.CoreModule.json
import app.documents.core.network.storages.onedrive.login.OneDriveLoginProvider
import app.documents.core.network.storages.onedrive.login.OneDriveLoginService
import app.documents.core.storage.preference.NetworkSettings
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import dagger.Module
import dagger.Provides
import kotlinx.serialization.ExperimentalSerializationApi
import okhttp3.MediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory

@Module
class OneDriveLoginModule {

    @Provides
    @OneDriveScope
    fun provideOneDriveLogin(oneDriveLoginService: OneDriveLoginService): OneDriveLoginProvider =
        OneDriveLoginProvider(oneDriveLoginService)

    @Provides
    @OneDriveScope
    @OptIn(ExperimentalSerializationApi::class)
    fun provideOneDriveLoginService(okHttpClient: OkHttpClient, settings: NetworkSettings): OneDriveLoginService =
        Retrofit.Builder()
            .client(okHttpClient)
            .baseUrl(settings.getBaseUrl())
            .addConverterFactory(json.asConverterFactory(MediaType.get(OneDriveModule.MEDIA_TYPE)))
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .build()
            .create(OneDriveLoginService::class.java)
}