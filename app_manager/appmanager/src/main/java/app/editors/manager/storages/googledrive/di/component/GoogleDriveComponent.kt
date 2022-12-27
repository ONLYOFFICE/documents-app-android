package app.editors.manager.storages.googledrive.di.component

import app.editors.manager.di.component.AppComponent
import app.editors.manager.storages.googledrive.di.module.GoogleDriveModule
import app.editors.manager.storages.googledrive.di.module.GoogleDriveScope
import app.editors.manager.storages.googledrive.googledrive.api.IGoogleDriveServiceProvider
import dagger.BindsInstance
import dagger.Component
import javax.inject.Named


@Component(modules = [GoogleDriveModule::class], dependencies = [AppComponent::class])
@GoogleDriveScope
interface GoogleDriveComponent {

    @Component.Builder
    interface Builder {

        fun appComponent(appComponent: AppComponent): Builder

        @BindsInstance
        fun token(@Named("userInfo") token: String = ""): Builder

        fun build(): GoogleDriveComponent

    }

    val googleDriveServiceProvider: IGoogleDriveServiceProvider

}