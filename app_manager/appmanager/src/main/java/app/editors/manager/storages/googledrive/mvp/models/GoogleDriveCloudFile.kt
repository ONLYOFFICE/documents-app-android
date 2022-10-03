package app.editors.manager.storages.googledrive.mvp.models

import app.editors.manager.mvp.models.explorer.CloudFile

class GoogleDriveCloudFile : CloudFile() {
    var mimeType: String = ""
}