package app.editors.manager.googledrive.mvp.presenters

import android.accounts.Account
import android.util.Log
import app.documents.core.account.CloudAccount
import app.editors.manager.app.App
import app.editors.manager.app.googleDriveLoginService
import app.editors.manager.googledrive.googledrive.login.GoogleDriveResponse
import app.editors.manager.googledrive.mvp.models.User
import app.editors.manager.googledrive.mvp.models.resonse.UserResponse
import app.editors.manager.googledrive.mvp.views.GoogleDriveSignInView
import app.editors.manager.managers.utils.Constants
import app.editors.manager.managers.utils.StorageUtils
import app.editors.manager.mvp.presenters.base.BasePresenter
import app.editors.manager.onedrive.managers.utils.OneDriveUtils
import app.editors.manager.onedrive.onedrive.OneDriveService
import io.reactivex.disposables.Disposable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import lib.toolkit.base.R
import lib.toolkit.base.managers.utils.AccountData
import lib.toolkit.base.managers.utils.AccountUtils

class GoogleDriveSignInPresenter: BasePresenter<GoogleDriveSignInView>() {

    init {
        App.getApp().appComponent.inject(this)
    }

    var disposable: Disposable? = null

    override fun onDestroy() {
        super.onDestroy()
        disposable?.dispose()
    }

    fun getUserInfo(code: String) {
        val map = mapOf(
            StorageUtils.ARG_CODE to code,
            StorageUtils.ARG_CLIENT_ID to Constants.Google.COM_CLIENT_ID,
            StorageUtils.OneDrive.ARG_CLIENT_SECRET to Constants.Google.COM_CLIENT_SECRET,
            StorageUtils.ARG_REDIRECT_URI to Constants.Google.COM_REDIRECT_URL,
            StorageUtils.OneDrive.ARG_GRANT_TYPE to StorageUtils.OneDrive.VALUE_GRANT_TYPE_AUTH,
        )
        disposable = App.getApp().googleDriveLoginService.getUserInfo("Bearer $code")
            .subscribe { response ->
                when(response) {
                    is GoogleDriveResponse.Success -> {
                        createUser((response.response as UserResponse).user!!, code, "")
                    }
                    is GoogleDriveResponse.Error -> {
                        throw response.error
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

        val account =
            Account(cloudAccount.getAccountName(), context.getString(R.string.account_type))

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