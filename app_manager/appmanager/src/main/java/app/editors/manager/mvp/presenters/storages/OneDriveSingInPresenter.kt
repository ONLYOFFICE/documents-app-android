package app.editors.manager.mvp.presenters.storages

import app.documents.core.model.cloud.CloudAccount
import app.documents.core.model.cloud.CloudPortal
import app.documents.core.model.cloud.PortalProvider
import app.documents.core.network.common.utils.OneDriveUtils
import app.documents.core.network.storages.onedrive.api.OneDriveResponse
import app.documents.core.network.storages.onedrive.models.response.AuthResponse
import app.documents.core.network.storages.onedrive.models.user.User
import app.editors.manager.app.App
import app.editors.manager.app.oneDriveLoginProvider
import app.editors.manager.app.oneDriveProvider
import app.editors.manager.mvp.views.base.BaseStorageSignInView
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
            .subscribe({ oneDriveResponse ->
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
            id = user.id,
            login = user.userPrincipalName,
            name = user.displayName,
            portal = CloudPortal(
                url = OneDriveUtils.ONEDRIVE_PORTAL,
                provider = PortalProvider.OneDrive,
//                settings = PortalSettings(
//                    isSslState = networkSettings.getSslState(),
//                    isSslCiphers = networkSettings.getCipher()
//                )
            )
        )

        val accountData = AccountData(
            portal = cloudAccount.portal.url,
            scheme = cloudAccount.portal.scheme.value,
            displayName = user.displayName,
            userId = cloudAccount.id,
            refreshToken = refreshToken,
            email = user.userPrincipalName,
        )

        saveAccount(cloudAccount, accountData, accessToken)
    }
}