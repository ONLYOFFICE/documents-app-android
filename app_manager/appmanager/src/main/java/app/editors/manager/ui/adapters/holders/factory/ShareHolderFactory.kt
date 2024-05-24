package app.editors.manager.ui.adapters.holders.factory

import android.view.View
import app.editors.manager.R
import app.editors.manager.ui.adapters.holders.ShareAddItemViewHolder
import app.editors.manager.ui.adapters.holders.ShareHeaderViewHolder
import app.editors.manager.ui.adapters.holders.ShareItemViewHolder
import lib.toolkit.base.ui.adapters.factory.HolderFactory
import lib.toolkit.base.ui.adapters.holder.BaseViewHolder

class ShareHolderFactory(private val clickListener: (view: View, position: Int) -> Unit)
    : HolderFactory() {

    override fun createViewHolder(view: View, type: Int): BaseViewHolder<*> {
        return when (type) {
            R.layout.list_share_add_item -> ShareAddItemViewHolder(view, clickListener)
            lib.toolkit.base.R.layout.list_item_header -> ShareHeaderViewHolder(view)
            R.layout.list_share_settings_item -> ShareItemViewHolder(view, clickListener)
            else -> throw RuntimeException("Need holder")
        }
    }
}