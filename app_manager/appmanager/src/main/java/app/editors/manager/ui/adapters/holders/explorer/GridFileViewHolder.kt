package app.editors.manager.ui.adapters.holders.explorer

import android.graphics.Bitmap
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.view.drawToBitmap
import androidx.core.view.isVisible
import app.documents.core.network.manager.models.explorer.CloudFile
import app.editors.manager.R
import app.editors.manager.databinding.LayoutExplorerGridFileBinding
import app.editors.manager.managers.utils.ManagerUiUtils
import app.editors.manager.ui.adapters.ExplorerAdapter


class GridFileViewHolder(view: View, adapter: ExplorerAdapter) :
    GridBaseViewHolder<CloudFile>(view, adapter) {

    companion object {

        val LAYOUT: Int = R.layout.layout_explorer_grid_file
    }

    private val binding: LayoutExplorerGridFileBinding = LayoutExplorerGridFileBinding.bind(view)

    override val rootLayout: CardView
        get() = binding.cardRootLayout

    override val selectIcon: ImageView
        get() = binding.selectIcon

    override val title: TextView
        get() = binding.title

    override val subtitle: TextView
        get() = binding.subtitle

    override fun getCachedIcon(): View {
        return binding.image
    }

    override fun bind(element: CloudFile) {
        super.bind(element)
        binding.image.setImageResource(ManagerUiUtils.getFileIcon(element.fileExst))
        binding.favorite.isVisible = element.favorite
    }
}