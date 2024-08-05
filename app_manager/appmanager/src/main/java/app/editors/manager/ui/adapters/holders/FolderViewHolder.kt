package app.editors.manager.ui.adapters.holders

import android.view.View
import androidx.core.view.isVisible
import app.documents.core.network.manager.models.explorer.CloudFolder
import app.editors.manager.R
import app.editors.manager.databinding.ListExplorerFolderBinding
import app.editors.manager.managers.utils.StringUtils
import app.editors.manager.ui.adapters.ExplorerAdapter

class FolderViewHolder(view: View, adapter: ExplorerAdapter) :
    BaseViewHolderExplorer<CloudFolder>(view, adapter) {

    private var viewBinding = ListExplorerFolderBinding.bind(view)

    init {
        viewBinding.listExplorerFolderLayout.setOnLongClickListener { v: View? ->
            adapter.mOnItemLongClickListener?.onItemLongClick(v, layoutPosition)
            false
        }

        viewBinding.listExplorerFolderContext.setOnClickListener {
            adapter.mOnItemContextListener?.onItemContextClick(layoutPosition)
        }
    }

    override fun bind(folder: CloudFolder) {
        with(viewBinding) {
            listExplorerFolderName.text = folder.title
            listExplorerFolderInfo.text = StringUtils.getCloudItemInfo(
                context = adapter.context,
                item = folder,
                userId = adapter.accountId,
                sortBy = adapter.preferenceTool.sortBy,
                isSectionMy = adapter.isSectionMy
            )

            // Show/hide context button
            listExplorerFolderContext.isVisible = !adapter.isSelectMode && !adapter.isFoldersMode

            listExplorerRoomPin.isVisible = folder.pinned
            viewIconSelectableLayout.setItem(folder, adapter.isRoot)
            viewIconSelectableLayout.selectMode = adapter.isSelectMode
            viewIconSelectableLayout.itemSelected = folder.isSelected

            if (adapter.isFoldersMode && folder.type in arrayOf(25, 26)) {
                listExplorerFolderLayout.alpha = .6f
                listExplorerFolderLayout.setOnClickListener(null)
            } else {
                listExplorerFolderLayout.alpha = 1f
                listExplorerFolderLayout.setOnClickListener { v: View? ->
                    adapter.mOnItemClickListener?.onItemClick(v, layoutPosition)
                }
            }
        }
    }

    companion object {
        val LAYOUT: Int = R.layout.list_explorer_folder
    }
}