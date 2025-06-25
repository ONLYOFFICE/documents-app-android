package app.editors.manager.ui.adapters.holders.explorer

import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import app.documents.core.network.manager.models.explorer.CloudFolder
import app.editors.manager.R
import app.editors.manager.databinding.LayoutExplorerListTemplateBinding
import app.editors.manager.managers.utils.ManagerUiUtils.setTemplateIcon
import app.editors.manager.managers.utils.RoomUtils
import app.editors.manager.ui.adapters.ExplorerAdapter

class ListTemplateViewHolder(view: View, adapter: ExplorerAdapter) :
    ListBaseViewHolder<CloudFolder>(view, adapter) {

    private val binding = LayoutExplorerListTemplateBinding.bind(view)

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

    companion object {
        val LAYOUT: Int = R.layout.layout_explorer_list_template
    }
}