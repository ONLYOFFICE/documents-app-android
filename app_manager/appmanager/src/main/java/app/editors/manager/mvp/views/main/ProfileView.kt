package app.editors.manager.mvp.views.main

import app.documents.core.account.CloudAccount
import app.editors.manager.mvp.presenters.main.ProfileState
import app.editors.manager.mvp.views.base.BaseView
import moxy.viewstate.strategy.OneExecutionStateStrategy
import moxy.viewstate.strategy.StateStrategyType

@StateStrategyType(OneExecutionStateStrategy::class)
interface ProfileView : BaseView {
    fun onClose(isLogout: Boolean, account: CloudAccount? = null)
    fun onRender(state: ProfileState)
}