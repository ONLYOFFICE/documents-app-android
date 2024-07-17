package app.editors.manager.ui.adapters.holders.explorer

import android.view.View
import app.editors.manager.R
import app.editors.manager.mvp.models.list.Footer
import app.editors.manager.ui.adapters.ExplorerAdapter
import app.editors.manager.ui.adapters.holders.BaseViewHolderExplorer

class GridFooterViewHolder(parent: View, adapter: ExplorerAdapter) :
    BaseViewHolderExplorer<Footer>(parent, adapter) {

    companion object {

        val LAYOUT: Int = R.layout.layout_explorer_list_footer
    }

    override fun getCachedIcon(): View? = null

    override fun bind(element: Footer) {
        // Nothing
    }
}