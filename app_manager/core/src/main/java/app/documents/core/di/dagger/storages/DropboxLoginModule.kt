package app.documents.core.di.dagger.storages

import app.documents.core.di.dagger.CoreModule.json
import app.documents.core.network.common.contracts.ApiContract
import app.documents.core.network.common.utils.DropboxUtils
import app.documents.core.network.storages.dropbox.login.DropboxLoginProvider
import app.documents.core.network.storages.dropbox.login.DropboxLoginService
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import dagger.Module
import dagger.Provides
import app.documents.core.network.common.NetworkClient
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Protocol
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Named

@Module
class DropboxLoginModule {

    @Provides
    @Named("login")
    @DropboxScope
    fun provideOkHttpClient(): OkHttpClient {
        val builder = NetworkClient.getOkHttpBuilder(isSslOn = true, isCiphers = false)
        builder.protocols(listOf(Protocol.HTTP_1_1))
            .readTimeout(NetworkClient.ClientSettings.READ_TIMEOUT, TimeUnit.SECONDS)
            .writeTimeout(NetworkClient.ClientSettings.WRITE_TIMEOUT, TimeUnit.SECONDS)
            .connectTimeout(NetworkClient.ClientSettings.CONNECT_TIMEOUT, TimeUnit.SECONDS)
        return builder.build()
    }

    @Provides
    @DropboxScope
    fun provideDropboxLogin(dropboxLoginService: DropboxLoginService): DropboxLoginProvider =
        DropboxLoginProvider(dropboxLoginService)

    @Provides
    @DropboxScope
    fun provideDropboxLoginService(@Named("login") okHttpClient: OkHttpClient): DropboxLoginService =
        Retrofit.Builder()
            .client(okHttpClient)
            .baseUrl(DropboxUtils.DROPBOX_BASE_URL)
            .addConverterFactory(json.asConverterFactory(MediaType.get(ApiContract.VALUE_CONTENT_TYPE)))
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .build()
            .create(DropboxLoginService::class.java)
}