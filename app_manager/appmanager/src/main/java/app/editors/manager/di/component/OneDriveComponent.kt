package app.editors.manager.di.component

import app.documents.core.di.dagger.storages.OneDriveLoginModule
import app.documents.core.di.dagger.storages.OneDriveModule
import app.documents.core.di.dagger.storages.OneDriveScope
import app.documents.core.network.storages.onedrive.api.OneDriveProvider
import app.documents.core.network.storages.onedrive.login.OneDriveLoginProvider
import dagger.Component


@Component(modules = [OneDriveModule::class, OneDriveLoginModule::class], dependencies = [AppComponent::class])
@OneDriveScope
interface OneDriveComponent {

    @Component.Builder
    interface Builder {

        fun appComponent(appComponent: AppComponent): Builder

        fun build(): OneDriveComponent

    }

    val oneDriveLoginProvider: OneDriveLoginProvider
    val oneDriveProvider: OneDriveProvider
}