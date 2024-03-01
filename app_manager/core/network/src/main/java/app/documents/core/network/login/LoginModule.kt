package app.documents.core.network.login

import dagger.Module
import dagger.Provides
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import javax.inject.Named

@Module
object LoginModule {

    @Provides
    fun provideLoginDataSource(
        json: Json,
        okHttpClient: OkHttpClient,
        @Named("baseUrl") baseUrl: String
    ): LoginDataSource {
        return LoginDataSourceImpl(json, okHttpClient, baseUrl)
    }
}