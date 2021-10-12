package app.editors.manager.dropbox.mvp.views

import app.editors.manager.mvp.views.base.BaseView
import moxy.viewstate.strategy.OneExecutionStateStrategy
import moxy.viewstate.strategy.StateStrategyType

@StateStrategyType(OneExecutionStateStrategy::class)
interface DropboxSignInView: BaseView {
    fun onLogin()
}