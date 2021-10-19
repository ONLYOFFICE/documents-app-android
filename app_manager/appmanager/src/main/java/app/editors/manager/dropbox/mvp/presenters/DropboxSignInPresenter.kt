package app.editors.manager.dropbox.mvp.presenters

import android.accounts.Account
import app.documents.core.account.CloudAccount
import app.editors.manager.app.App
import app.editors.manager.app.dropboxLoginService
import app.editors.manager.dropbox.dropbox.login.DropboxResponse
import app.editors.manager.dropbox.managers.utils.DropboxUtils
import app.editors.manager.dropbox.mvp.models.request.AccountRequest
import app.editors.manager.dropbox.mvp.models.response.UserResponse
import app.editors.manager.dropbox.mvp.views.DropboxSignInView
import app.editors.manager.mvp.presenters.base.BasePresenter
import app.editors.manager.onedrive.managers.utils.OneDriveUtils
import app.editors.manager.onedrive.mvp.models.user.User
import app.editors.manager.onedrive.onedrive.OneDriveService
import io.reactivex.disposables.Disposable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import lib.toolkit.base.R
import lib.toolkit.base.managers.utils.AccountData
import lib.toolkit.base.managers.utils.AccountUtils

class DropboxSignInPresenter: BasePresenter<DropboxSignInView>() {

    init {
        App.getApp().appComponent.inject(this)
    }

    var disposable: Disposable? = null

    override fun onDestroy() {
        super.onDestroy()
        disposable?.dispose()
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
        networkSettings.setBaseUrl(OneDriveService.ONEDRIVE_BASE_URL)
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

        val account = Account(cloudAccount.getAccountName(), context.getString(R.string.account_type))

        if (AccountUtils.addAccount(context, account, "", accountData)) {
            addAccountToDb(cloudAccount)
        } else {
            AccountUtils.setAccountData(context, account, accountData)
            AccountUtils.setPassword(context, account, accessToken)
            addAccountToDb(cloudAccount)
        }
        AccountUtils.setToken(context, account, accessToken)
    }

    private fun addAccountToDb(cloudAccount: CloudAccount) {
        CoroutineScope(Dispatchers.Default).launch {
            accountDao.getAccountOnline()?.let {
                accountDao.addAccount(it.copy(isOnline = false))
            }
            accountDao.addAccount(cloudAccount.copy(isOnline = true))
            withContext(Dispatchers.Main) {
                viewState.onLogin()
            }
        }
    }

}