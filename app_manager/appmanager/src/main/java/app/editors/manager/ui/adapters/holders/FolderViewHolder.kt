package app.editors.manager.ui.adapters.holders

import android.view.View
import androidx.core.view.isVisible
import app.documents.core.network.manager.models.explorer.CloudFolder
import app.editors.manager.R
import app.editors.manager.app.accountOnline
import app.editors.manager.databinding.ListExplorerFolderBinding
import app.editors.manager.managers.utils.ManagerUiUtils.setFolderIcon
import app.editors.manager.managers.utils.StringUtils
import app.editors.manager.ui.adapters.ExplorerAdapter

class FolderViewHolder(view: View, adapter: ExplorerAdapter) :
    BaseViewHolderExplorer<CloudFolder>(view, adapter) {

    private var viewBinding = ListExplorerFolderBinding.bind(view)

    init {
        viewBinding.listExplorerFolderLayout.setOnClickListener { v: View? ->
            adapter.mOnItemClickListener?.onItemClick(v, layoutPosition)
        }

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
                userId = adapter.context.accountOnline?.id,
                sortBy = adapter.preferenceTool.sortBy,
                isSectionMy = adapter.isSectionMy
            )

            listExplorerFolderContext.isVisible = true

            listExplorerRoomPin.isVisible = folder.pinned
            viewIconSelectableLayout.setItem(folder, adapter.isRoot)
            viewIconSelectableLayout.selectMode = adapter.isSelectMode
            viewIconSelectableLayout.itemSelected = folder.isSelected

            // Show/hide context button
            if (adapter.isSelectMode || adapter.isFoldersMode) {
                listExplorerFolderContext.isVisible = false
            }

        }
    }

    companion object {
        val LAYOUT: Int = R.layout.list_explorer_folder
    }
}