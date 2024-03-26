package app.editors.manager.ui.adapters.holders

import android.view.View
import androidx.core.view.isVisible
import app.documents.core.model.cloud.Recent
import app.editors.manager.R
import app.editors.manager.databinding.ListExplorerFilesBinding
import app.editors.manager.mvp.models.ui.RecentUI
import app.editors.manager.mvp.models.ui.toRecent
import lib.toolkit.base.managers.utils.StringUtils.getExtensionFromPath
import lib.toolkit.base.ui.adapters.holder.BaseViewHolder
import lib.toolkit.base.ui.adapters.holder.ViewType
import java.util.Locale

class RecentViewHolder(
    view: View,
    private val itemListener: ((recent: Recent) -> Unit)? = null,
    private val contextListener: ((recent: Recent, position: Int) -> Unit)? = null
) : BaseViewHolder<ViewType>(view) {

    private val viewBinding = ListExplorerFilesBinding.bind(view)

    override fun bind(item: ViewType) {
        if (item is RecentUI) {
            with(viewBinding) {
                val info = item.source ?: view.context.getString(R.string.this_device)

                listExplorerFileName.text = item.name
                listExplorerFileInfo.text = info
                listExplorerFileInfo.isVisible = info.isNotEmpty()
                listExplorerFileFavorite.isVisible = false
                listExplorerFileLayout.setOnClickListener {
                    itemListener?.invoke(item.toRecent())
                }
                listExplorerFileContext.setOnClickListener {
                    contextListener?.invoke(item.toRecent(), absoluteAdapterPosition)
                }
                viewIconSelectableLayout.setIconFromExtension(getExtensionFromPath(item.name.lowercase(Locale.ROOT)))
            }
        }
    }
}