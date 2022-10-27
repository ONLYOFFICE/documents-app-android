package app.editors.manager.storages.onedrive.mvp.presenters

import app.documents.core.account.CloudAccount
import app.editors.manager.BuildConfig
import app.editors.manager.app.App
import app.editors.manager.app.oneDriveLoginService
import app.editors.manager.managers.utils.StorageUtils
import app.editors.manager.storages.base.presenter.BaseStorageSignInPresenter
import app.editors.manager.storages.base.view.BaseStorageSignInView
import app.editors.manager.storages.onedrive.managers.utils.OneDriveUtils
import app.editors.manager.storages.onedrive.mvp.models.response.AuthResponse
import app.editors.manager.storages.onedrive.mvp.models.user.User
import app.editors.manager.storages.onedrive.onedrive.api.OneDriveResponse
import app.editors.manager.storages.onedrive.onedrive.api.OneDriveService
import lib.toolkit.base.managers.utils.AccountData
import moxy.InjectViewState

@InjectViewState
class OneDriveSingInPresenter : BaseStorageSignInPresenter<BaseStorageSignInView>() {

    init {
        App.getApp().appComponent.inject(this)
    }

    fun getToken(code: String) {
        val map = mapOf(
            StorageUtils.ARG_CLIENT_ID to BuildConfig.ONE_DRIVE_COM_CLIENT_ID,
            StorageUtils.ARG_SCOPE to StorageUtils.OneDrive.VALUE_SCOPE,
            StorageUtils.ARG_REDIRECT_URI to BuildConfig.ONE_DRIVE_COM_REDIRECT_URL,
            StorageUtils.ARG_GRANT_TYPE to StorageUtils.OneDrive.VALUE_GRANT_TYPE_AUTH,
            StorageUtils.ARG_CLIENT_SECRET to BuildConfig.ONE_DRIVE_COM_CLIENT_SECRET,
            StorageUtils.ARG_CODE to code
        )
        var accessToken = ""
        var refreshToken = ""
        networkSettings.setBaseUrl(OneDriveService.ONEDRIVE_AUTH_URL)
        disposable = App.getApp().oneDriveLoginService.getToken(map)
            .map { oneDriveResponse ->
                viewState.onStartLogin()
                when(oneDriveResponse) {
                    is OneDriveResponse.Success -> {
                        accessToken = (oneDriveResponse.response as AuthResponse).access_token
                        refreshToken = oneDriveResponse.response.refresh_token
                        networkSettings.setBaseUrl(OneDriveService.ONEDRIVE_BASE_URL)
                        return@map oneDriveResponse.response
                    }
                    is OneDriveResponse.Error -> {
                        throw oneDriveResponse.error
                    }
                }
            }.flatMap {accessToken -> App.getApp().oneDriveLoginService.getUserInfo((accessToken).access_token) }
            .subscribe { oneDriveResponse ->
                when(oneDriveResponse) {
                    is OneDriveResponse.Success -> {
                        createUser((oneDriveResponse.response as User), accessToken, refreshToken)
                    }
                    is OneDriveResponse.Error -> {
                        viewState.onError(oneDriveResponse.error.message)
                    }
                }
            }
    }


    private fun createUser(user: User, accessToken: String, refreshToken: String) {
        networkSettings.setBaseUrl(OneDriveService.ONEDRIVE_BASE_URL)
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