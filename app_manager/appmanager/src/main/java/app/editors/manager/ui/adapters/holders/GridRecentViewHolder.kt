package app.editors.manager.ui.adapters.holders

import android.view.View
import android.widget.TableLayout
import android.widget.TableLayout.LayoutParams.WRAP_CONTENT
import androidx.core.view.isVisible
import app.documents.core.model.cloud.Recent
import app.editors.manager.R
import app.editors.manager.databinding.LayoutExplorerGridFileBinding
import app.editors.manager.databinding.ListExplorerFilesBinding
import app.editors.manager.managers.utils.StringUtils
import app.editors.manager.mvp.models.ui.RecentUI
import app.editors.manager.mvp.models.ui.toRecent
import com.facebook.appevents.codeless.internal.ViewHierarchy.setOnClickListener
import lib.toolkit.base.managers.utils.StringUtils.getExtensionFromPath
import lib.toolkit.base.ui.adapters.holder.BaseViewHolder
import lib.toolkit.base.ui.adapters.holder.ViewType
import java.util.Locale

class GridRecentViewHolder(
    view: View,
    private val itemListener: ((recent: Recent) -> Unit)? = null,
    private val contextListener: ((recent: Recent, position: Int) -> Unit)? = null
) : BaseViewHolder<ViewType>(view) {

    private val viewBinding = LayoutExplorerGridFileBinding.bind(view)

    override fun bind(item: ViewType) {
        if (item is RecentUI) {
            with(viewBinding) {
                title.post {
                    if (title.lineCount > 1) {
                        title.layoutParams = TableLayout.LayoutParams(0, WRAP_CONTENT, 1f)
                    }
                }
                title.text = item.name
                subtitle.text = item.source ?: view.context.getString(R.string.this_device)
                icon.setIconFromExtension(getExtensionFromPath(item.name.lowercase(Locale.ROOT)), true)
                root.setOnClickListener { itemListener?.invoke(item.toRecent()) }
            }
        }
    }
}