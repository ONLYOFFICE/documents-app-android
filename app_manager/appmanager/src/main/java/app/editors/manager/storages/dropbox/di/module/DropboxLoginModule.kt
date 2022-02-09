package app.editors.manager.storages.dropbox.di.module

import app.editors.manager.storages.dropbox.dropbox.login.DropboxLoginService
import app.editors.manager.storages.dropbox.dropbox.login.DropboxLoginServiceProvider
import app.editors.manager.storages.dropbox.dropbox.login.IDropboxLoginServiceProvider
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
annotation class DropboxLoginScope

@Module
class DropboxLoginModule {
    @Provides
    fun provideDropboxLogin(dropboxLoginService: DropboxLoginService): IDropboxLoginServiceProvider = DropboxLoginServiceProvider(dropboxLoginService)


    @Provides
    fun provideDropboxLoginService(okHttpClient: OkHttpClient): DropboxLoginService = Retrofit.Builder()
        .client(okHttpClient)
        .baseUrl("https://api.dropboxapi.com/")
        .addConverterFactory(Json {
            isLenient = true
            ignoreUnknownKeys = true
        }.asConverterFactory(MediaType.get("application/json")))
        .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
        .build()
        .create(DropboxLoginService::class.java)
}