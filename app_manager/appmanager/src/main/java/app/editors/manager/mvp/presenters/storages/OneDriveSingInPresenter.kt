package app.editors.manager.mvp.presenters.storages

import app.documents.core.storage.account.CloudAccount
import app.editors.manager.app.App
import app.editors.manager.app.oneDriveLoginProvider
import app.editors.manager.mvp.views.base.BaseStorageSignInView
import app.documents.core.network.common.utils.OneDriveUtils
import app.documents.core.network.storages.onedrive.api.OneDriveResponse
import app.documents.core.network.storages.onedrive.models.response.AuthResponse
import app.documents.core.network.storages.onedrive.models.user.User
import app.editors.manager.app.oneDriveProvider
import lib.toolkit.base.managers.utils.AccountData
import moxy.InjectViewState

@InjectViewState
class OneDriveSingInPresenter : BaseStorageSignInPresenter<BaseStorageSignInView>() {

    init {
        App.getApp().appComponent.inject(this)
    }

    fun getToken(code: String) {
        var accessToken = ""
        var refreshToken = ""
        disposable = context.oneDriveLoginProvider.getToken(code)
            .map { oneDriveResponse ->
                viewState.onStartLogin()
                when (oneDriveResponse) {
                    is OneDriveResponse.Success -> {
                        val response = oneDriveResponse.response as AuthResponse
                        accessToken = response.access_token
                        refreshToken = response.refresh_token
                        return@map response
                    }
                    is OneDriveResponse.Error -> {
                        throw oneDriveResponse.error
                    }
                }
            }.flatMap { response -> context.oneDriveProvider.getUserInfo(response.access_token) }
            .subscribe ({ oneDriveResponse ->
                when (oneDriveResponse) {
                    is OneDriveResponse.Success -> {
                        createUser((oneDriveResponse.response as User), accessToken, refreshToken)
                        App.getApp().refreshOneDriveInstance()
                    }
                    is OneDriveResponse.Error -> {
                        throw oneDriveResponse.error
                    }
                }
            }, ::fetchError)
    }


    private fun createUser(user: User, accessToken: String, refreshToken: String) {
        val cloudAccount = CloudAccount(
            id = user.userPrincipalName,
            isWebDav = false,
            isOneDrive = true,
            portal = OneDriveUtils.ONEDRIVE_PORTAL,
            webDavPath = "",
            webDavProvider = "",
            login = user.userPrincipalName,
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
            email = user.userPrincipalName,
        )

        saveAccount(cloudAccount, accountData, accessToken)
    }
}