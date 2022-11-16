package app.editors.manager.di.module

import android.content.Context
import app.documents.core.storage.account.CloudAccount
import app.documents.core.network.common.contracts.ApiContract
import app.documents.core.storage.preference.NetworkSettings
import app.editors.manager.app.Api
import app.editors.manager.app.RoomApi
import app.editors.manager.managers.retrofit.BaseInterceptor
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.runBlocking
import lib.toolkit.base.managers.http.NetworkClient
import lib.toolkit.base.managers.http.NetworkClient.ClientSettings.CONNECT_TIMEOUT
import lib.toolkit.base.managers.http.NetworkClient.ClientSettings.READ_TIMEOUT
import lib.toolkit.base.managers.http.NetworkClient.ClientSettings.WRITE_TIMEOUT
import lib.toolkit.base.managers.utils.AccountUtils
import lib.toolkit.base.managers.utils.TimeUtils
import okhttp3.OkHttpClient
import okhttp3.Protocol
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Scope


@Scope
@Retention(AnnotationRetention.RUNTIME)
annotation class ApiScope

@Module
class ApiModule {

    @Provides
    @ApiScope
    fun provideApi(factory: GsonConverterFactory, client: OkHttpClient, settings: NetworkSettings): Api {
        var url = settings.getBaseUrl()
        if (url.isEmpty()) {
            url = ApiContract.DEFAULT_HOST
        }
        return Retrofit.Builder()
            .baseUrl(url)
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .addConverterFactory(factory)
            .client(client)
            .build().create(Api::class.java)
    }

    @Provides
    @ApiScope
    fun provideRoomService(factory: GsonConverterFactory, okHttpClient: OkHttpClient, settings: NetworkSettings): RoomApi {
        return Retrofit.Builder()
            .client(okHttpClient)
            .baseUrl(settings.getBaseUrl())
            .addConverterFactory(factory)
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .build()
            .create(RoomApi::class.java)
    }

    @Provides
    @ApiScope
    fun provideConverterFactory(): GsonConverterFactory {
        return GsonConverterFactory.create(
            GsonBuilder()
                .setLenient()
                .setPrettyPrinting()
                .setDateFormat(TimeUtils.OUTPUT_PATTERN_DEFAULT)
                .serializeNulls()
                .create()
        )
    }

    @Provides
    @ApiScope
    fun provideOkHttpClient(@Named("token") token: String, settings: NetworkSettings): OkHttpClient {
        val builder = NetworkClient.getOkHttpBuilder(settings.getSslState(), settings.getCipher())
        builder.protocols(listOf(Protocol.HTTP_1_1))
            .readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
            .writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS)
            .connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)
            .addInterceptor(BaseInterceptor(token))
        return builder.build()
    }

    @Provides
    @ApiScope
    @Named("token")
    fun provideToken(context: Context, account: CloudAccount?): String = runBlocking {
        account?.let { cloudAccount ->
            return@runBlocking AccountUtils.getToken(context = context, cloudAccount.getAccountName())
                ?: ""
        } ?: ""
    }

}