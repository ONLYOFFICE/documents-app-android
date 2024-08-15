package app.editors.manager.ui.adapters.holders

import android.view.View
import androidx.core.view.isVisible
import app.documents.core.network.manager.models.explorer.CloudFile
import app.editors.manager.R
import app.editors.manager.databinding.ListExplorerFilesBinding
import app.editors.manager.mvp.presenters.main.PickerMode
import app.editors.manager.ui.adapters.ExplorerAdapter
import app.editors.manager.managers.utils.StringUtils as ManagerStringUtils

class FileViewHolder(private val itemView: View, adapter: ExplorerAdapter) :
    BaseViewHolderExplorer<CloudFile>(itemView, adapter) {

    private var viewBinding = ListExplorerFilesBinding.bind(itemView)

    init {
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
                userId = adapter.accountId,
                sortBy = adapter.preferenceTool.sortBy,
                isSectionMy = adapter.isSectionMy
            )

            listExplorerFileContext.isVisible = !adapter.isSelectMode && adapter.pickerMode == PickerMode.None
            listExplorerFileFavorite.isVisible = file.favorite

            viewIconSelectableLayout.setItem(file)
            viewIconSelectableLayout.selectMode = adapter.isSelectMode

            if (adapter.pickerMode is PickerMode.Files) {
                viewIconSelectableLayout.itemSelected =
                    file.id in (adapter.pickerMode as PickerMode.Files).selectedIds
            } else {
                viewIconSelectableLayout.itemSelected = file.isSelected
            }

            val pickerMode = adapter.pickerMode
            if (pickerMode == PickerMode.Folders || pickerMode is PickerMode.Files &&
                (!file.isPdfForm || file.folderId == pickerMode.destFolderId)
            ) {
                root.alpha = .6f
                root.isClickable = false
                root.isFocusable = false
                root.setOnClickListener(null)
            } else {
                root.alpha = 1f
                root.isClickable = true
                root.isFocusable = true
                root.setOnClickListener { view ->
                    adapter.mOnItemClickListener?.onItemClick(view, layoutPosition)
                }
            }
        }
    }

    companion object {
        val LAYOUT: Int = R.layout.list_explorer_files
    }
}