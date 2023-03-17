package app.documents.core.di.dagger.storages

import app.documents.core.di.dagger.CoreModule.json
import app.documents.core.network.common.contracts.ApiContract
import app.documents.core.network.common.utils.DropboxUtils
import app.documents.core.network.storages.dropbox.login.DropboxLoginProvider
import app.documents.core.network.storages.dropbox.login.DropboxLoginService
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import dagger.Module
import dagger.Provides
import kotlinx.serialization.ExperimentalSerializationApi
import okhttp3.MediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory

@Module
class DropboxLoginModule {

    @Provides
    @DropboxScope
    fun provideDropboxLogin(dropboxLoginService: DropboxLoginService): DropboxLoginProvider =
        DropboxLoginProvider(dropboxLoginService)

    @Provides
    @DropboxScope
    @OptIn(ExperimentalSerializationApi::class)
    fun provideDropboxLoginService(okHttpClient: OkHttpClient): DropboxLoginService =
        Retrofit.Builder()
            .client(okHttpClient)
            .baseUrl(DropboxUtils.DROPBOX_BASE_URL)
            .addConverterFactory(json.asConverterFactory(MediaType.get(ApiContract.VALUE_CONTENT_TYPE)))
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .build()
            .create(DropboxLoginService::class.java)
}