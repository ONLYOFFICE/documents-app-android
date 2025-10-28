package app.editors.manager.ui.adapters.diffutilscallback

import java.io.File

class FileDiffUtilsCallback(
    newList: List<File>,
    oldList: List<File>
) : BaseDiffUtilsCallback<File>(newList, oldList) {

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val oldFile = mOldList[oldItemPosition]
        val newFile = mNewList[newItemPosition]
        return oldFile.absolutePath == newFile.absolutePath
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val oldFile = mOldList[oldItemPosition]
        val newFile = mNewList[newItemPosition]
        return oldFile.lastModified() == newFile.lastModified()
    }
}