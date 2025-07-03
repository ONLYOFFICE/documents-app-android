package app.editors.manager.ui.adapters.holders.explorer

import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import app.documents.core.network.common.contracts.ApiContract
import app.documents.core.network.manager.models.explorer.CloudFolder
import app.editors.manager.R
import app.editors.manager.databinding.LayoutExplorerListFolderBinding
import app.editors.manager.mvp.presenters.main.PickerMode
import app.editors.manager.ui.adapters.ExplorerAdapter

class ListFolderViewHolder(view: View, adapter: ExplorerAdapter) :
    ListBaseViewHolder<CloudFolder>(view, adapter) {

    private val binding = LayoutExplorerListFolderBinding.bind(view)

    override val root: View
        get() = binding.root

    override val rootLayout: ConstraintLayout
        get() = binding.constraintRootLayout

    override val title: TextView
        get() = binding.title

    override val subtitle: TextView
        get() = binding.subtitle

    override val contextButton: Button
        get() = binding.contextButton

    override val contextButtonLayout: ViewGroup
        get() = binding.contextButtonLayout

    override val selectIcon: ImageView
        get() = binding.selectIcon

    override fun bind(element: CloudFolder) {
        super.bind(element)
        bindFolderType(element)
        bindFolderStorageImage(element, binding.storageImage)
        if (adapter.pickerMode == PickerMode.Ordering) {
            initOrderingMode(binding.dragIcon, binding.contextButtonLayout)
        }
    }

    override fun getCachedIcon(): View {
        return binding.imageLayout
    }

    private fun bindFolderType(folder: CloudFolder) {
        binding.image.setImageResource(
            when (folder.type) {
                ApiContract.SectionType.IN_PROCESS_FORM_FOLDER -> R.drawable.ic_folder_list_in_process
                ApiContract.SectionType.READY_FORM_FOLDER -> R.drawable.ic_folder_list_complete
                else -> R.drawable.ic_folder_list
            }
        )
    }

    companion object {

        val LAYOUT: Int = R.layout.layout_explorer_list_folder
    }
}