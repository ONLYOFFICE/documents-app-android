package app.editors.manager.onedrive.mvp.presenters

import android.accounts.Account
import app.documents.core.account.CloudAccount
import app.editors.manager.R
import app.editors.manager.app.App
import app.editors.manager.app.oneDriveLoginService
import app.editors.manager.onedrive.onedrive.OneDriveResponse
import app.editors.manager.mvp.presenters.base.BasePresenter
import app.editors.manager.onedrive.managers.utils.OneDriveUtils
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


    fun checkOneDrive(token: String) {
        disposable = App.getApp().oneDriveLoginService.getUserInfo(token)
            .subscribe { oneDriveResponse ->
                when (oneDriveResponse) {
                    is OneDriveResponse.Success -> {
                        createUser(oneDriveResponse.response as User, token)
                    }
                    is OneDriveResponse.Error -> {
                        throw oneDriveResponse.error
                    }
                }
            }
    }


    private fun createUser(user: User, token: String) {
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
            accessToken = token,
            webDav = cloudAccount.webDavPath,
            email = user.userPrincipalName,
        )

        val account = Account(cloudAccount.getAccountName(), context.getString(R.string.account_type))

        if (AccountUtils.addAccount(context, account, "", accountData)) {
            addAccountToDb(cloudAccount)
        } else {
            AccountUtils.setAccountData(context, account, accountData)
            AccountUtils.setPassword(context, account, token)
            addAccountToDb(cloudAccount)
        }
        AccountUtils.setToken(context, account, token)
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