package app.documents.core.network.storages.googledrive.models

import app.documents.core.network.manager.models.explorer.CloudFile

class GoogleDriveCloudFile : CloudFile() {
    var mimeType: String = ""
}