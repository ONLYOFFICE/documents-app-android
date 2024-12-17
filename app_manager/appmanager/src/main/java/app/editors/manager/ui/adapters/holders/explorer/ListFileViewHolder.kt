package app.editors.manager.ui.adapters.holders.explorer

import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import app.documents.core.network.manager.models.explorer.CloudFile
import app.editors.manager.R
import app.editors.manager.databinding.LayoutExplorerListFileBinding
import app.editors.manager.managers.utils.ManagerUiUtils
import app.editors.manager.mvp.presenters.main.PickerMode
import app.editors.manager.ui.adapters.ExplorerAdapter

class ListFileViewHolder(itemView: View, adapter: ExplorerAdapter) :
    ListBaseViewHolder<CloudFile>(itemView, adapter) {

    private val binding = LayoutExplorerListFileBinding.bind(itemView)

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

    override val selectIcon: ImageView
        get() = binding.selectIcon

    override fun getCachedIcon(): View {
        return binding.image
    }

    override fun bind(element: CloudFile) {
        super.bind(element)
        binding.image.setImageResource(ManagerUiUtils.getFileThumbnail(element.fileExst, false))
        binding.favorite.isVisible = element.isFavorite
        binding.badgeNewCard.isVisible = element.isNew
        binding.editing.isVisible = element.isEditing
        setFileExpiring(element, binding.title)
        if (adapter.pickerMode == PickerMode.Ordering) {
            contextButton.isVisible = false
            binding.dragIcon.isVisible = true
        }
    }

    companion object {

        val LAYOUT: Int = R.layout.layout_explorer_list_file
    }
}