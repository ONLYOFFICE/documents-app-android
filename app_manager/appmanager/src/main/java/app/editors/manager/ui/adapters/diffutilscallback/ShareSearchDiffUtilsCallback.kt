package app.editors.manager.ui.adapters.diffutilscallback

import lib.toolkit.base.ui.adapters.holder.ViewType

class ShareSearchDiffUtilsCallback(newList: List<ViewType>, oldList: List<ViewType>)
    : BaseDiffUtilsCallback<ViewType>(newList, oldList) {

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val oldItem = mOldList[oldItemPosition]
        val newItem = mNewList[newItemPosition]
        return oldItem.getItemId() == newItem.getItemId()
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val oldItem = mOldList[oldItemPosition]
        val newItem = mNewList[newItemPosition]
        return oldItem.getItemName() == newItem.getItemName()
    }
}