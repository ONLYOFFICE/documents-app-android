package app.editors.manager.ui.adapters.holders

import android.view.View
import app.editors.manager.R
import app.editors.manager.databinding.ListExplorerFilesBinding
import app.editors.manager.managers.utils.ManagerUiUtils.setFileIcon
import app.editors.manager.managers.utils.isVisible
import app.editors.manager.mvp.models.explorer.CloudFile
import app.editors.manager.ui.adapters.ExplorerAdapter
import lib.toolkit.base.managers.utils.StringUtils
import lib.toolkit.base.managers.utils.TimeUtils

class FileViewHolder(itemView: View, adapter: ExplorerAdapter) :
    BaseViewHolderExplorer<CloudFile>(itemView, adapter) {

    private var viewBinding = ListExplorerFilesBinding.bind(itemView)

    init {
        viewBinding.listExplorerFileLayout.setOnClickListener{ view ->
            adapter.mOnItemClickListener?.onItemClick(view, layoutPosition)
        }

        viewBinding.listExplorerFileLayout.setOnLongClickListener { view ->
            adapter.mOnItemLongClickListener?.onItemLongClick(view, layoutPosition)
            false
        }

        viewBinding.listExplorerFileContext.setOnClickListener { view ->
            adapter.mOnItemContextListener?.onItemContextClick(view, layoutPosition)
        }
    }

    override fun bind(file: CloudFile) {
        // Get file info
        val filesInfo: String = file.createdBy.displayName + PLACEHOLDER_POINT
            .takeIf { file.createdBy.displayName.isNotEmpty() }.orEmpty() +
                TimeUtils.getWeekDate(file.updated) + PLACEHOLDER_POINT +
                StringUtils.getFormattedSize(adapter.context, file.pureContentLength)

        if (adapter.preferenceTool.selfId.equals(file.createdBy.id, ignoreCase = true)) {
            if (!adapter.isSectionMy) {
                filesInfo + PLACEHOLDER_POINT + adapter.context.getString(R.string.item_owner_self)
            }
        } else if (file.createdBy.title.isNotEmpty()) {
            filesInfo + PLACEHOLDER_POINT + file.createdBy.displayName
        }

        with(viewBinding) {
            listExplorerFileName.text = file.title
            listExplorerFileInfo.text = filesInfo
            listExplorerFileContext.isVisible = true
            listExplorerFileFavorite.isVisible = file.favorite

            viewIconSelectableLayout.viewIconSelectableImage.background = null
            viewIconSelectableLayout.viewIconSelectableMask.background = null
            viewIconSelectableLayout.viewIconSelectableImage.setFileIcon(file.fileExst)

            // For selection mode add background/foreground
            if (adapter.isSelectMode) {
                listExplorerFileContext.isVisible = false
                if (file.isSelected) {
                    viewIconSelectableLayout.viewIconSelectableMask.setBackgroundResource(R.drawable.drawable_list_image_select_mask)
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
        const val LAYOUT: Int = R.layout.list_explorer_files
    }
}