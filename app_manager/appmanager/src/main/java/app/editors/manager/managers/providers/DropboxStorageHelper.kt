package app.editors.manager.managers.providers

import app.documents.core.network.storages.dropbox.api.DropboxProvider
import app.editors.manager.app.App
import app.editors.manager.app.dropboxProvider
import app.editors.manager.managers.works.dropbox.UploadWork
import javax.inject.Inject

class DropboxStorageHelper @Inject constructor() :
    StorageHelper<DropboxProvider>(UploadWork::class.java) {

    override val api: DropboxProvider get() = App.getApp().dropboxProvider
}