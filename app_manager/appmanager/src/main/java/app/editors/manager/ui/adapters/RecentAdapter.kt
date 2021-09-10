package app.editors.manager.ui.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import app.documents.core.account.Recent
import app.editors.manager.R
import app.editors.manager.ui.adapters.holders.RecentViewHolder
import lib.toolkit.base.ui.adapters.BaseListAdapter

class RecentAdapter(
    val context: Context,
    private val itemListener: ((recent: Recent, position: Int) -> Unit)? = null,
    private val contextListener: ((recent: Recent, position: Int) -> Unit)? = null
) : BaseListAdapter<Recent>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.list_explorer_files, parent, false)
        return RecentViewHolder(view, itemListener, contextListener)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is RecentViewHolder) {
            holder.bind(mList[position])
        }
    }

    fun moveItem(position: Int, i: Int) {
        val newEntity = mList[position]
        val firstEntity = mList[i]
        mList[i] = newEntity
        mList[position] = firstEntity
        notifyItemMoved(position, i)
    }
}