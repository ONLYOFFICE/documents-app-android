package app.documents.core.network.login

import app.documents.core.model.cloud.CloudPortal
import dagger.Module
import dagger.Provides
import kotlinx.serialization.json.Json

@Module
object LoginModule {

    @Provides
    fun provideLoginDataSource(
        json: Json,
        cloudPortal: CloudPortal?
    ): LoginDataSource {
        return LoginDataSourceImpl(json, cloudPortal)
    }
}