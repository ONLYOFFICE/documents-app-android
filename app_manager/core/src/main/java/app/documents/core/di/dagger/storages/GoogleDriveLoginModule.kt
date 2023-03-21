package app.documents.core.di.dagger.storages

import app.documents.core.di.dagger.CoreModule.json
import app.documents.core.network.common.contracts.ApiContract
import app.documents.core.network.common.utils.GoogleDriveUtils
import app.documents.core.network.storages.googledrive.login.GoogleDriveLoginService
import app.documents.core.network.storages.googledrive.login.GoogleDriveLoginProvider
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import dagger.Module
import dagger.Provides
import kotlinx.serialization.ExperimentalSerializationApi
import okhttp3.MediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory

@Module
class GoogleDriveLoginModule {

    @Provides
    @GoogleDriveScope
    fun provideGoogleDriveLogin(googleDriveLoginService: GoogleDriveLoginService): GoogleDriveLoginProvider =
        GoogleDriveLoginProvider(googleDriveLoginService)

    @Provides
    @GoogleDriveScope
    @OptIn(ExperimentalSerializationApi::class)
    fun provideGoogleDriveLoginService(okHttpClient: OkHttpClient): GoogleDriveLoginService =
        Retrofit.Builder()
            .client(okHttpClient)
            .baseUrl(GoogleDriveUtils.GOOGLE_DRIVE_AUTH_URL)
            .addConverterFactory(json.asConverterFactory(MediaType.get(ApiContract.VALUE_CONTENT_TYPE)))
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .build()
            .create(GoogleDriveLoginService::class.java)
}