package app.editors.manager.mvp.views.main

import app.documents.core.storage.account.CloudAccount
import app.editors.manager.mvp.presenters.main.CloudAccountState
import app.editors.manager.mvp.views.base.BaseView
import moxy.viewstate.strategy.OneExecutionStateStrategy
import moxy.viewstate.strategy.StateStrategyType

@StateStrategyType(OneExecutionStateStrategy::class)
interface CloudAccountsView : BaseView {
    fun onAccountLogin()
    fun onWebDavLogin(account: CloudAccount)
    fun onShowClouds()
    fun onShowBottomDialog(account: CloudAccount?)
    fun onShowWaitingDialog()
    fun removeItem(position: Int)
    fun onUpdateItem(account: CloudAccount, position: Int)
    fun onSuccessLogin()
    fun onSignIn(portal: String?, login: String)
    fun onEmptyList()
    fun onSelectionMode()
    fun onDefaultState()
    fun onSelectedItem(position: Int)
    fun onActionBarTitle(title: String)
    fun onNotifyItems()
    fun onRender(state: CloudAccountState)
}