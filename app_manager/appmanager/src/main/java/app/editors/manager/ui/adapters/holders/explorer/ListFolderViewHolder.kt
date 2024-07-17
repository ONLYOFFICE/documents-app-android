package app.editors.manager.ui.adapters.holders.explorer

import android.graphics.Bitmap
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.drawToBitmap
import androidx.core.view.isVisible
import app.documents.core.network.manager.models.explorer.CloudFolder
import app.editors.manager.R
import app.editors.manager.databinding.LayoutExplorerListFolderBinding
import app.editors.manager.managers.utils.StorageUtils
import app.editors.manager.ui.adapters.ExplorerAdapter

class ListFolderViewHolder(view: View, adapter: ExplorerAdapter) :
    ListBaseViewHolder<CloudFolder>(view, adapter) {

    private val binding = LayoutExplorerListFolderBinding.bind(view)

    override val rootLayout: ConstraintLayout
        get() = binding.constraintRootLayout

    override val title: TextView
        get() = binding.title

    override val subtitle: TextView
        get() = binding.subtitle

    override val contextButton: Button
        get() = binding.contextButton

    override fun bind(element: CloudFolder) {
        super.bind(element)
        if (element.providerItem && element.providerKey.isNotEmpty() && adapter.isRoot) {
            binding.overlayImage.isVisible = true
            binding.overlayImage.setImageResource(StorageUtils.getStorageIcon(element.providerKey))
        } else if (element.shared) {
            binding.overlayImage.isVisible = true
            binding.overlayImage.setImageResource(R.drawable.ic_list_item_share_user_icon_secondary)
        } else {
            binding.overlayImage.isVisible = false
        }
    }

    override fun getCachedIcon(): View {
        return binding.imageLayout
    }

    companion object {

        val LAYOUT: Int = R.layout.layout_explorer_list_folder
    }
}