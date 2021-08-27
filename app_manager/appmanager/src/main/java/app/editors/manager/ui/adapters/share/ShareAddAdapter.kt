package app.editors.manager.ui.adapters.share

import android.content.Context
import app.editors.manager.app.Api
import app.editors.manager.app.App.Companion.getApp
import app.editors.manager.app.api
import app.editors.manager.ui.adapters.BaseViewTypeAdapter
import app.editors.manager.ui.adapters.holders.factory.ShareAddHolderFactory
import lib.toolkit.base.ui.adapters.BaseAdapter
import lib.toolkit.base.ui.adapters.holder.BaseViewHolder
import lib.toolkit.base.ui.adapters.holder.ViewType
import javax.inject.Inject

//TODO make divided viewHolder and load avatars, make filter by api args
class ShareAddAdapter(factory: ShareAddHolderFactory)
    : BaseViewTypeAdapter<ViewType>(factory) {

    @Inject
    lateinit var context: Context

    private var mode: BaseAdapter.Mode
    private var api: Api? = null

    init {
        getApp().appComponent.inject(this)
        mode = BaseAdapter.Mode.USERS
        api = context.api()
    }

    override fun onBindViewHolder(holder: BaseViewHolder<ViewType>, position: Int) {
        super.onBindViewHolder(holder, position)
        holder.bind(getItem(position)!!, mode, getItem(position - 1))
    }

    fun setMode(mode: BaseAdapter.Mode) {
        this.mode = mode
    }
}