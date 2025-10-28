package app.editors.manager.ui.adapters.diffutilscallback

import androidx.recyclerview.widget.DiffUtil

abstract class BaseDiffUtilsCallback<D>(
    protected val mNewList: List<D>,
    protected val mOldList: List<D>
) : DiffUtil.Callback() {

    override fun getNewListSize(): Int = mNewList.size

    override fun getOldListSize(): Int = mOldList.size
}