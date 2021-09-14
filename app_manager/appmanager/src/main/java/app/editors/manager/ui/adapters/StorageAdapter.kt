package app.editors.manager.ui.adapters

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import app.editors.manager.R
import app.editors.manager.ui.adapters.base.BaseAdapter
import app.editors.manager.ui.adapters.holders.StorageViewHolder
import lib.toolkit.base.managers.extensions.inflate

class StorageAdapter : BaseAdapter<String>() {
    override fun onCreateViewHolder(viewGroup: ViewGroup, typeHolder: Int): RecyclerView.ViewHolder =
        StorageViewHolder(viewGroup.inflate(R.layout.list_storage_select_item), mOnItemClickListener)

    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {
        if(viewHolder is StorageViewHolder) {
            getItem(position)?.let { item ->
                viewHolder.bind(item)
            }
        }
    }
}