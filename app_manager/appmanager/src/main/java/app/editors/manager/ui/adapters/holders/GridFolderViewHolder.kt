package app.editors.manager.ui.adapters.holders

import android.view.View
import android.widget.TableLayout
import android.widget.TableLayout.LayoutParams.WRAP_CONTENT
import androidx.core.view.isVisible
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
            setOnLongClickListener { adapter.mOnItemLongClickListener?.onItemLongClick(view, layoutPosition); false }
        }
    }

    override fun bind(element: CloudFolder) {
        with(binding) {
            title.post {
                if (title.lineCount > 1) {
                    title.layoutParams = TableLayout.LayoutParams(0, WRAP_CONTENT, 1f)
                }
            }
            title.text = element.title
            subtitle.text = StringUtils.getCloudItemInfo(
                context = adapter.context,
                item = element,
                userId = adapter.accountId,
                sortBy = adapter.preferenceTool.sortBy,
                isSectionMy = adapter.isSectionMy
            )
            icon.setItem(element, adapter.isRoot, true)
            iconPin.isVisible = element.pinned
        }
    }
}