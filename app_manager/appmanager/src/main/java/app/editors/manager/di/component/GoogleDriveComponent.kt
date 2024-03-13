package app.editors.manager.di.component

import app.documents.core.di.dagger.storages.GoogleDriveModule
import app.documents.core.di.dagger.storages.GoogleDriveScope
import app.documents.core.network.storages.googledrive.api.GoogleDriveProvider
import dagger.Component


@Component(modules = [GoogleDriveModule::class], dependencies = [AppComponent::class])
@GoogleDriveScope
interface GoogleDriveComponent {

    @Component.Builder
    interface Builder {

        fun appComponent(appComponent: AppComponent): Builder

        fun build(): GoogleDriveComponent

    }

    val googleDriveProvider: GoogleDriveProvider
}