package app.editors.manager.ui.adapters

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import app.editors.manager.R
import app.editors.manager.mvp.models.user.Thirdparty
import app.editors.manager.ui.adapters.holders.ThirdpartyViewHolder
import lib.toolkit.base.ui.adapters.BaseListAdapter
import lib.toolkit.base.ui.adapters.factory.inflate

class ThirdpartyAdapter : BaseListAdapter<Thirdparty>() {
    override fun onCreateViewHolder(view: ViewGroup, type: Int): RecyclerView.ViewHolder =
        ThirdpartyViewHolder(view.inflate(R.layout.thirdparty_item_layout))

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is ThirdpartyViewHolder) {
            mList[position].let { item ->
                holder.bind(item)
            }
        }
    }
}