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
    ): CloudLoginDataSource {
        return CloudLoginDataSourceImpl(json, okHttpClient, cloudPortal)
    }

    @Provides
    fun provideGoogleLoginDataSource(
        json: Json,
        @LoginOkHttpClient okHttpClient: OkHttpClient
    ): GoogleLoginDataSource {
        return GoogleLoginDataSourceImpl(json, okHttpClient)
    }

    @Provides
    fun provideDropboxLoginDataSource(
        json: Json,
        @LoginOkHttpClient okHttpClient: OkHttpClient
    ): DropboxLoginDataSource {
        return DropboxLoginDataSourceImpl(json, okHttpClient)
    }

    @Provides
    fun provideOnedriveLoginDataSource(
        json: Json,
        @LoginOkHttpClient okHttpClient: OkHttpClient
    ): OnedriveLoginDataSource {
        return OnedriveLoginDataSourceImpl(json, okHttpClient)
    }

    @Provides
    fun provideWebdavLoginDataSource(
        @LoginOkHttpClient okHttpClient: OkHttpClient
    ): WebdavLoginDataSource {
        return WebdavLoginDataSourceImpl(okHttpClient)
    }

    @Provides
    fun provideOwnCloudLoginDataSource(
        json: Json,
        @LoginOkHttpClient okHttpClient: OkHttpClient
    ): OwnCloudLoginDataSource {
        return OwnCloudLoginDataSourceImpl(json, okHttpClient)
    }
}