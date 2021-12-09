package app.editors.manager.ui.adapters.holders

import android.annotation.SuppressLint
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import app.documents.core.account.Recent
import app.editors.manager.R
import app.editors.manager.app.App
import app.editors.manager.databinding.ListExplorerFilesBinding
import app.editors.manager.managers.utils.ManagerUiUtils
import app.editors.manager.managers.utils.isVisible
import lib.toolkit.base.managers.utils.StringUtils.getExtensionFromPath
import lib.toolkit.base.managers.utils.StringUtils.getFormattedSize
import lib.toolkit.base.managers.utils.TimeUtils.getWeekDate
import java.util.*

class RecentViewHolder(
    private val view: View,
    private val itemListener: ((recent: Recent, position: Int) -> Unit)? = null,
    private val contextListener: ((recent: Recent, position: Int) -> Unit)? = null
) :
    RecyclerView.ViewHolder(view) {

    private val viewBinding = ListExplorerFilesBinding.bind(view)

    @SuppressLint("SetTextI18n")
    fun bind(recent: Recent) {
        with(viewBinding) {
            val info = getWeekDate(Date(recent.date)) + App.getApp()
                .getString(R.string.placeholder_point) +
                    getFormattedSize(view.context, recent.size)
            listExplorerFileName.text = recent.name
            listExplorerFileInfo.text = info
            listExplorerFileFavorite.isVisible = false
            listExplorerFileLayout.setOnClickListener {
                itemListener?.invoke(recent, absoluteAdapterPosition)
            }
            listExplorerFileContext.setOnClickListener {
                contextListener?.invoke(recent, absoluteAdapterPosition)
            }
            ManagerUiUtils.setFileIcon(viewIconSelectableLayout.viewIconSelectableImage,
                getExtensionFromPath(recent.name.lowercase(Locale.ROOT)))
        }
    }
}