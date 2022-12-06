package app.editors.manager.mvp.presenters.storages

import app.documents.core.network.storages.dropbox.models.response.TokenResponse
import app.documents.core.network.storages.googledrive.api.GoogleDriveResponse
import app.documents.core.network.storages.googledrive.models.User
import app.documents.core.network.storages.googledrive.models.resonse.UserResponse
import app.documents.core.storage.account.CloudAccount
import app.editors.manager.app.App
import app.editors.manager.app.googleDriveLoginProvider
import app.editors.manager.app.googleDriveProvider
import app.editors.manager.mvp.views.base.BaseStorageSignInView
import lib.toolkit.base.managers.utils.AccountData

class GoogleDriveSignInPresenter : BaseStorageSignInPresenter<BaseStorageSignInView>() {

    init {
        App.getApp().appComponent.inject(this)
    }

    fun getUserInfo(code: String) {
        var token = TokenResponse("", "")
        disposable = context.googleDriveLoginProvider.getToken(code).flatMap { tokenResponse ->
            token = tokenResponse
            context.googleDriveProvider.getUserInfo("Bearer ${token.accessToken}")
        }.subscribe { loginResponse ->
            when (loginResponse) {
                is GoogleDriveResponse.Success -> {
                    createUser(
                        (loginResponse.response as UserResponse).user!!,
                        token.accessToken,
                        token.refreshToken
                    )
                }
                is GoogleDriveResponse.Error -> {
                    throw loginResponse.error
                }
            }
        }
    }

    private fun createUser(user: User, accessToken: String, refreshToken: String) {
        val cloudAccount = CloudAccount(
            id = user.permissionId,
            isWebDav = false,
            isGoogleDrive = true,
            portal = "drive.google.com",
            webDavPath = "",
            avatarUrl = user.photoLink,
            webDavProvider = "",
            login = user.emailAddress,
            scheme = "https://",
            isSslState = networkSettings.getSslState(),
            isSslCiphers = networkSettings.getCipher(),
            name = user.displayName
        )

        val accountData = AccountData(
            portal = cloudAccount.portal ?: "",
            scheme = cloudAccount.scheme ?: "",
            displayName = user.displayName,
            userId = cloudAccount.id,
            provider = cloudAccount.webDavProvider ?: "",
            accessToken = accessToken,
            refreshToken = refreshToken,
            webDav = cloudAccount.webDavPath,
            email = user.emailAddress,
        )

        saveAccount(cloudAccount, accountData, accessToken)
    }

}