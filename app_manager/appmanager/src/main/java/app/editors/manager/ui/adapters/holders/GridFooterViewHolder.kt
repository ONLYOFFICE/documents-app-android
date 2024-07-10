package app.editors.manager.ui.adapters.holders

import android.view.View
import app.editors.manager.R
import app.editors.manager.mvp.models.list.Footer
import app.editors.manager.ui.adapters.ExplorerAdapter

class GridFooterViewHolder(parent: View, adapter: ExplorerAdapter)
    : BaseViewHolderExplorer<Footer>(parent, adapter) {

//    private val viewBinding = ListExplorerFooterBinding.bind(parent)
//
//    init {
//        viewBinding.listExplorerFooterProgress.indeterminateDrawable?.setColorFilter(
//            ContextCompat.getColor(adapter.context, lib.toolkit.base.R.color.colorSecondary),
//            PorterDuff.Mode.SRC_IN)
//    }
//

    override fun bind(footer: Footer) {
//        viewBinding.listExplorerFooterLayout.isVisible = adapter.isFooter
    }

    companion object {
        val LAYOUT: Int = R.layout.list_explorer_footer
    }
}