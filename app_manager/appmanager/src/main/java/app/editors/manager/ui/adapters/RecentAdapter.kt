package app.editors.manager.ui.adapters

import androidx.recyclerview.widget.DiffUtil
import app.documents.core.model.cloud.Recent
import app.editors.manager.mvp.models.ui.RecentUI
import app.editors.manager.mvp.models.ui.toRecentUI
import app.editors.manager.ui.adapters.base.BaseViewTypeAdapter
import app.editors.manager.ui.adapters.diffutilscallback.RecentDiffUtilsCallback
import app.editors.manager.ui.adapters.holders.explorer.GridFileViewHolder
import app.editors.manager.ui.adapters.holders.explorer.ListFileViewHolder
import app.editors.manager.ui.adapters.holders.factory.RecentHolderFactory
import lib.toolkit.base.ui.adapters.holder.ViewType

class RecentAdapter(
    var isGrid: Boolean,
    factory: RecentHolderFactory,
) : BaseViewTypeAdapter<ViewType>(factory) {

    override fun getItemViewType(position: Int): Int {
        return if (isGrid) GridFileViewHolder.LAYOUT else ListFileViewHolder.LAYOUT
    }

    fun setRecent(list: List<Recent>) {
        val newList = list.map { it.toRecentUI() }
        val diffUtils = RecentDiffUtilsCallback(newList, itemsList)
        val result = DiffUtil.calculateDiff(diffUtils)
        super.set(newList, result)
    }

    fun isEmpty(): Boolean {
        return itemsList.filterIsInstance<RecentUI>().isEmpty()
    }
}