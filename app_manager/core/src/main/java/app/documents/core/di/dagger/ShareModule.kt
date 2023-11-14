package app.documents.core.di.dagger

import app.documents.core.di.dagger.CoreModule.json
import app.documents.core.network.share.ShareService
import app.documents.core.storage.preference.NetworkSettings
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import dagger.Module
import dagger.Provides
import okhttp3.MediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit

@Module
object ShareModule {

    @Provides
    fun provideShareService(okHttpClient: OkHttpClient, settings: NetworkSettings): ShareService = Retrofit.Builder()
        .client(okHttpClient)
        .baseUrl(settings.getBaseUrl())
        .addConverterFactory(json.asConverterFactory(MediaType.get("application/json")))
        .build()
        .create(ShareService::class.java)

}