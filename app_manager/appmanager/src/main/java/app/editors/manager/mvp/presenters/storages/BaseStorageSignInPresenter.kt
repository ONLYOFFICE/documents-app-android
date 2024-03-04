package app.editors.manager.mvp.presenters.storages

import android.accounts.Account
import app.documents.core.model.cloud.CloudAccount
import app.editors.manager.mvp.presenters.base.BasePresenter
import app.editors.manager.mvp.views.base.BaseStorageSignInView
import io.reactivex.disposables.Disposable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import lib.toolkit.base.R
import lib.toolkit.base.managers.utils.AccountData
import lib.toolkit.base.managers.utils.AccountUtils
import moxy.presenterScope

open class BaseStorageSignInPresenter<view: BaseStorageSignInView>: BasePresenter<view>() {

    var disposable: Disposable? = null

    override fun onDestroy() {
        super.onDestroy()
        disposable?.dispose()
    }

    fun saveAccount(cloudAccount: CloudAccount, accountData: AccountData, accessToken: String) {
        networkSettings.isDocSpace = false
        val account = Account(cloudAccount.accountName, context.getString(R.string.account_type))

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
        presenterScope.launch {
            cloudDataSource.getAccountOnline()?.let {
                cloudDataSource.addAccount(it.copy(isOnline = false))
            }
            cloudDataSource.addAccount(cloudAccount.copy(isOnline = true))
            withContext(Dispatchers.Main) {
                viewState.onLogin()
            }
        }
    }

}