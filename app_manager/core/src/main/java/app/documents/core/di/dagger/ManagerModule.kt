package app.documents.core.di.dagger

import android.content.Context
import app.documents.core.account.AccountManager
import app.documents.core.database.datasource.CloudDataSource
import app.documents.core.manager.FileOpenRepository
import app.documents.core.manager.FileOpenRepositoryImpl
import app.documents.core.manager.ManagerRepository
import app.documents.core.manager.ManagerRepositoryImpl
import app.documents.core.model.cloud.CloudAccount
import app.documents.core.model.exception.CloudAccountNotFoundException
import app.documents.core.network.common.NetworkClient
import app.documents.core.network.common.interceptors.WebDavInterceptor
import app.documents.core.network.manager.ManagerService
import app.documents.core.network.room.RoomService
import app.documents.core.network.webdav.ConverterFactory
import app.documents.core.network.webdav.WebDavService
import app.documents.core.providers.CloudFileProvider
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
    fun provideFileOpenRepository(
        context: Context,
        cloudFileProvider: CloudFileProvider,
        cloudAccount: CloudAccount?
    ): FileOpenRepository {
        return FileOpenRepositoryImpl(
            context = context,
            cloudFileProvider = cloudFileProvider,
            cloudAccount = cloudAccount,
        )
    }

    @Provides
    fun provideApi(factory: GsonConverterFactory, client: OkHttpClient, cloudAccount: CloudAccount?): ManagerService {
        if (cloudAccount == null) throw CloudAccountNotFoundException
        return Retrofit.Builder()
            .baseUrl(cloudAccount.portal.urlWithScheme)
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .addConverterFactory(factory)
            .client(client)
            .build()
            .create(ManagerService::class.java)
    }

    @Provides
    fun provideRoomService(
        cloudAccount: CloudAccount?,
        factory: GsonConverterFactory,
        okHttpClient: OkHttpClient
    ): RoomService {
        if (cloudAccount == null) throw CloudAccountNotFoundException
        return Retrofit.Builder()
            .client(okHttpClient)
            .baseUrl(cloudAccount.portal.urlWithScheme)
            .addConverterFactory(factory)
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .build()
            .create(RoomService::class.java)
    }

    @Provides
    fun provideWebDavService(
        cloudAccount: CloudAccount?,
        accountManager: AccountManager
    ): WebDavService {
        if (cloudAccount == null) throw CloudAccountNotFoundException
        val okHttpClient = NetworkClient.getOkHttpBuilder(
            portalSettings = cloudAccount.portal.settings,
            WebDavInterceptor(
                cloudAccount.login,
                accountManager.getPassword(cloudAccount.accountName)
            )
        ).build()

        return Retrofit.Builder()
            .baseUrl(cloudAccount.portal.urlWithScheme)
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .addConverterFactory(ConverterFactory())
            .client(okHttpClient)
            .build()
            .create(WebDavService::class.java)
    }
}