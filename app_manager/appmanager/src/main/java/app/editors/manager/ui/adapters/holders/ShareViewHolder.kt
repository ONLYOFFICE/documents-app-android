package app.editors.manager.ui.adapters.holders

import android.view.View
import lib.toolkit.base.ui.adapters.BaseAdapter
import lib.toolkit.base.ui.adapters.holder.BaseViewHolder
import lib.toolkit.base.ui.adapters.holder.ViewType

open class ShareViewHolder(view: View) : BaseViewHolder<ViewType>(view) {

    open fun bind(item: ViewType, mode: BaseAdapter.Mode, previousItem: ViewType?) = Unit
}