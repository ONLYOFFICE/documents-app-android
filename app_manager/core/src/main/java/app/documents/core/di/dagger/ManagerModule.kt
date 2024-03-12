package app.documents.core.di.dagger

import app.documents.core.database.datasource.CloudDataSource
import app.documents.core.manager.ManagerRepository
import app.documents.core.manager.ManagerRepositoryImpl
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
    fun provideManagerRepository(
        cloudAccount: CloudAccount?,
        managerService: ManagerService,
        cloudDataSource: CloudDataSource,
    ): ManagerRepository {
        return ManagerRepositoryImpl(
            cloudPortal = cloudAccount?.portal,
            managerService = managerService,
            cloudDataSource = cloudDataSource
        )
    }

    @Provides
    fun provideApi(factory: GsonConverterFactory, client: OkHttpClient, cloudAccount: CloudAccount?): ManagerService {
        return Retrofit.Builder()
            .baseUrl(cloudAccount?.portal?.urlWithScheme.orEmpty())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .addConverterFactory(factory)
            .client(client)
            .build().create(ManagerService::class.java)
    }

    @Provides
    fun provideRoomService(
        factory: GsonConverterFactory,
        okHttpClient: OkHttpClient,
        settings: NetworkSettings
    ): RoomService {
        return Retrofit.Builder()
            .client(okHttpClient)
            .baseUrl(settings.getBaseUrl())
            .addConverterFactory(factory)
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .build()
            .create(RoomService::class.java)
    }

}