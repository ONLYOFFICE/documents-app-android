package app.editors.manager.di.component

import app.editors.manager.app.Api
import app.editors.manager.app.RoomApi
import app.editors.manager.di.module.ApiModule
import app.editors.manager.di.module.ApiScope
import dagger.Component

@Component(modules = [ApiModule::class], dependencies = [AppComponent::class])
@ApiScope
interface ApiComponent {

    @Component.Builder
    interface Builder {

        fun appComponent(appComponent: AppComponent): Builder

        fun build(): ApiComponent

    }

    val api: Api
    val roomApi: RoomApi
}