package app.editors.manager.di.component

import app.documents.core.share.ShareService
import app.editors.manager.di.module.ShareModule
import app.editors.manager.di.module.ShareScope
import dagger.Component

@Component(modules = [ShareModule::class], dependencies = [AppComponent::class])
@ShareScope
interface ShareComponent {

    @Component.Builder
    interface Builder {

        fun appComponent(appComponent: AppComponent): Builder

        fun build(): ShareComponent

    }

    val shareService: ShareService

}