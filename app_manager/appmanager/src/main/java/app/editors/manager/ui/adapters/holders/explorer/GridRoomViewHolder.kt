package app.editors.manager.ui.adapters.holders.explorer

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.view.isVisible
import app.documents.core.network.manager.models.explorer.CloudFolder
import app.editors.manager.R
import app.editors.manager.databinding.LayoutExplorerGridRoomBinding
import app.editors.manager.managers.utils.ManagerUiUtils.setRoomIcon
import app.editors.manager.managers.utils.RoomUtils
import app.editors.manager.ui.adapters.ExplorerAdapter

class GridRoomViewHolder(view: View, adapter: ExplorerAdapter) :
    GridBaseViewHolder<CloudFolder>(view, adapter) {

    companion object {

        val LAYOUT: Int = R.layout.layout_explorer_grid_room
    }

    private val binding: LayoutExplorerGridRoomBinding = LayoutExplorerGridRoomBinding.bind(view)

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
            iconPinned.isVisible = element.pinned
            cardImageLayout.setRoomIcon(element, cardImage, cardText, badge, adapter.isGridView)
            if (element.newCount > 0) {
                binding.badgeNew.isVisible = true
                binding.badgeNew.number = element.newCount
            }
        }
    }

    override fun getCachedIcon(): View {
        return binding.cardImageLayout
    }

    override val selectIcon: ImageView
        get() = binding.selectIcon

}