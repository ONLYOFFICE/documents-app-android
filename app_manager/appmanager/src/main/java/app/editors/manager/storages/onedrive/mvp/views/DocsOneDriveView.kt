package app.editors.manager.storages.onedrive.mvp.views

import app.editors.manager.mvp.views.main.DocsBaseView
import moxy.viewstate.strategy.OneExecutionStateStrategy
import moxy.viewstate.strategy.StateStrategyType

@StateStrategyType(OneExecutionStateStrategy::class)
interface DocsOneDriveView: DocsBaseView {
}