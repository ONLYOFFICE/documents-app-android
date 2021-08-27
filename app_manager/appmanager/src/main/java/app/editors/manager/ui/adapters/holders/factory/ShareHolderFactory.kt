package app.editors.manager.ui.adapters.holders.factory

import android.view.View
import app.editors.manager.R
import app.editors.manager.ui.adapters.base.BaseAdapter
import app.editors.manager.ui.adapters.holders.ShareHeaderViewHolder
import app.editors.manager.ui.adapters.holders.ShareItemViewHolder
import app.editors.manager.ui.dialogs.ActionBottomDialog
import lib.toolkit.base.ui.adapters.factory.HolderFactory
import lib.toolkit.base.ui.adapters.holder.BaseViewHolder
import java.lang.RuntimeException

class ShareHolderFactory(private val clickListener: (view: View, position: Int) -> Unit)
    : HolderFactory() {

    override fun createViewHolder(view: View, type: Int): BaseViewHolder<*> {
        return when (type) {
            R.layout.list_share_add_header -> ShareHeaderViewHolder(view)
            R.layout.list_share_settings_item -> ShareItemViewHolder(view, clickListener)
            else -> throw RuntimeException("Need holder")
        }
    }
}