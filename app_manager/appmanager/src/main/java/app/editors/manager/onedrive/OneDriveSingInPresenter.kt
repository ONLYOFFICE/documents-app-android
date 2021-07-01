package app.editors.manager.onedrive

import android.accounts.Account
import android.util.Log
import app.documents.core.account.CloudAccount
import app.documents.core.webdav.WebDavApi
import app.editors.manager.R
import app.editors.manager.app.App
import app.editors.manager.managers.providers.OneDriveResponse
import app.editors.manager.managers.utils.Constants
import app.editors.manager.managers.utils.StorageUtils
import app.editors.manager.mvp.presenters.base.BasePresenter
import io.reactivex.disposables.Disposable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import lib.toolkit.base.managers.utils.AccountData
import lib.toolkit.base.managers.utils.AccountUtils
import moxy.InjectViewState
import java.net.URL


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
        Log.d("ONEDRIVE", "$token")
        disposable = App.getApp().getOneDriveComponent(token).oneDriveService.userInfo()
            .subscribe({oneDriveResponse ->
                when(oneDriveResponse) {
                    is OneDriveResponse.Success -> {
                        createUser(oneDriveResponse.response as User, token)
                    }
                    is OneDriveResponse.Error -> {Log.d("ONEDRIVE", "${oneDriveResponse.error}")}
                }
            }, {error ->
                Log.d("ONEDRIVE", "error = ${error.message}")
            })
    }


    private fun createUser(user: User, token: String) {
        networkSettings.setBaseUrl("https://graph.microsoft.com/")
        val cloudAccount = CloudAccount(
            id = "${user.userPrincipalName}",
            isWebDav = false,
            isOneDrive = true,
            portal = "",
            webDavPath = "",
            webDavProvider = "",
            login = "${user.userPrincipalName}",
            scheme = "https://",
            isSslState = networkSettings.getSslState(),
            isSslCiphers = networkSettings.getCipher(),
            name = "${user.displayName}"
        )

        val accountData = AccountData(
            portal = cloudAccount.portal ?: "",
            scheme = cloudAccount.scheme ?: "",
            displayName = "${user.displayName}",
            userId = cloudAccount.id,
            provider = cloudAccount.webDavProvider ?: "",
            accessToken = token,
            webDav = cloudAccount.webDavPath,
            email = "${user.userPrincipalName}",
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