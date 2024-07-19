package app.editors.manager.ui.adapters.holders.explorer

import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import app.documents.core.network.manager.models.explorer.CloudFolder
import app.editors.manager.R
import app.editors.manager.databinding.LayoutExplorerListRoomBinding
import app.editors.manager.managers.utils.ManagerUiUtils.setRoomIcon
import app.editors.manager.ui.adapters.ExplorerAdapter

class ListRoomViewHolder(view: View, adapter: ExplorerAdapter) :
    ListBaseViewHolder<CloudFolder>(view, adapter) {

    private val binding = LayoutExplorerListRoomBinding.bind(view)

    override val rootLayout: ConstraintLayout
        get() = binding.constraintRootLayout

    override val title: TextView
        get() = binding.title

    override val subtitle: TextView
        get() = binding.subtitle

    override val contextButton: Button
        get() = binding.contextButton

    override val selectIcon: ImageView
        get() = binding.selectIcon

    override fun bind(element: CloudFolder) {
        super.bind(element)
        with(binding) {
            iconPinned.isVisible = element.pinned
            cardImageLayout.setRoomIcon(element, cardImage, cardText, badge, adapter.isGridView)
        }
    }

    override fun getCachedIcon(): View {
        return binding.cardImageLayout
    }

    companion object {

        val LAYOUT: Int = R.layout.layout_explorer_list_room
    }
}