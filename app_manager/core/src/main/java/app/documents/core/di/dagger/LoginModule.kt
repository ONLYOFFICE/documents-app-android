package app.documents.core.di.dagger

import app.documents.core.account.AccountRepository
import app.documents.core.login.GoogleLoginRepository
import app.documents.core.login.GoogleLoginRepositoryImpl
import app.documents.core.login.LoginRepository
import app.documents.core.login.LoginRepositoryImpl
import app.documents.core.login.LoginScope
import app.documents.core.model.cloud.CloudPortal
import app.documents.core.network.login.GoogleLoginDataSource
import app.documents.core.network.login.LoginDataSource
import app.documents.core.network.login.LoginNetworkModule
import dagger.Module
import dagger.Provides

@Module(includes = [LoginNetworkModule::class])
class LoginModule {

    @Provides
    @LoginScope
    fun provideLoginRepository(
        cloudPortal: CloudPortal?,
        loginDataSource: LoginDataSource,
        accountRepository: AccountRepository
    ): LoginRepository {
        return LoginRepositoryImpl(
            cloudPortal,
            loginDataSource,
            accountRepository
        )
    }

    @Provides
    @LoginScope
    fun provideGoogleLoginRepository(
        googleLoginDataSource: GoogleLoginDataSource,
        accountRepository: AccountRepository
    ): GoogleLoginRepository {
        return GoogleLoginRepositoryImpl(
            googleLoginDataSource,
            accountRepository
        )
    }
}