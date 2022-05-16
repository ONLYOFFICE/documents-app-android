package app.editors.manager.mvp.views.login

import app.documents.core.account.CloudAccount
import app.editors.manager.mvp.views.base.BaseView
import moxy.viewstate.strategy.OneExecutionStateStrategy
import moxy.viewstate.strategy.StateStrategyType

@StateStrategyType(OneExecutionStateStrategy::class)
interface AccountsView : BaseView {
    fun onAccountLogin()
    fun onUsersAccounts(accounts: MutableList<CloudAccount>)
    fun onAccountDelete(position: Int)
    fun onSignIn(portal: String, login: String)
    fun showWaitingDialog()
    fun onWebDavLogin(account: CloudAccount)
    fun onOneDriveLogin(account: CloudAccount)
    fun onDropboxLogin(account: CloudAccount)
    fun onGoogleDriveLogin(account: CloudAccount)
}