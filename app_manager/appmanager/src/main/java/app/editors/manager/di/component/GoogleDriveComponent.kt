package app.editors.manager.di.component

import app.documents.core.di.dagger.storages.GoogleDriveLoginModule
import app.documents.core.di.dagger.storages.GoogleDriveModule
import app.documents.core.di.dagger.storages.GoogleDriveScope
import app.documents.core.network.storages.googledrive.api.GoogleDriveProvider
import app.documents.core.network.storages.googledrive.login.GoogleDriveLoginProvider
import dagger.Component


@Component(modules = [GoogleDriveModule::class, GoogleDriveLoginModule::class], dependencies = [AppComponent::class])
@GoogleDriveScope
interface GoogleDriveComponent {

    @Component.Builder
    interface Builder {

        fun appComponent(appComponent: AppComponent): Builder

        fun build(): GoogleDriveComponent

    }

    val googleDriveLoginProvider: GoogleDriveLoginProvider
    val googleDriveProvider: GoogleDriveProvider
}