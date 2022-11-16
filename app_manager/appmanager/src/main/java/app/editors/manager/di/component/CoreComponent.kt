package app.editors.manager.di.component

import app.documents.core.di.dagger.ShareModule
import app.documents.core.repositories.ShareRepository
import app.editors.manager.di.module.ApiScope
import dagger.Component

@Component(modules = [ShareModule::class], dependencies = [AppComponent::class])
@ApiScope
interface CoreComponent {

    @Component.Builder
    interface Builder {

        fun appComponent(appComponent: AppComponent): Builder

        fun build(): CoreComponent

    }

    val shareRepository: ShareRepository
}