package app.editors.manager.storages.googledrive.mvp.models

import app.documents.core.network.manager.models.explorer.CloudFile

class GoogleDriveCloudFile : CloudFile() {
    var mimeType: String = ""
}