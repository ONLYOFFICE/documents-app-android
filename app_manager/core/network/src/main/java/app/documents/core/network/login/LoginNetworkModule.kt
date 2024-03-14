package app.documents.core.network.login

import app.documents.core.model.cloud.CloudPortal
import dagger.Module
import dagger.Provides
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import javax.inject.Qualifier

@Qualifier
annotation class LoginOkHttpClient

@Module
object LoginNetworkModule {

    @Provides
    fun provideLoginDataSource(
        json: Json,
        @LoginOkHttpClient okHttpClient: OkHttpClient,
        cloudPortal: CloudPortal?
    ): LoginDataSource {
        return LoginDataSourceImpl(json, okHttpClient, cloudPortal)
    }

    @Provides
    fun provideGoogleLoginDataSource(
        json: Json,
        @LoginOkHttpClient okHttpClient: OkHttpClient
    ): GoogleLoginDataSource {
        return GoogleLoginDataSourceImpl(json, okHttpClient)
    }
}