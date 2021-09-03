package app.editors.manager.ui.adapters

import app.editors.manager.mvp.models.ui.ShareHeaderUi
import app.editors.manager.ui.adapters.base.BaseViewTypeAdapter
import app.editors.manager.ui.adapters.holders.ShareViewHolder
import app.editors.manager.ui.adapters.holders.factory.ShareHolderFactory
import lib.toolkit.base.ui.adapters.BaseAdapter
import lib.toolkit.base.ui.adapters.holder.BaseViewHolder
import lib.toolkit.base.ui.adapters.holder.ViewType

class ShareAdapter(factory: ShareHolderFactory) : BaseViewTypeAdapter<ViewType>(factory) {

    private var mode: BaseAdapter.Mode = BaseAdapter.Mode.USERS

    override fun onBindViewHolder(holder: BaseViewHolder<ViewType>, position: Int) {
        if (holder is ShareViewHolder<ViewType>) {
            getItem(position)?.let { item ->
                holder.bind(item, mode, getItem(position - 1))
            }
        } else {
            super.onBindViewHolder(holder, position)
        }
    }

    fun setMode(mode: BaseAdapter.Mode) {
        this.mode = mode
    }

    fun removeHeader(item: ShareHeaderUi) {
        removeItem(item)
    }
}