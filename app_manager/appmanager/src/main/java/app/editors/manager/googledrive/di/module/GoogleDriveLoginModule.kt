package app.editors.manager.googledrive.di.module

import app.editors.manager.googledrive.googledrive.login.GoogleDriveLoginService
import app.editors.manager.googledrive.googledrive.login.GoogleDriveLoginServiceProvider
import app.editors.manager.googledrive.googledrive.login.IGoogleDriveLoginServiceProvider
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import dagger.Module
import dagger.Provides
import kotlinx.serialization.json.Json
import okhttp3.MediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory


@Module
class GoogleDriveLoginModule {

    @Provides
    fun provideGoogleDriveLogin(googleDriveLoginService: GoogleDriveLoginService): IGoogleDriveLoginServiceProvider = GoogleDriveLoginServiceProvider(googleDriveLoginService)


    @Provides
    fun provideGoogleDriveLoginService(okHttpClient: OkHttpClient): GoogleDriveLoginService = Retrofit.Builder()
        .client(okHttpClient)
        .baseUrl("https://api.dropboxapi.com/")
        .addConverterFactory(Json {
            isLenient = true
            ignoreUnknownKeys = true
        }.asConverterFactory(MediaType.get("application/json")))
        .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
        .build()
        .create(GoogleDriveLoginService::class.java)
}