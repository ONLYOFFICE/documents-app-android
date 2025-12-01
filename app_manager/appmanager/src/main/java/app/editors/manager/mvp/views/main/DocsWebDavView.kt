package app.editors.manager.mvp.views.main

import app.documents.core.model.login.OidcConfiguration
import moxy.viewstate.strategy.OneExecutionStateStrategy
import moxy.viewstate.strategy.StateStrategyType

@StateStrategyType(OneExecutionStateStrategy::class)
interface DocsWebDavView : DocsBaseView {
    fun onActionDialog()
    fun onOwnCloudAuthorization(config: OidcConfiguration)
}