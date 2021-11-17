package app.editors.manager.googledrive.googledrive.api

import app.editors.manager.googledrive.googledrive.login.GoogleDriveResponse
import io.reactivex.Single

interface IGoogleDriveServiceProvider {

    fun getFiles(map: Map<String, String>): Single<GoogleDriveResponse>

}