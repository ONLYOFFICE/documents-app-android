package app.editors.manager.ui.adapters.holders

import android.view.View
import lib.toolkit.base.ui.adapters.BaseAdapter
import lib.toolkit.base.ui.adapters.holder.BaseViewHolder
import lib.toolkit.base.ui.adapters.holder.ViewType

open class ShareViewHolder<T: ViewType>(view: View) : BaseViewHolder<T>(view) {

    open fun bind(item: T, mode: BaseAdapter.Mode, previousItem: T?) = Unit
}