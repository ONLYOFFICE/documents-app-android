package app.editors.manager.ui.adapters.holders.factory

import android.view.View
import app.documents.core.model.cloud.Recent
import app.editors.manager.ui.adapters.holders.FileViewHolder
import app.editors.manager.ui.adapters.holders.GridFileViewHolder
import app.editors.manager.ui.adapters.holders.GridRecentViewHolder
import app.editors.manager.ui.adapters.holders.RecentViewHolder
import lib.toolkit.base.ui.adapters.factory.HolderFactory
import lib.toolkit.base.ui.adapters.holder.BaseViewHolder

class RecentHolderFactory(
    private val itemListener: ((recent: Recent) -> Unit)?,
    private val contextListener: ((recent: Recent, position: Int) -> Unit)?
) : HolderFactory() {

    override fun createViewHolder(view: View, type: Int): BaseViewHolder<*> {

        return when (type) {
            GridFileViewHolder.LAYOUT -> GridRecentViewHolder(view, itemListener, contextListener)
            FileViewHolder.LAYOUT -> RecentViewHolder(view, itemListener, contextListener)
            else -> throw RuntimeException("Need holder")
        }
    }
}