package app.editors.manager.mvp.views.filter

import app.editors.manager.mvp.models.filter.Author
import app.editors.manager.mvp.views.base.BaseViewExt
import moxy.viewstate.strategy.OneExecutionStateStrategy
import moxy.viewstate.strategy.StateStrategyType

interface FilterAuthorView : BaseViewExt {

    @StateStrategyType(OneExecutionStateStrategy::class)
    fun onGetUsers(users: List<Author.User>)

    @StateStrategyType(OneExecutionStateStrategy::class)
    fun onGetGroups(groups: List<Author.Group>)

    @StateStrategyType(OneExecutionStateStrategy::class)
    fun onLoadingGroups()

    @StateStrategyType(OneExecutionStateStrategy::class)
    fun onLoadingUsers()

    @StateStrategyType(OneExecutionStateStrategy::class)
    fun onUpdateAvatar(user: Author.User)
}