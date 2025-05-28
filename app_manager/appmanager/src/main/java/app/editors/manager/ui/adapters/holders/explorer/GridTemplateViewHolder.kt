package app.editors.manager.ui.adapters.holders.explorer

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import app.documents.core.network.manager.models.explorer.CloudFolder
import app.editors.manager.R
import app.editors.manager.databinding.LayoutExplorerGridTemplateBinding
import app.editors.manager.managers.utils.ManagerUiUtils.setTemplateIcon
import app.editors.manager.managers.utils.RoomUtils
import app.editors.manager.ui.adapters.ExplorerAdapter

class GridTemplateViewHolder(view: View, adapter: ExplorerAdapter) :
    GridBaseViewHolder<CloudFolder>(view, adapter) {

    private val binding = LayoutExplorerGridTemplateBinding.bind(view)

    override val root: View
        get() = binding.root

    override val rootLayout: CardView
        get() = binding.cardRootLayout

    override val title: TextView
        get() = binding.title

    override val subtitle: TextView
        get() = binding.subtitle

    override fun bind(element: CloudFolder) {
        super.bind(element)
        with(binding) {
            cardText.text = RoomUtils.getRoomInitials(element.title)
            cardImageLayout.setTemplateIcon(
                template = element,
                image = cardImage,
                text = cardText,
                isGrid = adapter.isGridView
            )
        }
    }

    override fun getCachedIcon(): View {
        return binding.cardImageLayout
    }

    override val selectIcon: ImageView
        get() = binding.selectIcon

    companion object {
        val LAYOUT: Int = R.layout.layout_explorer_grid_template
    }
}