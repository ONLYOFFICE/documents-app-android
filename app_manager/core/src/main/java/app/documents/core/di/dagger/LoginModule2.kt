package app.documents.core.di.dagger

import app.documents.core.account.AccountManager
import app.documents.core.database.datasource.CloudDataSource
import app.documents.core.login.LoginRepository
import app.documents.core.login.LoginRepositoryImpl
import app.documents.core.login.LoginScope
import app.documents.core.network.login.LoginDataSource
import app.documents.core.network.login.LoginModule
import app.documents.core.storage.preference.NetworkSettings
import dagger.Module
import dagger.Provides

@Module(includes = [LoginModule::class])
class LoginModule2 {

    @Provides
    @LoginScope
    fun provideLoginRepository(
        loginDataSource: LoginDataSource,
        cloudDataSource: CloudDataSource,
        networkSettings: NetworkSettings,
        accountManager: AccountManager
    ): LoginRepository {
        return LoginRepositoryImpl(
            loginDataSource,
            networkSettings,
            accountManager,
            cloudDataSource
        )
    }
}