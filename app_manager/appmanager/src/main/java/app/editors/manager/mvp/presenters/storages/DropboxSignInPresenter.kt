package app.editors.manager.mvp.presenters.storages

import app.documents.core.network.common.utils.DropboxUtils
import app.documents.core.network.storages.dropbox.login.DropboxLoginProvider
import app.documents.core.network.storages.dropbox.models.response.TokenResponse
import app.documents.core.network.storages.dropbox.models.response.UserResponse
import app.documents.core.storage.account.CloudAccount
import app.editors.manager.app.App
import app.editors.manager.mvp.views.base.BaseStorageSignInView
import kotlinx.coroutines.launch
import lib.toolkit.base.managers.utils.AccountData
import moxy.presenterScope
import javax.inject.Inject


class DropboxSignInPresenter : BaseStorageSignInPresenter<BaseStorageSignInView>() {

    @Inject
    lateinit var dropboxLoginProvider: DropboxLoginProvider

    init {
        App.getApp().dropboxComponent.inject(this)
    }

    fun authUser(code: String) {
        presenterScope.launch {
            val tokenResponse = dropboxLoginProvider.getAccessToken(code)
            val accountResponse = dropboxLoginProvider.getUserInfo(tokenResponse.accessToken)
            createUser(tokenResponse, accountResponse)
        }
    }

    private fun createUser(tokenResponse: TokenResponse, userResponse: UserResponse) {
        val cloudAccount = CloudAccount(
            id = tokenResponse.accountId,
            isWebDav = false,
            isOneDrive = false,
            isDropbox = true,
            portal = DropboxUtils.DROPBOX_PORTAL,
            webDavPath = "",
            webDavProvider = "",
            login = userResponse.email,
            scheme = "https://",
            isSslState = networkSettings.getSslState(),
            isSslCiphers = networkSettings.getCipher(),
            name = userResponse.name?.displayName,
            avatarUrl = userResponse.profilePhotoUrl,
            refreshToken = tokenResponse.refreshToken
        )

        val accountData = AccountData(
            portal = cloudAccount.portal.orEmpty(),
            scheme = cloudAccount.scheme.orEmpty(),
            displayName = userResponse.name?.displayName.orEmpty(),
            userId = cloudAccount.id,
            provider = cloudAccount.webDavProvider.orEmpty(),
            accessToken = tokenResponse.accessToken,
            webDav = cloudAccount.webDavPath,
            email = userResponse.email.orEmpty(),
            refreshToken = tokenResponse.refreshToken
        )

        saveAccount(cloudAccount = cloudAccount, accountData = accountData, accessToken = tokenResponse.accessToken)
    }

}