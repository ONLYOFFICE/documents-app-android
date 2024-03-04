package app.editors.manager.ui.adapters.holders

import android.view.View
import androidx.core.view.isVisible
import app.documents.core.network.manager.models.explorer.CloudFile
import app.editors.manager.R
import app.editors.manager.app.accountOnline
import app.editors.manager.databinding.ListExplorerFilesBinding
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

            listExplorerFileContext.isVisible = !adapter.isSelectMode
            listExplorerFileFavorite.isVisible = file.favorite

            viewIconSelectableLayout.setItem(file)
            viewIconSelectableLayout.selectMode = adapter.isSelectMode
            viewIconSelectableLayout.itemSelected = file.isSelected
        }
    }

    companion object {
        val LAYOUT: Int = R.layout.list_explorer_files
    }
}