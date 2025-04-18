package app.editors.manager.ui.dialogs.explorer

import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import app.documents.core.network.manager.models.explorer.CloudFile
import app.editors.manager.R
import app.editors.manager.databinding.ListExplorerContextHeaderBinding
import app.editors.manager.databinding.ListExplorerContextItemBinding
import app.editors.manager.mvp.models.ui.UiFormFillingStatus
import app.editors.manager.ui.adapters.base.BaseAdapter
import app.editors.manager.ui.views.badge.setFormStatus
import lib.toolkit.base.managers.extensions.inflate
import lib.toolkit.base.managers.utils.TimeUtils

class ExplorerContextAdapter(
    private val onClickListener: (ExplorerContextItem) -> Unit,
) : BaseAdapter<ExplorerContextItem>() {

    companion object {
        private const val HEADER_VIEW_TYPE = 0
        private const val ITEM_VIEW_TYPE = 1
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            HEADER_VIEW_TYPE -> ExplorerContextHeaderViewHolder(parent.inflate(R.layout.list_explorer_context_header))
            else -> ExplorerContextItemViewHolder(parent.inflate(R.layout.list_explorer_context_item))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = itemList[position]
        when (holder) {
            is ExplorerContextHeaderViewHolder -> if (item is ExplorerContextItem.Header) holder.bind(item)
            is ExplorerContextItemViewHolder -> {
                holder.itemView.setOnClickListener { onClickListener.invoke(itemList[position]) }
                holder.bind(item)
            }
        }
    }

    override fun getItemViewType(position: Int): Int = when (itemList[position]) {
        is ExplorerContextItem.Header -> HEADER_VIEW_TYPE
        else -> ITEM_VIEW_TYPE
    }

    private inner class ExplorerContextHeaderViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        private val viewBinding = ListExplorerContextHeaderBinding.bind(view)

        fun bind(header: ExplorerContextItem.Header) {
            with(viewBinding) {
                icon.setImageBitmap(header.state.icon)
                title.text = header.state.item.title
                subtitle.text = header.info
                val file = header.state.item as? CloudFile
                file?.let {
                    badgeFormStatus.setFormStatus(UiFormFillingStatus.from(file.formFillingStatus))
                    if (file.expired != null) {
                        info.isVisible = true
                        info.text = info.context.getString(
                            R.string.rooms_expire_context_info,
                            TimeUtils.formatDate(header.state.item.expired)
                        )
                    }
                }
            }
        }
    }

    private inner class ExplorerContextItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        private val viewBinding = ListExplorerContextItemBinding.bind(view)

        fun bind(item: ExplorerContextItem) {
            with(viewBinding) {
                if (item is ExplorerContextItem.Favorites && item.favorite) icon.imageTintList = null
                icon.setImageResource(item.icon)
                title.setText(item.title)
                divider.root.isVisible = dividerVisible(item)
            }
        }

        private fun dividerVisible(item: ExplorerContextItem) =
            if (bindingAdapterPosition < itemList.size - 1) {
                item.order != itemList[bindingAdapterPosition + 1].order
            } else false

    }

}