package app.editors.manager.managers.providers

import app.documents.core.network.storages.googledrive.api.GoogleDriveProvider
import app.editors.manager.app.App
import app.editors.manager.app.googleDriveProvider
import app.editors.manager.managers.works.googledrive.UploadWork

class GoogleDriveStorageHelper : StorageHelper<GoogleDriveProvider>(UploadWork::class.java) {

    override val api: GoogleDriveProvider get() = App.getApp().googleDriveProvider
}