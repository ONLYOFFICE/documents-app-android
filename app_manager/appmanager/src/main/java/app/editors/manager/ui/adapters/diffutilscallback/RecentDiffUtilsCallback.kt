package app.editors.manager.ui.adapters.diffutilscallback

import app.editors.manager.mvp.models.list.Header
import app.editors.manager.mvp.models.ui.RecentUI
import lib.toolkit.base.ui.adapters.holder.ViewType

class RecentDiffUtilsCallback(mNewList: List<ViewType>, mOldList: List<ViewType>) :
    BaseDiffUtilsCallback<ViewType>(mNewList, mOldList) {

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return if (mOldList[oldItemPosition] is Header && mNewList[newItemPosition] is Header) {
            (mOldList[oldItemPosition] as Header).title == (mNewList[newItemPosition] as Header).title
        } else if (mOldList[oldItemPosition] is RecentUI && mNewList[newItemPosition] is RecentUI) {
            (mOldList[oldItemPosition] as RecentUI).id == (mNewList[newItemPosition] as RecentUI).id
        } else false
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return if (mOldList[oldItemPosition] is RecentUI && mNewList[newItemPosition] is RecentUI)
            return (mOldList[oldItemPosition] as RecentUI).date == (mNewList[newItemPosition] as RecentUI).date
        else false
    }
}