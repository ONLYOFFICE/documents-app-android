package app.editors.manager.managers.providers

import app.documents.core.network.storages.onedrive.api.OneDriveProvider
import app.editors.manager.app.App
import app.editors.manager.app.oneDriveProvider
import app.editors.manager.managers.works.onedrive.UploadWork

class OneDriveStorageHelper : StorageHelper<OneDriveProvider>(UploadWork::class.java) {

    override val api: OneDriveProvider get() = App.getApp().oneDriveProvider
}