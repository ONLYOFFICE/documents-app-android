package app.documents.core.network.login

import app.documents.core.model.cloud.Scheme
import dagger.Module
import dagger.Provides
import kotlinx.serialization.json.Json
import javax.inject.Qualifier

@Qualifier
annotation class PortalUrl

@Qualifier
annotation class PortalScheme

@Module
object LoginModule {

    @Provides
    fun provideLoginDataSource(
        json: Json,
        @PortalUrl portalUrl: String,
        @PortalScheme portalScheme: Scheme
    ): LoginDataSource {
        return LoginDataSourceImpl(json, portalUrl, portalScheme)
    }
}