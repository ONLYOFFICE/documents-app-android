package app.documents.core.login

import app.documents.core.account.AccountRepository
import app.documents.core.di.dagger.LoginModule
import app.documents.core.model.cloud.CloudPortal
import dagger.BindsInstance
import dagger.Subcomponent
import javax.inject.Scope

@Scope
@Retention(AnnotationRetention.RUNTIME)
annotation class LoginScope

@LoginScope
@Subcomponent(modules = [LoginModule::class])
interface LoginComponent {

    @Subcomponent.Factory
    interface Factory {

        fun create(
            @BindsInstance
            cloudPortal: CloudPortal?
        ): LoginComponent
    }

    val currentPortal: CloudPortal?

    val accountRepository: AccountRepository
    val cloudLoginRepository: CloudLoginRepository
    val googleLoginRepository: GoogleLoginRepository
    val dropboxLoginRepository: DropboxLoginRepository
    val onedriveLoginRepository: OnedriveLoginRepository
    val webdavLoginRepository: WebdavLoginRepository
}