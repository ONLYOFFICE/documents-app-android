package app.editors.manager.ui.adapters.holders

import android.view.View
import app.editors.manager.databinding.ListExplorerHeaderBinding
import app.editors.manager.mvp.models.list.Header
import lib.toolkit.base.ui.adapters.holder.BaseViewHolder
import lib.toolkit.base.ui.adapters.holder.ViewType

class RecentHeaderViewHolder(view: View) : BaseViewHolder<ViewType>(view) {
    private val viewBinding = ListExplorerHeaderBinding.bind(view)

    override fun bind(item: ViewType) {
        if (item is Header) {
            viewBinding.listExplorerHeaderTitle.text = item.title
        }
    }
}