package app.editors.manager.di.component

import android.content.Context
import app.documents.core.di.dagger.CoreModule
import app.documents.core.di.dagger.CoreScope
import app.documents.core.network.login.ILoginServiceProvider
import app.documents.core.network.manager.ManagerService
import app.documents.core.network.room.RoomService
import app.documents.core.network.share.ShareService
import app.documents.core.network.webdav.WebDavService
import app.documents.core.providers.CloudFileProvider
import app.documents.core.providers.LocalFileProvider
import app.documents.core.providers.RoomProvider
import app.documents.core.providers.WebDavFileProvider
import dagger.Component

@CoreScope
@Component(modules = [CoreModule::class], dependencies = [AppComponent::class])
interface CoreComponent {

    @Component.Builder
    interface Builder {

        fun appComponent(appComponent: AppComponent): Builder

        fun build(): CoreComponent

    }

    val shareService: ShareService
    val managerService: ManagerService
    val webDavService: WebDavService
    val roomService: RoomService

    val cloudFileProvider: CloudFileProvider
    val localFileProvider: LocalFileProvider
    val roomProvider: RoomProvider
    val webDavFileProvider: WebDavFileProvider
    val loginService: ILoginServiceProvider

}