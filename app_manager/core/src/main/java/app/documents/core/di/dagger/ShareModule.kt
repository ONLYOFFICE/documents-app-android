package app.documents.core.di.dagger

import app.documents.core.di.dagger.CoreModule.json
import app.documents.core.model.cloud.CloudAccount
import app.documents.core.model.exception.CloudAccountNotFoundException
import app.documents.core.network.share.ShareService
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import dagger.Module
import dagger.Provides
import okhttp3.MediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit

@Module
object ShareModule {

    @Provides
    fun provideShareService(cloudAccount: CloudAccount?, okHttpClient: OkHttpClient): ShareService {
        if (cloudAccount == null) throw CloudAccountNotFoundException
        return Retrofit.Builder()
            .client(okHttpClient)
            .baseUrl(cloudAccount.portal.urlWithScheme)
            .addConverterFactory(json.asConverterFactory(MediaType.get("application/json")))
            .build()
            .create(ShareService::class.java)
    }
}