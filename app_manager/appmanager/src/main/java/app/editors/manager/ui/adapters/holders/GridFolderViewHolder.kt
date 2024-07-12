package app.editors.manager.ui.adapters.holders

import android.content.res.ColorStateList
import android.view.View
import androidx.core.widget.TextViewCompat
import app.documents.core.network.manager.models.explorer.CloudFolder
import app.editors.manager.R
import app.editors.manager.databinding.LayoutExplorerGridFolderBinding
import app.editors.manager.managers.utils.StringUtils
import app.editors.manager.ui.adapters.ExplorerAdapter

class GridFolderViewHolder(view: View, adapter: ExplorerAdapter) :
    BaseViewHolderExplorer<CloudFolder>(view, adapter) {

    companion object {
        val LAYOUT: Int = R.layout.layout_explorer_grid_folder
    }

    private val binding: LayoutExplorerGridFolderBinding = LayoutExplorerGridFolderBinding.bind(view)

    init {
        with(binding.root) {
            setOnClickListener { adapter.mOnItemClickListener?.onItemClick(view, layoutPosition) }
            setOnLongClickListener { adapter.mOnItemContextListener?.onItemContextClick(layoutPosition); false }
        }
    }

    override fun bind(element: CloudFolder) {
        with(binding) {
            title.text = element.title
            title.setCompoundDrawablesWithIntrinsicBounds(
                0,
                0,
                if (element.pinned) R.drawable.ic_pin_to_top else 0,
                0
            )
            TextViewCompat.setCompoundDrawableTintList(
                title,
                ColorStateList.valueOf(root.context.getColor(lib.toolkit.base.R.color.colorTextSecondary))
            )
            subtitle.text = StringUtils.getCloudItemInfo(
                context = adapter.context,
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