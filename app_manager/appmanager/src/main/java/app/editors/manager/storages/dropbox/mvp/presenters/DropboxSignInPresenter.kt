package app.editors.manager.storages.dropbox.mvp.presenters

import android.accounts.Account
import app.documents.core.account.CloudAccount
import app.editors.manager.app.App
import app.editors.manager.app.dropboxLoginService
import app.editors.manager.storages.dropbox.dropbox.api.DropboxService
import app.editors.manager.storages.dropbox.dropbox.login.DropboxResponse
import app.editors.manager.storages.dropbox.managers.utils.DropboxUtils
import app.editors.manager.storages.dropbox.mvp.models.request.AccountRequest
import app.editors.manager.storages.dropbox.mvp.models.response.UserResponse
import app.editors.manager.storages.dropbox.mvp.views.DropboxSignInView
import app.editors.manager.mvp.presenters.base.BasePresenter
import app.editors.manager.storages.base.fragment.BaseStorageSignInFragment
import app.editors.manager.storages.base.presenter.BaseStorageSignInPresenter
import app.editors.manager.storages.base.view.BaseStorageSignInView
import io.reactivex.disposables.Disposable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import lib.toolkit.base.R
import lib.toolkit.base.managers.utils.AccountData
import lib.toolkit.base.managers.utils.AccountUtils

class DropboxSignInPresenter: BaseStorageSignInPresenter<BaseStorageSignInView>() {

    init {
        App.getApp().appComponent.inject(this)
    }

    fun getUserInfo(token: String, uid: String) {
        val accountRequest = AccountRequest(account_id = uid.replace("%3A", ":"))
        disposable = App.getApp().dropboxLoginService.getUserInfo("Bearer $token", accountRequest)
            .subscribe { response ->
                when(response) {
                    is DropboxResponse.Success -> {
                        createUser(response.response as UserResponse, token)
                    }
                    is DropboxResponse.Error -> {
                        throw response.error
                    }
                }
            }
    }

    private fun createUser(user: UserResponse, accessToken: String) {
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
            avatarUrl = user.profile_photo_url
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