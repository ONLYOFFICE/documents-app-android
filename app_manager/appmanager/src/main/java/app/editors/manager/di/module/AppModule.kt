package app.editors.manager.di.module

import app.documents.core.di.dagger.AccountModule
import app.documents.core.di.dagger.LoginModule
import app.documents.core.di.dagger.RecentModule
import app.documents.core.di.dagger.SettingsModule
import app.editors.manager.storages.dropbox.di.module.DropboxLoginModule
import app.editors.manager.storages.googledrive.di.module.GoogleDriveLoginModule
import app.editors.manager.storages.onedrive.di.module.OneDriveLoginModule
import dagger.Module

@Module(includes = [ToolModule::class, SettingsModule::class, AccountModule::class, RecentModule::class, LoginModule::class, OneDriveLoginModule::class, DropboxLoginModule::class, GoogleDriveLoginModule::class])
class AppModule