package app.editors.manager.storages.onedrive.di.module

import app.editors.manager.storages.onedrive.onedrive.authorization.IOneDriveAuthServiceProvider
import app.editors.manager.storages.onedrive.onedrive.authorization.OneDriveAuthService
import app.editors.manager.storages.onedrive.onedrive.authorization.OneDriveAuthServiceProvider
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import dagger.Module
import dagger.Provides
import kotlinx.serialization.json.Json
import okhttp3.MediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import javax.inject.Scope

@Scope
@Retention(AnnotationRetention.RUNTIME)
annotation class OneDriveAuthScope

@Module
class OneDriveAuthModule {
    @Provides
    fun provideOneDriveAuth(oneDriveAuthService: OneDriveAuthService): IOneDriveAuthServiceProvider = OneDriveAuthServiceProvider(oneDriveAuthService)


    @Provides
    fun provideOneDriveAuthService(okHttpClient: OkHttpClient): OneDriveAuthService = Retrofit.Builder()
        .client(okHttpClient)
        .baseUrl("https://login.microsoftonline.com/")
        .addConverterFactory(Json {
            isLenient = true
            ignoreUnknownKeys = true
        }.asConverterFactory(MediaType.get("application/json;odata.metadata=minimal;odata.streaming=true;IEEE754Compatible=false;charset=utf-8")))
        .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
        .build()
        .create(OneDriveAuthService::class.java)
}