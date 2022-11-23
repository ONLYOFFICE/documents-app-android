package app.documents.core.di.dagger

import app.documents.core.di.dagger.CoreModule.json
import app.documents.core.network.login.ILoginServiceProvider
import app.documents.core.network.login.LoginService
import app.documents.core.network.login.LoginServiceProvider
import app.documents.core.storage.preference.NetworkSettings
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import dagger.Module
import dagger.Provides
import kotlinx.serialization.json.Json
import okhttp3.MediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory

@Module
class LoginModule {

    @Provides
    fun provideLoginService(okHttpClient: OkHttpClient, settings: NetworkSettings): LoginService = Retrofit.Builder()
        .client(okHttpClient)
        .baseUrl(settings.getBaseUrl())
        .addConverterFactory(json.asConverterFactory(MediaType.get("application/json")))
        .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
        .build()
        .create(LoginService::class.java)

    @Provides
    fun provideLogin(loginService: LoginService): ILoginServiceProvider = LoginServiceProvider(loginService)

}