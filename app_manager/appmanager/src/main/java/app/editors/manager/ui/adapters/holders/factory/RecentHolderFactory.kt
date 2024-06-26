package app.editors.manager.ui.adapters.holders.factory

import android.view.View
import app.documents.core.model.cloud.Recent
import app.editors.manager.R
import app.editors.manager.ui.adapters.holders.RecentHeaderViewHolder
import app.editors.manager.ui.adapters.holders.RecentViewHolder
import lib.toolkit.base.ui.adapters.factory.HolderFactory
import lib.toolkit.base.ui.adapters.holder.BaseViewHolder

class RecentHolderFactory(
    private val itemListener: ((recent: Recent) -> Unit)?,
    private val contextListener: ((recent: Recent, position: Int) -> Unit)?
) : HolderFactory() {

    override fun createViewHolder(view: View, type: Int): BaseViewHolder<*> {

        return when (type) {
            R.layout.list_explorer_files -> RecentViewHolder(view, itemListener, contextListener)
            lib.toolkit.base.R.layout.list_item_header -> RecentHeaderViewHolder(view)
            else -> throw RuntimeException("Need holder")
        }
    }
}