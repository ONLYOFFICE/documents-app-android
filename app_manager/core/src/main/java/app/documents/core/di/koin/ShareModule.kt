package app.documents.core.di.koin

import app.documents.core.di.dagger.CoreModule
import app.documents.core.network.common.interceptors.BaseInterceptor
import app.documents.core.network.share.ShareService
import app.documents.core.storage.preference.NetworkSettings
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import app.documents.core.network.common.NetworkClient
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Protocol
import org.koin.android.ext.koin.androidApplication
import org.koin.core.parameter.parametersOf
import org.koin.dsl.module
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit

//val shareModule = module {
//
//    single { NetworkSettings(androidApplication()) }
//
//    factory {(token: String) ->
//        Retrofit.Builder()
//            .client(get<OkHttpClient> {
//                parametersOf(token, get<NetworkSettings>())
//            })
//            .baseUrl(get<NetworkSettings>().getBaseUrl())
//            .addConverterFactory(CoreModule.json.asConverterFactory(MediaType.get("application/json")))
//            .build()
//            .create(ShareService::class.java)
//    }
//
//    factory { (token: String, settings: NetworkSettings) ->
//        val builder = NetworkClient.getOkHttpBuilder(settings.getSslState(), settings.getCipher())
//        builder.protocols(listOf(Protocol.HTTP_1_1))
//            .readTimeout(NetworkClient.ClientSettings.READ_TIMEOUT, TimeUnit.SECONDS)
//            .writeTimeout(NetworkClient.ClientSettings.WRITE_TIMEOUT, TimeUnit.SECONDS)
//            .connectTimeout(NetworkClient.ClientSettings.CONNECT_TIMEOUT, TimeUnit.SECONDS)
//            .addInterceptor(BaseInterceptor(token, androidApplication()))
//            .build()
//    }
//
//}