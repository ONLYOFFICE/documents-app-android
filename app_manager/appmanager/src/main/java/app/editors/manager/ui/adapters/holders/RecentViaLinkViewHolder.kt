package app.editors.manager.ui.adapters.holders

import android.view.View
import app.editors.manager.R
import app.editors.manager.mvp.models.list.RecentViaLink
import app.editors.manager.ui.adapters.ExplorerAdapter


class RecentViaLinkViewHolder(private val view: View, adapter: ExplorerAdapter) :
    BaseViewHolderExplorer<RecentViaLink>(view, adapter) {

    private val initHeight = view.resources.getDimensionPixelSize(lib.toolkit.base.R.dimen.item_two_line_height)

    init {
        view.setOnClickListener {
            adapter.mOnItemClickListener?.onItemClick(view, layoutPosition)
        }
    }

    override fun bind(element: RecentViaLink) {
        view.layoutParams = view.layoutParams.apply {
            height = if (adapter.isSelectMode || adapter.isFoldersMode) 0 else initHeight
        }
    }

    companion object {
        val LAYOUT: Int = R.layout.list_explorer_recent
    }
}