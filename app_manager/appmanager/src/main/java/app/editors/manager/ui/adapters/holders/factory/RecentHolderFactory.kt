package app.editors.manager.ui.adapters.holders.factory

import android.view.View
import app.documents.core.model.cloud.Recent
import app.editors.manager.ui.adapters.holders.explorer.ListFileViewHolder
import app.editors.manager.ui.adapters.holders.explorer.GridFileViewHolder
import app.editors.manager.ui.adapters.holders.explorer.GridRecentViewHolder
import app.editors.manager.ui.adapters.holders.explorer.ListRecentViewHolder
import lib.toolkit.base.ui.adapters.factory.HolderFactory
import lib.toolkit.base.ui.adapters.holder.BaseViewHolder

class RecentHolderFactory(
    private val itemListener: ((recent: Recent) -> Unit)?,
    private val contextListener: ((recent: Recent, position: Int) -> Unit)?
) : HolderFactory() {

    override fun createViewHolder(view: View, type: Int): BaseViewHolder<*> {

        return when (type) {
            GridFileViewHolder.LAYOUT -> GridRecentViewHolder(view, itemListener, contextListener)
            ListFileViewHolder.LAYOUT -> ListRecentViewHolder(view, itemListener, contextListener)
            else -> throw RuntimeException("Need holder")
        }
    }
}