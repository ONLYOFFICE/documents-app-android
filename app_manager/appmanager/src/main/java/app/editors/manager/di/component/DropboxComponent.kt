package app.editors.manager.di.component

import app.documents.core.di.dagger.storages.DropboxLoginModule
import app.documents.core.di.dagger.storages.DropboxModule
import app.documents.core.di.dagger.storages.DropboxScope
import app.documents.core.network.storages.dropbox.api.DropboxProvider
import app.documents.core.network.storages.dropbox.login.DropboxLoginProvider
import dagger.Component


@Component(modules = [DropboxModule::class, DropboxLoginModule::class], dependencies = [AppComponent::class])
@DropboxScope
interface DropboxComponent {

    @Component.Builder
    interface Builder {

        fun appComponent(appComponent: AppComponent): Builder

        fun build(): DropboxComponent

    }

    val dropboxLoginProvider: DropboxLoginProvider
    val dropboxProvider: DropboxProvider
}