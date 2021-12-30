package app.editors.manager.ui.adapters.holders

import android.view.View
import app.documents.core.account.Recent
import app.editors.manager.databinding.ListExplorerFilesBinding
import app.editors.manager.managers.utils.ManagerUiUtils
import app.editors.manager.managers.utils.isVisible
import app.editors.manager.mvp.models.ui.RecentUI
import app.editors.manager.mvp.models.ui.toRecent
import lib.toolkit.base.managers.utils.StringUtils.getExtensionFromPath
import lib.toolkit.base.ui.adapters.holder.BaseViewHolder
import lib.toolkit.base.ui.adapters.holder.ViewType
import java.util.*

class RecentViewHolder(
    view: View,
    private val itemListener: ((recent: Recent, position: Int) -> Unit)? = null,
    private val contextListener: ((recent: Recent, position: Int) -> Unit)? = null
) : BaseViewHolder<ViewType>(view) {

    private val viewBinding = ListExplorerFilesBinding.bind(view)

    override fun bind(item: ViewType) {
        if (item is RecentUI) {
            with(viewBinding) {
                val info = when {
                    item.isLocal -> "This device"
                    else -> item.source
                }

                listExplorerFileName.text = item.name
                listExplorerFileInfo.text = info
                listExplorerFileInfo.isVisible = !info.isNullOrEmpty()
                listExplorerFileFavorite.isVisible = false
                listExplorerFileLayout.setOnClickListener {
                    itemListener?.invoke(item.toRecent(), absoluteAdapterPosition)
                }
                listExplorerFileContext.setOnClickListener {
                    contextListener?.invoke(item.toRecent(), absoluteAdapterPosition)
                }
                ManagerUiUtils.setFileIcon(viewIconSelectableLayout.viewIconSelectableImage,
                    getExtensionFromPath(item.name.lowercase(Locale.ROOT)))
            }
        }
    }
}