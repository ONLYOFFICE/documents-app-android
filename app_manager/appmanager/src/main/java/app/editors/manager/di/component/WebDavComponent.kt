package app.editors.manager.di.component

import app.documents.core.di.module.WebDavApiModule
import app.documents.core.di.module.WebDavScope
import app.documents.core.webdav.WebDavApi
import dagger.Component

@Component(modules = [WebDavApiModule::class], dependencies = [AppComponent::class])
@WebDavScope
interface WebDavComponent {

    @Component.Builder
    interface Builder {

        fun appComponent(component: AppComponent): Builder

        fun build(): WebDavComponent

    }

    val webDavApi: WebDavApi

}