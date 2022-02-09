package app.editors.manager.storages.onedrive.onedrive.authorization

import app.editors.manager.storages.onedrive.onedrive.OneDriveResponse
import io.reactivex.Single

interface IOneDriveAuthServiceProvider {
    fun getToken(map: Map<String, String>): Single<OneDriveResponse>
}