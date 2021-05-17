package app.editors.manager.di.component

import app.documents.core.webdav.WebDavApi
import app.documents.core.di.module.WebDavApiModule
import app.documents.core.di.module.WebDavScope
import dagger.Component

@Component(modules = [WebDavApiModule::class], dependencies = [AppComponent::class])
@WebDavScope
interface WebDavComponent {

    fun getWebDavApi(): WebDavApi

}