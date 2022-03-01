package app.editors.manager.storages.onedrive.mvp.views

import app.editors.manager.mvp.views.base.BaseView
import app.editors.manager.storages.base.view.BaseStorageSignInView
import moxy.viewstate.strategy.OneExecutionStateStrategy
import moxy.viewstate.strategy.StateStrategyType

@StateStrategyType(OneExecutionStateStrategy::class)
interface OneDriveSignInView : BaseStorageSignInView {
}