package app.editors.manager.ui.adapters.holders

import android.view.View
import androidx.work.WorkManager
import app.editors.manager.R
import app.editors.manager.databinding.ListExplorerUploadFilesBinding
import app.editors.manager.managers.utils.ManagerUiUtils.setFileIcon
import app.documents.core.network.manager.models.explorer.UploadFile
import app.editors.manager.ui.adapters.ExplorerAdapter
import lib.toolkit.base.managers.utils.StringUtils
import java.util.*

class UploadFileViewHolder(itemView: View, adapter: ExplorerAdapter) :
    BaseViewHolderExplorer<UploadFile>(itemView, adapter) {

    private var file: UploadFile? = null
    private var viewBinding = ListExplorerUploadFilesBinding.bind(itemView)

    init {
        viewBinding.listExplorerUploadFileCancel.setOnClickListener {
            WorkManager.getInstance().cancelAllWorkByTag(file?.uri.toString())
        }
    }

    override fun bind(file: UploadFile) {
        with(viewBinding) {
            this@UploadFileViewHolder.file = file
            if (file.progress == 0) {
                uploadFileProgressBar.progress = 0
                listExplorerUploadFileProgress.setText(R.string.upload_manager_waiting_title)
            } else {
                updateProgress(file)
            }
            listExplorerUploadFileName.text = file.name
            viewIconSelectableLayout.viewIconSelectableLayout.background = null
            viewIconSelectableLayout.viewIconSelectableMask.background = null
            file.uri?.path?.let { path ->
                viewIconSelectableLayout.viewIconSelectableImage.setFileIcon(StringUtils.getExtensionFromPath(path))
            }
        }
    }

    fun updateProgress(file: UploadFile) {
        val fileProgress = "${getFileProgress(file)} / ${file.size}"
        viewBinding.listExplorerUploadFileProgress.text = fileProgress
        viewBinding.uploadFileProgressBar.progress = file.progress
    }

    private fun getFileProgress(file: UploadFile): String {
        val stringSize: String = file.size?.substring(0, file.size?.indexOf(" ") ?: 0).orEmpty()
        val total = stringSize.replace(',', '.').toDouble()
        val kof = total / 100
        val progressSize: Double = kof * file.progress
        return String.format(Locale.getDefault(), "%.2f", progressSize)
    }

    companion object {
        const val LAYOUT: Int = R.layout.list_explorer_upload_files
    }
}