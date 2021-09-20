package app.editors.manager.di.component

import app.documents.core.di.module.LoginModule
import app.documents.core.di.module.LoginScope
import app.documents.core.login.ILoginServiceProvider
import dagger.Component

@Component(modules = [LoginModule::class], dependencies = [AppComponent::class])
@LoginScope
interface LoginComponent {
    val loginService: ILoginServiceProvider
}