package app.editors.manager.ui.adapters.holders

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.View
import androidx.core.view.isVisible
import app.documents.core.network.manager.models.explorer.CloudFolder
import app.editors.manager.R
import app.editors.manager.databinding.ListExplorerFolderBinding
import app.editors.manager.managers.utils.ManagerUiUtils.setFolderIcon
import app.editors.manager.managers.utils.RoomUtils
import app.editors.manager.ui.adapters.ExplorerAdapter
import lib.toolkit.base.managers.utils.TimeUtils

class FolderViewHolder(view: View, adapter: ExplorerAdapter) :
    BaseViewHolderExplorer<CloudFolder>(view, adapter), SelectableIcon {

    private var viewBinding = ListExplorerFolderBinding.bind(view)
    override val selectableView = viewBinding.viewIconSelectableLayout.root

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

            listExplorerRoomPin.isVisible = folder.pinned
            setFolderIcon(folder)
            setSelected(adapter.isSelectMode, folder.isSelected)

            // Show/hide context button
            if (adapter.isSelectMode || adapter.isFoldersMode) {
                listExplorerFolderContext.isVisible = false
            }

        }
    }

    private fun setFolderIcon(folder: CloudFolder) {
        val initials = RoomUtils.getRoomInitials(folder.title)
        with(viewBinding.viewIconSelectableLayout) {
            if (!folder.logo?.large.isNullOrEmpty() || (folder.isRoom && initials.isNullOrEmpty())) {
                viewIconSelectableText.isVisible = false
                viewIconSelectableImage.isVisible = true
                viewIconSelectableImage.setFolderIcon(folder, adapter.isRoot)
            } else {
                viewIconSelectableImage.isVisible = false
                viewIconSelectableText.isVisible = true
                viewIconSelectableText.text = initials
                viewIconSelectableText.backgroundTintList =
                    ColorStateList.valueOf(
                        folder.logo?.color?.let { color -> Color.parseColor("#$color") }
                            ?: root.context.getColor(lib.toolkit.base.R.color.colorPrimary)
                    )
            }
        }
    }

    companion object {
        val LAYOUT: Int = R.layout.list_explorer_folder
    }
}