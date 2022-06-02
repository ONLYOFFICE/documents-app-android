package app.editors.manager.storages.dropbox.mvp.presenters

import app.documents.core.account.CloudAccount
import app.editors.manager.BuildConfig
import app.editors.manager.app.App
import app.editors.manager.app.dropboxLoginService
import app.editors.manager.storages.base.presenter.BaseStorageSignInPresenter
import app.editors.manager.storages.base.view.BaseStorageSignInView
import app.editors.manager.storages.dropbox.dropbox.api.DropboxService
import app.editors.manager.storages.dropbox.dropbox.login.DropboxResponse
import app.editors.manager.storages.dropbox.managers.utils.DropboxUtils
import app.editors.manager.storages.dropbox.mvp.models.request.AccountRequest
import app.editors.manager.storages.dropbox.mvp.models.request.TokenType
import app.editors.manager.storages.dropbox.mvp.models.request.TokenRequest
import app.editors.manager.storages.dropbox.mvp.models.response.TokenResponse
import app.editors.manager.storages.dropbox.mvp.models.response.UserResponse
import lib.toolkit.base.managers.utils.AccountData
import okhttp3.Credentials

class DropboxSignInPresenter : BaseStorageSignInPresenter<BaseStorageSignInView>() {

    init {
        App.getApp().appComponent.inject(this)
    }

    fun getUserInfo(token: String) {
        val request = TokenRequest(code = token, TokenType.GET.type)
        val service = App.getApp().dropboxLoginService

        var accessToken = ""
        var refreshToken = ""

        disposable = service.getRefreshToken(
            Credentials.basic(BuildConfig.DROP_BOX_COM_CLIENT_ID, BuildConfig.DROP_BOX_COM_CLIENT_SECRET),
            mapOf(
                TokenRequest::code.name to request.code,
                TokenRequest::grant_type.name to request.grant_type,
                TokenRequest::redirect_uri.name to request.redirect_uri
            )
        ).flatMap { responseToken ->
            when (responseToken) {
                is DropboxResponse.Success -> {
                    val response = responseToken.response as TokenResponse
                    accessToken = response.accessToken
                    refreshToken = response.refreshToken

                    service.getUserInfo("Bearer ${response.accessToken}", AccountRequest(response.accountId))
                }
                is DropboxResponse.Error -> {
                    throw responseToken.error
                }
            }
        }.subscribe({ response ->
            when(response) {
                    is DropboxResponse.Success -> {
                        createUser(response.response as UserResponse, accessToken, refreshToken)
                    }
                    is DropboxResponse.Error -> {
                        throw response.error
                    }
                }
        }) { error ->
            fetchError(error)
        }

    }

    private fun createUser(user: UserResponse, accessToken: String, refreshToken: String) {
        networkSettings.setBaseUrl(DropboxService.DROPBOX_BASE_URL)
        val cloudAccount = CloudAccount(
            id = user.account_id,
            isWebDav = false,
            isOneDrive = false,
            isDropbox = true,
            portal = DropboxUtils.DROPBOX_PORTAL,
            webDavPath = "",
            webDavProvider = "",
            login = user.email,
            scheme = "https://",
            isSslState = networkSettings.getSslState(),
            isSslCiphers = networkSettings.getCipher(),
            name = user.name?.display_name,
            avatarUrl = user.profile_photo_url,
            refreshToken = refreshToken
        )

        val accountData = AccountData(
            portal = cloudAccount.portal ?: "",
            scheme = cloudAccount.scheme ?: "",
            displayName = user.name?.display_name!!,
            userId = cloudAccount.id,
            provider = cloudAccount.webDavProvider ?: "",
            accessToken = accessToken,
            webDav = cloudAccount.webDavPath,
            email = user.email,
        )

        saveAccount(cloudAccount = cloudAccount, accountData = accountData, accessToken = accessToken)
    }

}