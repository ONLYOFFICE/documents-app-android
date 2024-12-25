package app.editors.manager.ui.adapters.holders.explorer

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import app.documents.core.network.manager.models.explorer.CloudFolder
import app.editors.manager.R
import app.editors.manager.databinding.LayoutExplorerGridFolderBinding
import app.editors.manager.ui.adapters.ExplorerAdapter

class GridFolderViewHolder(view: View, adapter: ExplorerAdapter) :
    GridBaseViewHolder<CloudFolder>(view, adapter) {

    companion object {

        val LAYOUT: Int = R.layout.layout_explorer_grid_folder
    }

    private val binding: LayoutExplorerGridFolderBinding =
        LayoutExplorerGridFolderBinding.bind(view)

    override val root: View
        get() = binding.root

    override val rootLayout: CardView
        get() = binding.cardRootLayout

    override val selectIcon: ImageView
        get() = binding.selectIcon

    override val title: TextView
        get() = binding.title

    override val subtitle: TextView
        get() = binding.subtitle

    override fun bind(element: CloudFolder) {
        super.bind(element)
        bindFolderType(element)
        bindFolderStorageImage(element, binding.storageImage)
    }

    override fun getCachedIcon(): View {
        return binding.imageLayout
    }

    private fun bindFolderType(folder: CloudFolder) {
        binding.image.setImageResource(
            when (folder.type) {
                26 -> R.drawable.ic_folder_column_in_process
                25 -> R.drawable.ic_folder_column_complete
                else -> R.drawable.ic_folder_column
            }
        )
    }
}