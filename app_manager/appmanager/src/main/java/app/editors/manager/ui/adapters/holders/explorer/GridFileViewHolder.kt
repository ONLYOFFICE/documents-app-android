package app.editors.manager.ui.adapters.holders.explorer

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.view.isVisible
import app.documents.core.network.common.contracts.ApiContract
import app.documents.core.network.common.contracts.ApiContract.SectionType.shouldShowShareBadge
import app.documents.core.network.manager.models.explorer.CloudFile
import app.editors.manager.R
import app.editors.manager.databinding.LayoutExplorerGridFileBinding
import app.editors.manager.managers.utils.GlideUtils.setFileThumbnailFromUrl
import app.editors.manager.managers.utils.ManagerUiUtils
import app.editors.manager.mvp.models.ui.UiFormFillingStatus
import app.editors.manager.ui.adapters.ExplorerAdapter
import app.editors.manager.ui.views.badge.setFormStatus


class GridFileViewHolder(view: View, adapter: ExplorerAdapter) :
    GridBaseViewHolder<CloudFile>(view, adapter) {

    companion object {

        val LAYOUT: Int = R.layout.layout_explorer_grid_file
    }

    private val binding: LayoutExplorerGridFileBinding = LayoutExplorerGridFileBinding.bind(view)

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

    override fun getCachedIcon(): View {
        return binding.image
    }

    override fun bind(element: CloudFile) {
        super.bind(element)
        bindThumbnail(element)
        binding.favorite.isVisible = element.isFavorite
        binding.badgeNewCard.isVisible = element.isNew
        binding.customFilter.isVisible = element.customFilterEnabled
        binding.badgeVersionCard.isVisible = element.version > 1
        if (binding.badgeVersionCard.isVisible) {
            binding.badgeVersion.text =
                itemView.context.getString(R.string.badge_doc_version, element.version)
        }
        binding.editing.isVisible = element.isEditing
        binding.link.isVisible = element.isSharedByLink
                || element.shared && shouldShowShareBadge(element.parentRoomType)
        binding.badgeFormStatus.setFormStatus(UiFormFillingStatus.from(element.formFillingStatus))
        setFileExpiring(element, binding.title)
    }

    fun bindThumbnail(element: CloudFile) {
        binding.fileTypeBadge.isVisible = false
        when (element.thumbnailStatus) {
            ApiContract.ThumbnailStatus.WAITING, ApiContract.ThumbnailStatus.CREATING -> {
                binding.image.isVisible = false
                binding.previewLoading.isVisible = true
            }

            ApiContract.ThumbnailStatus.ERROR, ApiContract.ThumbnailStatus.NOT_REQUIRED -> {
                binding.image.setImageResource(
                    ManagerUiUtils.getFileThumbnail(
                        element.fileExst,
                        true
                    )
                )
                binding.image.isVisible = true
                binding.previewLoading.isVisible = false
            }

            ApiContract.ThumbnailStatus.CREATED -> {
                binding.image.setFileThumbnailFromUrl(element.thumbnailUrl, element.fileExst)
                val fileTypeBadgeRes = ManagerUiUtils.getFileBadge(element.fileExst)
                if (fileTypeBadgeRes != 0) {
                    binding.fileTypeBadge.setImageResource(fileTypeBadgeRes)
                    binding.fileTypeBadge.isVisible = true
                }
                binding.image.isVisible = true
                binding.previewLoading.isVisible = false
            }
        }

    }
}