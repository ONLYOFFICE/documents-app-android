package app.editors.manager.ui.adapters.holders

import android.view.View
import app.editors.manager.mvp.models.list.Header
import lib.toolkit.base.databinding.ListItemHeaderBinding
import lib.toolkit.base.ui.adapters.holder.BaseViewHolder
import lib.toolkit.base.ui.adapters.holder.ViewType

class RecentHeaderViewHolder(view: View) : BaseViewHolder<ViewType>(view) {

    override fun bind(item: ViewType) {
        if (item is Header) {
            with(ListItemHeaderBinding.bind(itemView)) {
                title.text = item.title
            }
        }
    }
}