package app.editors.manager.ui.adapters.holders

import android.view.View
import app.editors.manager.R
import app.editors.manager.databinding.ListExplorerFilesBinding
import app.editors.manager.mvp.models.explorer.CloudFile
import app.editors.manager.ui.adapters.ExplorerAdapter
import lib.toolkit.base.managers.utils.StringUtils
import lib.toolkit.base.managers.utils.TimeUtils

class FileViewHolder(itemView: View, adapter: ExplorerAdapter) :
    BaseViewHolderExplorer<CloudFile>(itemView, adapter) {

    private var viewBinding = ListExplorerFilesBinding.bind(itemView)

    init {
        viewBinding.listExplorerFileLayout.setOnClickListener{ view ->
            adapter.mOnItemContextListener?.run {
                adapter.mOnItemClickListener.onItemClick(view, layoutPosition)
            }
        }
        viewBinding.listExplorerFileLayout.setOnLongClickListener { view ->
            adapter.mOnItemLongClickListener?.run {
                adapter.mOnItemLongClickListener.onItemLongClick(view, layoutPosition)
            }
            false
        }
        viewBinding.listExplorerFileContext.setOnClickListener { view ->
            adapter.mOnItemContextListener.onItemContextClick(view, layoutPosition)
        }
    }

    override fun bind(file: CloudFile) {
        // Get file info
        val filesInfo: String = TimeUtils.getWeekDate(file.updated) + PLACEHOLDER_POINT +
                StringUtils.getFormattedSize(adapter.mContext, file.pureContentLength)

        if (adapter.mPreferenceTool.selfId.equals(file.createdBy.id, ignoreCase = true)) {
            if (!adapter.isSectionMy) {
                filesInfo + PLACEHOLDER_POINT + adapter.mContext.getString(R.string.item_owner_self)
            }
        } else if (file.createdBy.title.isNotEmpty()) {
            filesInfo + PLACEHOLDER_POINT + file.createdBy.displayName
        }

        with(viewBinding) {
            listExplorerFileName.text = file.title
            listExplorerFileInfo.text = filesInfo
            listExplorerFileContext.visibility = View.VISIBLE

            viewIconSelectableLayout.viewIconSelectableImage.background = null
            viewIconSelectableLayout.viewIconSelectableMask.background = null
            adapter.setFileIcon(viewIconSelectableLayout.viewIconSelectableImage, file.fileExst)

            // For selection mode add background/foreground
            if (adapter.isSelectMode) {
                listExplorerFileContext.setVisibility(View.GONE)
                if (file.isSelected) {
                    viewIconSelectableLayout.viewIconSelectableMask.setBackgroundResource(R.drawable.drawable_list_image_select_mask)
                } else {
                    viewIconSelectableLayout.viewIconSelectableLayout
                        .setBackgroundResource(R.drawable.drawable_list_image_select_background)
                }
            }
        }

    }

    companion object {
        const val LAYOUT: Int = R.layout.list_explorer_files
    }
}