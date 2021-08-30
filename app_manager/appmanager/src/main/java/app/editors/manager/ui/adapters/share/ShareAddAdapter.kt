package app.editors.manager.ui.adapters.share

import app.editors.manager.app.App.Companion.getApp
import app.editors.manager.ui.adapters.BaseViewTypeAdapter
import app.editors.manager.ui.adapters.holders.ShareViewHolder
import app.editors.manager.ui.adapters.holders.factory.ShareHolderFactory
import lib.toolkit.base.ui.adapters.BaseAdapter
import lib.toolkit.base.ui.adapters.holder.BaseViewHolder
import lib.toolkit.base.ui.adapters.holder.ViewType

class ShareAddAdapter(factory: ShareHolderFactory) : BaseViewTypeAdapter<ViewType>(factory) {

    private var mode: BaseAdapter.Mode

    init {
        getApp().appComponent.inject(this)
        mode = BaseAdapter.Mode.USERS
    }

    override fun onBindViewHolder(holder: BaseViewHolder<ViewType>, position: Int) {
        super.onBindViewHolder(holder, position)
        getItem(position)?.let { item ->
            (holder as ShareViewHolder<ViewType>).bind(item, mode, getItem(position - 1))
        }
    }

    fun setMode(mode: BaseAdapter.Mode) {
        this.mode = mode
    }
}