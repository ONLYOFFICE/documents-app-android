package app.editors.manager.onedrive.di.module


import app.editors.manager.onedrive.onedrive.login.IOneDriveLoginServiceProvider
import app.editors.manager.onedrive.onedrive.login.OneDriveLoginService
import app.editors.manager.onedrive.onedrive.login.OneDriveLoginServiceProvider
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
annotation class OneDriveLoginScope

@Module
class OneDriveLoginModule {
    @Provides
    fun provideOneDriveLogin(oneDriveLoginService: OneDriveLoginService): IOneDriveLoginServiceProvider = OneDriveLoginServiceProvider(oneDriveLoginService)


    @Provides
    fun provideOneDriveLoginService(okHttpClient: OkHttpClient): OneDriveLoginService = Retrofit.Builder()
        .client(okHttpClient)
        .baseUrl("https://graph.microsoft.com/")
        .addConverterFactory(Json {
            isLenient = true
            ignoreUnknownKeys = true
        }.asConverterFactory(MediaType.get("application/json;odata.metadata=minimal;odata.streaming=true;IEEE754Compatible=false;charset=utf-8")))
        .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
        .build()
        .create(OneDriveLoginService::class.java)
}