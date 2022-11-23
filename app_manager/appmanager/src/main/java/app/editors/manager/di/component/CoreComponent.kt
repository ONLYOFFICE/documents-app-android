package app.editors.manager.di.component

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
import app.editors.manager.storages.dropbox.di.module.DropboxLoginModule
import app.editors.manager.storages.dropbox.di.module.DropboxModule
import app.editors.manager.storages.dropbox.dropbox.login.IDropboxLoginServiceProvider
import app.editors.manager.storages.googledrive.di.module.GoogleDriveLoginModule
import app.editors.manager.storages.googledrive.googledrive.login.IGoogleDriveLoginServiceProvider
import app.editors.manager.storages.onedrive.di.module.OneDriveLoginModule
import app.editors.manager.storages.onedrive.onedrive.login.IOneDriveLoginServiceProvider
import app.editors.manager.storages.onedrive.onedrive.login.OneDriveLoginService
import dagger.Component

@Component(modules = [ShareModule::class], dependencies = [AppComponent::class])
@ApiScope
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
    val oneDriveLoginService: IOneDriveLoginServiceProvider
    val dropboxLoginService: IDropboxLoginServiceProvider
    val googleDriveLoginService: IGoogleDriveLoginServiceProvider
}