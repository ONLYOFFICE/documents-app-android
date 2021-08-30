package app.editors.manager.ui.adapters.diffutilscallback

import app.editors.manager.mvp.models.ui.ShareViewType
import lib.toolkit.base.ui.adapters.holder.ViewType

class ShareSearchDiffUtilsCallback(newList: List<ViewType>, oldList: List<ViewType>) :
    BaseDiffUtilsCallback<ViewType>(newList, oldList) {

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        if (mOldList[oldItemPosition] is ShareViewType ||
            mOldList[oldItemPosition] is ShareViewType) {
            val oldItem = mOldList[oldItemPosition] as ShareViewType
            val newItem = mNewList[newItemPosition] as ShareViewType
            return oldItem.itemId == newItem.itemId
        }
        return false
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        if (mOldList[oldItemPosition] is ShareViewType ||
            mOldList[oldItemPosition] is ShareViewType) {
            val oldItem = mOldList[oldItemPosition] as ShareViewType
            val newItem = mNewList[newItemPosition] as ShareViewType
            return oldItem.itemName == newItem.itemName
        }
        return false
    }
}