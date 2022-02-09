package app.editors.manager.storages.onedrive.onedrive.login

import app.editors.manager.storages.onedrive.onedrive.OneDriveResponse
import io.reactivex.Single

interface IOneDriveLoginServiceProvider {
    fun getUserInfo(token: String): Single<OneDriveResponse>
}