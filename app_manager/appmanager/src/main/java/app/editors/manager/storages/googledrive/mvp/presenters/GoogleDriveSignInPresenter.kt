package app.editors.manager.storages.googledrive.mvp.presenters

import app.documents.core.account.CloudAccount
import app.editors.manager.BuildConfig
import app.editors.manager.app.App
import app.editors.manager.app.getGoogleDriveServiceProvider
import app.editors.manager.app.googleDriveLoginService
import app.editors.manager.managers.utils.StorageUtils
import app.editors.manager.storages.base.presenter.BaseStorageSignInPresenter
import app.editors.manager.storages.base.view.BaseStorageSignInView
import app.editors.manager.storages.googledrive.googledrive.login.GoogleDriveResponse
import app.editors.manager.storages.googledrive.googledrive.login.TokenResponse
import app.editors.manager.storages.googledrive.mvp.models.User
import app.editors.manager.storages.googledrive.mvp.models.resonse.UserResponse
import lib.toolkit.base.managers.utils.AccountData

class GoogleDriveSignInPresenter : BaseStorageSignInPresenter<BaseStorageSignInView>() {

    init {
        App.getApp().appComponent.inject(this)
    }

    fun getUserInfo(code: String) {
        var token = TokenResponse("", "")
        val map = mapOf(
            StorageUtils.ARG_CODE to code,
            StorageUtils.ARG_CLIENT_ID to BuildConfig.GOOGLE_COM_CLIENT_ID,
            StorageUtils.ARG_CLIENT_SECRET to BuildConfig.GOOGLE_COM_CLIENT_SECRET,
            StorageUtils.ARG_REDIRECT_URI to BuildConfig.GOOGLE_COM_REDIRECT_URL,
            StorageUtils.ARG_GRANT_TYPE to StorageUtils.Google.VALUE_GRANT_TYPE_AUTH,
        )

        disposable = App.getApp().googleDriveLoginService.getToken(map).flatMap { tokenResponse ->
            token = tokenResponse
            App.getApp().getGoogleDriveServiceProvider().getUserInfo("Bearer ${token.accessToken}")
        }.subscribe { loginResponse ->
            when (loginResponse) {
                is GoogleDriveResponse.Success -> {
                    createUser(
                        (loginResponse.response as UserResponse).user!!,
                        token.accessToken,
                        token.refreshToken.orEmpty()
                    )
                }
                is GoogleDriveResponse.Error -> {
                    throw loginResponse.error
                }
            }
        }
    }

    private fun createUser(user: User, accessToken: String, refreshToken: String) {
        //networkSettings.setBaseUrl("https://www.googleapis.com/")
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