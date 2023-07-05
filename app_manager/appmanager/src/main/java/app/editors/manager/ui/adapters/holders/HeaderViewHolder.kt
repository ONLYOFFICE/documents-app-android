package app.editors.manager.ui.adapters.holders

import android.view.View
import app.editors.manager.mvp.models.list.Header
import app.editors.manager.ui.adapters.ExplorerAdapter
import lib.toolkit.base.databinding.ListItemHeaderBinding

class HeaderViewHolder(itemView: View, adapter: ExplorerAdapter) :
    BaseViewHolderExplorer<Header>(itemView, adapter) {

    override fun bind(element: Header) {
        with(ListItemHeaderBinding.bind(itemView)) {
            title.text = element.title
        }
    }

    companion object {
        var LAYOUT: Int = lib.toolkit.base.R.layout.list_item_header
    }
}