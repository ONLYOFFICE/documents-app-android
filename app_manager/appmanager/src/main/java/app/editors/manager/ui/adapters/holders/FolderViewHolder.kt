package app.editors.manager.ui.adapters.holders

import android.view.View
import androidx.core.view.isVisible
import app.editors.manager.R
import app.editors.manager.databinding.ListExplorerFolderBinding
import app.editors.manager.managers.utils.ManagerUiUtils.setFolderIcon
import app.documents.core.network.manager.models.explorer.CloudFolder
import app.editors.manager.ui.adapters.ExplorerAdapter
import lib.toolkit.base.managers.utils.TimeUtils

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
            adapter.mOnItemContextListener?.onItemContextClick(view, layoutPosition)
        }
    }

    override fun bind(folder: CloudFolder) {
        // Get folder info
        val folderInfo = TimeUtils.getWeekDate(folder.updated)

        if (adapter.preferenceTool.selfId.equals(folder.createdBy.id, ignoreCase = true)) {
            if (!adapter.isSectionMy) {
                folderInfo + PLACEHOLDER_POINT +
                        adapter.context.getString(R.string.item_owner_self)
            }
        } else if (folder.createdBy.displayName.isNotEmpty()) {
            folderInfo + PLACEHOLDER_POINT + folder.createdBy.displayName
        }

        with(viewBinding) {
            listExplorerFolderName.text = folder.title
            listExplorerFolderInfo.text = folderInfo
            listExplorerFolderContext.isVisible = true
            viewIconSelectableLayout.viewIconSelectableImage.background = null
            viewIconSelectableLayout.viewIconSelectableMask.background = null
            viewIconSelectableLayout.viewIconSelectableImage.setFolderIcon(folder, adapter.isRoot)

            listExplorerRoomPin.isVisible = folder.pinned

            // Show/hide context button
            if (adapter.isSelectMode || adapter.isFoldersMode) {
                listExplorerFolderContext.isVisible = false
            }

            // For selection mode add background/foreground
            if (adapter.isSelectMode) {
                if (folder.isSelected) {
                    viewIconSelectableLayout.viewIconSelectableMask
                        .setBackgroundResource(R.drawable.drawable_list_image_select_mask)
                } else {
                    viewIconSelectableLayout.viewIconSelectableLayout
                        .setBackgroundResource(R.drawable.drawable_list_image_select_background)
                }
            } else {
                viewIconSelectableLayout.viewIconSelectableLayout.background = null
            }
        }
    }

    companion object {
        const val LAYOUT: Int = R.layout.list_explorer_folder
    }
}