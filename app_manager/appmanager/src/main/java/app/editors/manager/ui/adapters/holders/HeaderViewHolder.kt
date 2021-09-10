package app.editors.manager.ui.adapters.holders

import android.view.View
import app.editors.manager.R
import app.editors.manager.databinding.ListExplorerHeaderBinding
import app.editors.manager.mvp.models.list.Header
import app.editors.manager.ui.adapters.ExplorerAdapter

class HeaderViewHolder(itemView: View, adapter: ExplorerAdapter) :
    BaseViewHolderExplorer<Header>(itemView, adapter) {

    private val viewBinding = ListExplorerHeaderBinding.bind(itemView)

    override fun bind(header: Header) {
        viewBinding.listExplorerHeaderTitle.text = header.title
    }

    companion object {
        const val LAYOUT: Int = R.layout.list_explorer_header
    }
}