package app.editors.manager.dropbox.di.component

import app.editors.manager.di.component.AppComponent
import app.editors.manager.dropbox.di.module.DropboxModule
import app.editors.manager.dropbox.di.module.DropboxScope
import app.editors.manager.dropbox.dropbox.api.IDropboxServiceProvider
import dagger.Component


@Component(modules = [DropboxModule::class], dependencies = [AppComponent::class])
@DropboxScope
interface DropboxComponent {

    @Component.Builder
    interface Builder {

        fun appComponent(appComponent: AppComponent): Builder

        fun build(): DropboxComponent

    }

    val dropboxServiceProvider: IDropboxServiceProvider
}