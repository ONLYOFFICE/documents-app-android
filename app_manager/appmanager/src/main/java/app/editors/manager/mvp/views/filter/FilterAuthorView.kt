package app.editors.manager.mvp.views.filter

import app.editors.manager.mvp.views.base.BaseViewExt
import app.editors.manager.ui.fragments.filter.Author
import moxy.viewstate.strategy.OneExecutionStateStrategy
import moxy.viewstate.strategy.StateStrategyType

interface FilterAuthorView : BaseViewExt {

    @StateStrategyType(OneExecutionStateStrategy::class)
    fun onGetUsers(users: List<Author.User>)

    @StateStrategyType(OneExecutionStateStrategy::class)
    fun onGetGroups(groups: List<Author.Group>)
}