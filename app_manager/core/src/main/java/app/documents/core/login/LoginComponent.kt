package app.documents.core.login

import app.documents.core.di.dagger.LoginModule2
import app.documents.core.model.cloud.Scheme
import app.documents.core.network.login.PortalScheme
import app.documents.core.network.login.PortalUrl
import dagger.BindsInstance
import dagger.Subcomponent
import javax.inject.Scope

@Scope
@Retention(AnnotationRetention.RUNTIME)
annotation class LoginScope

@LoginScope
@Subcomponent(modules = [LoginModule2::class])
interface LoginComponent {

    @Subcomponent.Factory
    interface Factory {

        fun create(
            @BindsInstance
            @PortalUrl
            portalUrl: String,

            @BindsInstance
            @PortalScheme
            scheme: Scheme
        ): LoginComponent
    }

    val loginRepository: LoginRepository
}