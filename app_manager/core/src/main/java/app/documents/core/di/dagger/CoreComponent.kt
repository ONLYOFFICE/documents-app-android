package app.documents.core.di.dagger

import android.content.Context
import app.documents.core.database.datasource.CloudDataSource
import app.documents.core.database.datasource.RecentDataSource
import app.documents.core.database.migration.MigrationHelper
import app.documents.core.login.LoginComponent
import app.documents.core.network.manager.ManagerService
import app.documents.core.network.room.RoomService
import app.documents.core.network.share.ShareService
import app.documents.core.network.webdav.WebDavService
import app.documents.core.providers.CloudFileProvider
import app.documents.core.providers.LocalFileProvider
import app.documents.core.providers.RoomProvider
import app.documents.core.providers.WebDavFileProvider
import dagger.BindsInstance
import dagger.Component

@Component(modules = [CoreModule::class])
interface CoreComponent {

    @Component.Builder
    interface Builder {

        @BindsInstance
        fun context(context: Context): Builder

        fun build(): CoreComponent
    }

    fun loginComponent(): LoginComponent.Factory

    val cloudDataSource: CloudDataSource
    val recentDataSource: RecentDataSource
    val migrationHelper: MigrationHelper

    val shareService: ShareService
    val managerService: ManagerService
    val webDavService: WebDavService
    val roomService: RoomService

    val cloudFileProvider: CloudFileProvider
    val localFileProvider: LocalFileProvider
    val roomProvider: RoomProvider
    val webDavFileProvider: WebDavFileProvider
}