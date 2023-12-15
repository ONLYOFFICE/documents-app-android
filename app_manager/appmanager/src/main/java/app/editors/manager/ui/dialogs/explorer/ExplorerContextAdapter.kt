package app.editors.manager.ui.dialogs.explorer

import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import app.editors.manager.R
import app.editors.manager.databinding.ListExplorerContextHeaderBinding
import app.editors.manager.databinding.ListExplorerContextItemBinding
import app.editors.manager.ui.adapters.base.BaseAdapter
import lib.toolkit.base.managers.extensions.inflate

class ExplorerContextAdapter(
    private val onClickListener: ExplorerContextBottomDialog.OnClickListener?
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
                holder.itemView.setOnClickListener { onClickListener?.onContextButtonClick(itemList[position]) }
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
                icon.item = header.state.item
                title.text = header.state.item.title
                subtitle.text = header.info
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