package app.documents.core.di.dagger

import app.documents.core.account.AccountRepository
import app.documents.core.login.CloudLoginRepository
import app.documents.core.login.CloudLoginRepositoryImpl
import app.documents.core.login.DropboxLoginRepository
import app.documents.core.login.DropboxLoginRepositoryImpl
import app.documents.core.login.GoogleLoginRepository
import app.documents.core.login.GoogleLoginRepositoryImpl
import app.documents.core.login.LoginScope
import app.documents.core.login.OnedriveLoginRepository
import app.documents.core.login.OnedriveLoginRepositoryImpl
import app.documents.core.login.WebdavLoginRepository
import app.documents.core.login.WebdavLoginRepositoryImpl
import app.documents.core.model.cloud.CloudPortal
import app.documents.core.network.login.CloudLoginDataSource
import app.documents.core.network.login.DropboxLoginDataSource
import app.documents.core.network.login.GoogleLoginDataSource
import app.documents.core.network.login.LoginNetworkModule
import app.documents.core.network.login.OnedriveLoginDataSource
import app.documents.core.network.login.WebdavLoginDataSource
import dagger.Module
import dagger.Provides

@Module(includes = [LoginNetworkModule::class])
class LoginModule {

    @Provides
    @LoginScope
    fun provideLoginRepository(
        cloudPortal: CloudPortal?,
        cloudLoginDataSource: CloudLoginDataSource,
        accountRepository: AccountRepository
    ): CloudLoginRepository {
        return CloudLoginRepositoryImpl(
            cloudPortal,
            cloudLoginDataSource,
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
            accountRepository,
            googleLoginDataSource
        )
    }

    @Provides
    @LoginScope
    fun provideDropboxLoginRepository(
        dropboxLoginDataSource: DropboxLoginDataSource,
        accountRepository: AccountRepository
    ): DropboxLoginRepository {
        return DropboxLoginRepositoryImpl(
            accountRepository,
            dropboxLoginDataSource,
        )
    }

    @Provides
    @LoginScope
    fun provideOnedriveLoginRepository(
        onedriveLoginDataSource: OnedriveLoginDataSource,
        accountRepository: AccountRepository
    ): OnedriveLoginRepository {
        return OnedriveLoginRepositoryImpl(
            accountRepository,
            onedriveLoginDataSource
        )
    }

    @Provides
    @LoginScope
    fun provideWebdavLoginRepository(
        webdavLoginDataSource: WebdavLoginDataSource,
        accountRepository: AccountRepository
    ): WebdavLoginRepository {
        return WebdavLoginRepositoryImpl(
            accountRepository,
            webdavLoginDataSource
        )
    }
}