package app.editors.manager.ui.adapters.holders.explorer

import android.view.View
import androidx.core.view.isVisible
import app.documents.core.model.cloud.Recent
import app.editors.manager.R
import app.editors.manager.databinding.LayoutExplorerListFileBinding
import app.editors.manager.managers.utils.ManagerUiUtils
import app.editors.manager.mvp.models.ui.RecentUI
import app.editors.manager.mvp.models.ui.toRecent
import lib.toolkit.base.managers.utils.StringUtils.getExtensionFromPath
import lib.toolkit.base.ui.adapters.holder.BaseViewHolder
import lib.toolkit.base.ui.adapters.holder.ViewType
import java.util.Locale

class ListRecentViewHolder(
    view: View,
    private val itemListener: ((recent: Recent) -> Unit)? = null,
    private val contextListener: ((recent: Recent, position: Int) -> Unit)? = null
) : BaseViewHolder<ViewType>(view) {

    private val viewBinding = LayoutExplorerListFileBinding.bind(view)

    override fun bind(item: ViewType) {
        if (item is RecentUI) {
            with(viewBinding) {
                val info = item.source ?: view.context.getString(R.string.this_device)

                title.text = item.name
                subtitle.text = info
                subtitle.isVisible = info.isNotEmpty()
                favorite.isVisible = false
                root.setOnClickListener {
                    itemListener?.invoke(item.toRecent())
                }
                contextButton.setOnClickListener {
                    contextListener?.invoke(item.toRecent(), absoluteAdapterPosition)
                }
                image.setImageResource(
                    ManagerUiUtils.getFileThumbnail(
                        getExtensionFromPath(item.name.lowercase(Locale.ROOT))
                    )
                )
            }
        }
    }
}