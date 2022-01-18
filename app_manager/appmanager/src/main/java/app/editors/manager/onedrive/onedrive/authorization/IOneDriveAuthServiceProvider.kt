package app.editors.manager.onedrive.onedrive.authorization

import app.editors.manager.onedrive.onedrive.OneDriveResponse
import io.reactivex.Single

interface IOneDriveAuthServiceProvider {
    fun getToken(map: Map<String, String>): Single<OneDriveResponse>
}