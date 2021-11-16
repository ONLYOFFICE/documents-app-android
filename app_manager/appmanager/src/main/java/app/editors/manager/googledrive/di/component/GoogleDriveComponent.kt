package app.editors.manager.googledrive.di.component

import app.editors.manager.di.component.AppComponent
import app.editors.manager.googledrive.di.module.GoogleDriveModule
import app.editors.manager.googledrive.di.module.GoogleDriveScope
import app.editors.manager.googledrive.googledrive.api.IGoogleDriveServiceProvider
import dagger.Component


@Component(modules = [GoogleDriveModule::class], dependencies = [AppComponent::class])
@GoogleDriveScope
interface GoogleDriveComponent {

    @Component.Builder
    interface Builder {

        fun appComponent(appComponent: AppComponent): Builder

        fun build(): GoogleDriveComponent

    }

    val googleDriveServiceProvider: IGoogleDriveServiceProvider

}