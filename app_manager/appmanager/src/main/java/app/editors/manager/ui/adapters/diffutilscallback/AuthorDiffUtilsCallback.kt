package app.editors.manager.ui.adapters.diffutilscallback

import app.editors.manager.mvp.models.filter.Author
import lib.toolkit.base.ui.adapters.holder.ViewType

class AuthorDiffUtilsCallback(newList: List<ViewType>, oldList: List<ViewType>) :
    BaseDiffUtilsCallback<ViewType>(newList, oldList) {

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val oldItem = mOldList[oldItemPosition] as Author
        val newItem = mNewList[newItemPosition] as Author
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val oldItem = mOldList[oldItemPosition] as Author
        val newItem = mNewList[newItemPosition] as Author
        return oldItem.name == newItem.name
    }
}