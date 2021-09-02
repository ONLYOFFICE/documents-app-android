package app.editors.manager.ui.adapters.holders

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import app.editors.manager.ui.adapters.ExplorerAdapter

abstract class BaseViewHolderExplorer<T>(itemView: View, adapter: ExplorerAdapter) :
    RecyclerView.ViewHolder(itemView) {

    @JvmField
    protected var adapter: ExplorerAdapter = adapter

    abstract fun bind(element: T)

    companion object {
        const val PLACEHOLDER_POINT = " • "
    }
}