package app.editors.manager.onedrive.mvp.presenters

import android.accounts.Account
import app.documents.core.account.CloudAccount
import app.editors.manager.R
import app.editors.manager.app.App
import app.editors.manager.app.oneDriveAuthService
import app.editors.manager.app.oneDriveLoginService
import app.editors.manager.managers.utils.Constants
import app.editors.manager.managers.utils.StorageUtils
import app.editors.manager.onedrive.onedrive.OneDriveResponse
import app.editors.manager.mvp.presenters.base.BasePresenter
import app.editors.manager.onedrive.managers.utils.OneDriveUtils
import app.editors.manager.onedrive.mvp.models.response.AuthResponse
import app.editors.manager.onedrive.mvp.views.OneDriveSignInView
import app.editors.manager.onedrive.mvp.models.user.User
import app.editors.manager.onedrive.onedrive.OneDriveService
import io.reactivex.disposables.Disposable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import lib.toolkit.base.managers.utils.AccountData
import lib.toolkit.base.managers.utils.AccountUtils
import moxy.InjectViewState

@InjectViewState
class OneDriveSingInPresenter : BasePresenter<OneDriveSignInView>() {

    init {
        App.getApp().appComponent.inject(this)
    }

    var disposable: Disposable? = null

    override fun onDestroy() {
        super.onDestroy()
        disposable?.dispose()
    }

    fun getToken(code: String) {
        val map = mapOf(
            StorageUtils.ARG_CLIENT_ID to Constants.OneDrive.COM_CLIENT_ID,
            StorageUtils.ARG_SCOPE to StorageUtils.OneDrive.VALUE_SCOPE,
            StorageUtils.ARG_REDIRECT_URI to Constants.OneDrive.COM_REDIRECT_URL,
            StorageUtils.OneDrive.ARG_GRANT_TYPE to StorageUtils.OneDrive.VALUE_GRANT_TYPE_AUTH,
            StorageUtils.OneDrive.ARG_CLIENT_SECRET to Constants.OneDrive.COM_CLIENT_SECRET,
            StorageUtils.ARG_CODE to code
        )
        var accessToken = ""
        var refreshToken = ""
        disposable = App.getApp().oneDriveAuthService.getToken(map)
            .map { oneDriveResponse ->
                when(oneDriveResponse) {
                    is OneDriveResponse.Success -> {
                        accessToken = (oneDriveResponse.response as AuthResponse).access_token
                        refreshToken = oneDriveResponse.response.refresh_token
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
                        throw oneDriveResponse.error
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