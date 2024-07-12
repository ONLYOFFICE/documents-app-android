package app.editors.manager.ui.adapters.holders

import android.view.View
import app.documents.core.network.manager.models.explorer.CloudFile
import app.editors.manager.R
import app.editors.manager.databinding.LayoutExplorerGridFileBinding
import app.editors.manager.managers.utils.StringUtils
import app.editors.manager.ui.adapters.ExplorerAdapter


class GridFileViewHolder(view: View, adapter: ExplorerAdapter) :
    BaseViewHolderExplorer<CloudFile>(view, adapter) {

    companion object {
        val LAYOUT: Int = R.layout.layout_explorer_grid_file
    }

    private val binding: LayoutExplorerGridFileBinding = LayoutExplorerGridFileBinding.bind(view)

    init {
        with(binding.root) {
            setOnClickListener { adapter.mOnItemClickListener?.onItemClick(view, layoutPosition) }
            setOnLongClickListener { adapter.mOnItemContextListener?.onItemContextClick(layoutPosition); false }
        }
    }

    override fun bind(element: CloudFile) {
        with(binding) {
            title.text = element.title
            title.setCompoundDrawablesWithIntrinsicBounds(
                0,
                0,
                if (element.favorite) R.drawable.ic_favorites_fill else 0,
                0
            )
            subtitle.text = StringUtils.getCloudItemInfo(
                context = root.context,
                item = element,
                userId = adapter.accountId,
                sortBy = adapter.preferenceTool.sortBy,
                isSectionMy = adapter.isSectionMy
            )
            icon.setItem(element, adapter.isRoot, true, element.isSelected)
            icon.alpha = if (adapter.isSelectMode && !element.isSelected) .4f else 1f
        }
    }
}