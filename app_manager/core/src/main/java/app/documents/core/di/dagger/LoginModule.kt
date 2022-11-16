package app.documents.core.di.dagger

import android.content.Context
import app.documents.core.network.login.ILoginServiceProvider
import app.documents.core.network.login.LoginInterceptor
import app.documents.core.network.login.LoginService
import app.documents.core.network.login.LoginServiceProvider
import app.documents.core.storage.preference.NetworkSettings
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import dagger.Module
import dagger.Provides
import kotlinx.serialization.json.Json
import lib.toolkit.base.managers.http.NetworkClient
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Protocol
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Scope

@Scope
@Retention(AnnotationRetention.RUNTIME)
annotation class LoginScope

@Module
class LoginModule {

    @Provides
    fun provideLogin(loginService: LoginService): ILoginServiceProvider = LoginServiceProvider(loginService)

    @Provides
    fun provideLoginService(okHttpClient: OkHttpClient, settings: NetworkSettings): LoginService = Retrofit.Builder()
        .client(okHttpClient)
        .baseUrl(settings.getBaseUrl())
        .addConverterFactory(Json {
            isLenient = true
            ignoreUnknownKeys = true
        }.asConverterFactory(MediaType.get("application/json")))
        .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
        .build()
        .create(LoginService::class.java)

    @Provides
    fun provideOkHttpClient(context: Context, settings: NetworkSettings): OkHttpClient {
        val builder = NetworkClient.getOkHttpBuilder(settings.getSslState(), settings.getCipher())
        builder.protocols(listOf(Protocol.HTTP_1_1))
            .addInterceptor(LoginInterceptor(context))
            .readTimeout(NetworkClient.ClientSettings.READ_TIMEOUT, TimeUnit.SECONDS)
            .writeTimeout(NetworkClient.ClientSettings.WRITE_TIMEOUT, TimeUnit.SECONDS)
            .connectTimeout(NetworkClient.ClientSettings.CONNECT_TIMEOUT, TimeUnit.SECONDS)
        return builder.build()
    }

}