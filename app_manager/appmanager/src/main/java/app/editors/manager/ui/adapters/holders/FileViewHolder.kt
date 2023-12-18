package app.editors.manager.ui.adapters.holders

import android.view.View
import androidx.core.view.isVisible
import app.documents.core.network.manager.models.explorer.CloudFile
import app.editors.manager.R
import app.editors.manager.app.accountOnline
import app.editors.manager.databinding.ListExplorerFilesBinding
import app.editors.manager.managers.utils.ManagerUiUtils.setFileIcon
import app.editors.manager.ui.adapters.ExplorerAdapter
import app.editors.manager.managers.utils.StringUtils as ManagerStringUtils

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

        viewBinding.listExplorerFileContext.setOnClickListener {
            adapter.mOnItemContextListener?.onItemContextClick(layoutPosition)
        }
    }

    override fun bind(file: CloudFile) {
        with(viewBinding) {
            listExplorerFileName.text = file.title
            listExplorerFileInfo.text = ManagerStringUtils.getCloudItemInfo(
                context = adapter.context,
                item = file,
                userId = adapter.context.accountOnline?.id,
                sortBy = adapter.preferenceTool.sortBy,
                isSectionMy = adapter.isSectionMy
            )

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
        val LAYOUT: Int = R.layout.list_explorer_files
    }
}