package app.editors.manager.di.component

import app.editors.manager.app.Api
import app.editors.manager.di.module.ApiModule
import app.editors.manager.di.module.ApiScope
import dagger.Component

@Component(modules = [ApiModule::class], dependencies = [AppComponent::class])
@ApiScope
interface ApiComponent {

    fun getApi(): Api

}