package app.editors.manager.storages.onedrive.onedrive.login

import app.editors.manager.storages.onedrive.onedrive.api.OneDriveResponse
import io.reactivex.Single

interface IOneDriveLoginServiceProvider {
    fun getUserInfo(token: String): Single<OneDriveResponse>
    fun getToken(map: Map<String, String>): Single<OneDriveResponse>
}