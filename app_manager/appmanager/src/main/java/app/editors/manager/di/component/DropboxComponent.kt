package app.editors.manager.di.component

import app.documents.core.di.dagger.CoreComponent
import app.documents.core.di.dagger.storages.DropboxModule
import app.documents.core.di.dagger.storages.DropboxScope
import app.documents.core.network.storages.dropbox.api.DropboxProvider
import dagger.Component


@Component(
    modules = [DropboxModule::class],
    dependencies = [AppComponent::class, CoreComponent::class]
)
@DropboxScope
interface DropboxComponent {

    @Component.Builder
    interface Builder {

        fun appComponent(appComponent: AppComponent): Builder

        fun coreComponent(coreComponent: CoreComponent): Builder

        fun build(): DropboxComponent

    }

    val dropboxProvider: DropboxProvider
}