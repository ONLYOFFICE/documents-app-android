package app.documents.core.di.dagger

import app.documents.core.model.cloud.CloudAccount
import app.documents.core.network.manager.ManagerService
import app.documents.core.network.room.RoomService
import app.documents.core.storage.preference.NetworkSettings
import dagger.Module
import dagger.Provides
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

@Module
class ManagerModule {

    @Provides
    fun provideApi(factory: GsonConverterFactory, client: OkHttpClient, cloudAccount: CloudAccount?): ManagerService {
        return Retrofit.Builder()
            .baseUrl(cloudAccount?.portal?.url ?: "https://")
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .addConverterFactory(factory)
            .client(client)
            .build().create(ManagerService::class.java)
    }

    @Provides
    fun provideRoomService(factory: GsonConverterFactory, okHttpClient: OkHttpClient, settings: NetworkSettings): RoomService {
        return Retrofit.Builder()
            .client(okHttpClient)
            .baseUrl(settings.getBaseUrl())
            .addConverterFactory(factory)
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .build()
            .create(RoomService::class.java)
    }

}