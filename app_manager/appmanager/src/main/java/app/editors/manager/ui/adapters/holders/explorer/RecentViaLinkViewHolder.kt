package app.editors.manager.ui.adapters.holders.explorer

import android.view.View
import android.widget.ImageView
import app.editors.manager.R
import app.editors.manager.mvp.models.list.RecentViaLink
import app.editors.manager.mvp.presenters.main.PickerMode
import app.editors.manager.ui.adapters.ExplorerAdapter
import app.editors.manager.ui.adapters.holders.BaseViewHolderExplorer


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
            height = if (adapter.isSelectMode || adapter.pickerMode != PickerMode.None) 0 else initHeight
        }
    }

    override val selectIcon: ImageView? = null

    override fun getCachedIcon(): View? = null

    companion object {
        val LAYOUT: Int = R.layout.layout_explorer_recent
    }
}