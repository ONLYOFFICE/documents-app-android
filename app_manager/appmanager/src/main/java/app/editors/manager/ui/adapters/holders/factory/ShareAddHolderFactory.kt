package app.editors.manager.ui.adapters.holders.factory

import android.view.View
import app.editors.manager.R
import app.editors.manager.ui.adapters.holders.ShareAddHeaderViewHolder
import app.editors.manager.ui.adapters.holders.ShareAddItemViewHolder
import lib.toolkit.base.ui.adapters.factory.HolderFactory
import lib.toolkit.base.ui.adapters.holder.BaseViewHolder
import lib.toolkit.base.ui.adapters.holder.ViewType

class ShareAddHolderFactory(private val clickListener: (view: View, position: Int) -> Unit)
    : HolderFactory() {

    override fun createViewHolder(view: View, type: Int): BaseViewHolder<*> {
        return when (type) {
            R.layout.list_share_add_header -> ShareAddHeaderViewHolder<ViewType>(view)
            R.layout.list_share_add_item -> ShareAddItemViewHolder<ViewType>(view, clickListener)
            else -> throw RuntimeException("Need holder")
        }
    }
}