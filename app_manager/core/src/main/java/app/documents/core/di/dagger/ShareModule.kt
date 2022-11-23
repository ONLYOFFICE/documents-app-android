package app.documents.core.di.dagger

import android.content.Context
import app.documents.core.storage.account.CloudAccount
import app.documents.core.network.common.interceptors.BaseInterceptor
import app.documents.core.network.share.ShareRepositoryImpl
import app.documents.core.storage.preference.NetworkSettings
import app.documents.core.network.share.ShareService
import app.documents.core.repositories.ShareRepository
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import dagger.Module
import dagger.Provides
import kotlinx.serialization.ExperimentalSerializationApi
import okhttp3.MediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit

@Module
object ShareModule {

    @Provides
    @OptIn(ExperimentalSerializationApi::class)
    fun provideShareService(okHttpClient: OkHttpClient, settings: NetworkSettings): ShareService = Retrofit.Builder()
        .client(okHttpClient)
        .baseUrl(settings.getBaseUrl())
        .addConverterFactory(json.asConverterFactory(MediaType.get("application/json")))
        .build()
        .create(ShareService::class.java)

    @Provides
    fun provideOkHttpClient(@Named("token") token: String, settings: NetworkSettings, context: Context): OkHttpClient {
        val builder = NetworkClient.getOkHttpBuilder(settings.getSslState(), settings.getCipher())
        builder.protocols(listOf(Protocol.HTTP_1_1))
            .readTimeout(NetworkClient.ClientSettings.READ_TIMEOUT, TimeUnit.SECONDS)
            .writeTimeout(NetworkClient.ClientSettings.READ_TIMEOUT, TimeUnit.SECONDS)
            .connectTimeout(NetworkClient.ClientSettings.READ_TIMEOUT, TimeUnit.SECONDS)
            .addInterceptor(BaseInterceptor(token, context))
        return builder.build()
    }

    @Provides
    @Named("token")
    fun provideToken(context: Context, cloudAccount: CloudAccount?): String {
        cloudAccount?.let {
            return AccountUtils.getToken(context = context, cloudAccount.getAccountName())
                ?: throw RuntimeException("Token can't be null")
        } ?: throw RuntimeException("Token can't be null")
    }

    @Provides
    fun provideShareRepository(shareService: ShareService): ShareRepository {
        return ShareRepositoryImpl(shareService)
    }
}