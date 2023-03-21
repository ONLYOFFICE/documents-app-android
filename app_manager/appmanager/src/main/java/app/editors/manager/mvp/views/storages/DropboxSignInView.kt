package app.editors.manager.mvp.views.storages

import app.editors.manager.mvp.views.base.BaseStorageSignInView
import moxy.viewstate.strategy.OneExecutionStateStrategy
import moxy.viewstate.strategy.StateStrategyType

@StateStrategyType(OneExecutionStateStrategy::class)
interface DropboxSignInView: BaseStorageSignInView {
}