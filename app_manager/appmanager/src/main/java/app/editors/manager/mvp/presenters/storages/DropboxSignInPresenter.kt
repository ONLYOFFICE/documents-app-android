package app.editors.manager.mvp.presenters.storages

import app.documents.core.model.cloud.CloudAccount
import app.documents.core.model.cloud.CloudPortal
import app.documents.core.model.cloud.PortalProvider
import app.documents.core.model.cloud.PortalSettings
import app.documents.core.model.cloud.Scheme
import app.documents.core.model.login.response.TokenResponse
import app.documents.core.network.common.utils.DropboxUtils
import app.documents.core.network.storages.dropbox.login.DropboxLoginProvider
import app.documents.core.network.storages.dropbox.models.response.UserResponse
import app.editors.manager.app.App
import app.editors.manager.mvp.views.base.BaseStorageSignInView
import kotlinx.coroutines.launch
import lib.toolkit.base.managers.utils.AccountData
import moxy.presenterScope
import javax.inject.Inject


// TODO: add repository
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
            portal = CloudPortal(
                url = DropboxUtils.DROPBOX_PORTAL,
                provider = PortalProvider.DropBox,
                scheme = Scheme.Https,
                settings = PortalSettings(
//                    isSslState = networkSettings.getSslState(),
//                    isSslCiphers = networkSettings.getCipher()
                )
            ),
            login = userResponse.email.orEmpty(),
            name = userResponse.name?.displayName.orEmpty(),
            avatarUrl = userResponse.profilePhotoUrl.orEmpty(),
        )

        val accountData = AccountData(
            portal = cloudAccount.portal.url,
            scheme = cloudAccount.portal.scheme.value,
            displayName = userResponse.name?.displayName.orEmpty(),
            userId = cloudAccount.id,
            email = userResponse.email.orEmpty(),
            refreshToken = tokenResponse.refreshToken
        )

        saveAccount(cloudAccount = cloudAccount, accountData = accountData, accessToken = tokenResponse.accessToken)
    }

}