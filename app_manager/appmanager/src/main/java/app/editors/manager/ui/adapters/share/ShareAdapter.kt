package app.editors.manager.ui.adapters.share

import android.view.InflateException
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import app.editors.manager.R
import app.editors.manager.mvp.models.ui.ShareHeaderUi
import app.editors.manager.mvp.models.ui.ShareUi
import app.editors.manager.ui.adapters.base.BaseAdapter
import lib.toolkit.base.managers.extensions.inflate
import lib.toolkit.base.ui.adapters.holder.ViewType

class ShareAdapter(val listener: (view: View, position: Int) -> Unit) : BaseAdapter<ViewType>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            R.layout.list_share_settings_header -> {
                ShareHeaderViewHolder(parent.inflate(R.layout.list_share_settings_header))
            }
            R.layout.list_share_settings_item -> {
                ShareItemViewHolder(parent.inflate(R.layout.list_share_settings_item), listener)
            }
            else -> {
                throw InflateException("Error create share view holder")
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is ShareHeaderViewHolder) {
            holder.bind(mList[position] as ShareHeaderUi)
        } else if (holder is ShareItemViewHolder) {
            holder.bind(mList[position] as ShareUi)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return mList[position].viewType
    }
}