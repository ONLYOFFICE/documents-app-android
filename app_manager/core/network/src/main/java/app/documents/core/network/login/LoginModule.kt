package app.documents.core.network.login

import dagger.Module
import dagger.Provides
import kotlinx.serialization.json.Json

@Module
object LoginModule {

    @Provides
    fun provideLoginDataSource(json: Json): LoginDataSource {
        return LoginDataSourceImpl(json)
    }
}