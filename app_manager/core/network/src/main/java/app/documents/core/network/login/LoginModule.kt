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
object LoginModule {

    @Provides
    fun provideLoginDataSource(
        json: Json,
        @LoginOkHttpClient okHttpClient: OkHttpClient,
        cloudPortal: CloudPortal?
    ): LoginDataSource {
        return LoginDataSourceImpl(json, okHttpClient, cloudPortal)
    }
}