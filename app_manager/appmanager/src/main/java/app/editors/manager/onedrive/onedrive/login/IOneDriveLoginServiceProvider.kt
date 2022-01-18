package app.editors.manager.onedrive.onedrive.login

import app.editors.manager.onedrive.onedrive.OneDriveResponse
import io.reactivex.Single

interface IOneDriveLoginServiceProvider {
    fun getUserInfo(token: String): Single<OneDriveResponse>
}