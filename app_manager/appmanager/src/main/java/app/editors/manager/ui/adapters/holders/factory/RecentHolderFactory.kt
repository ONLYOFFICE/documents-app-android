package app.editors.manager.ui.adapters.holders.factory

import android.view.View
import app.documents.core.storage.recent.Recent
import app.editors.manager.R
import app.editors.manager.ui.adapters.holders.RecentHeaderViewHolder
import app.editors.manager.ui.adapters.holders.RecentViewHolder
import lib.toolkit.base.ui.adapters.factory.HolderFactory
import lib.toolkit.base.ui.adapters.holder.BaseViewHolder

class RecentHolderFactory(
    private val itemListener: ((recent: Recent, position: Int) -> Unit)?,
    private val contextListener: ((recent: Recent, position: Int) -> Unit)?
) : HolderFactory() {

    override fun createViewHolder(view: View, type: Int): BaseViewHolder<*> {

        return when (type) {
            R.layout.list_explorer_files -> RecentViewHolder(view, itemListener, contextListener)
            R.layout.list_explorer_header -> RecentHeaderViewHolder(view)
            else -> throw RuntimeException("Need holder")
        }
    }
}