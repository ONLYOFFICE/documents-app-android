package app.editors.manager.ui.adapters.holders.factory

import android.view.View
import app.editors.manager.mvp.models.explorer.CloudFile
import app.editors.manager.mvp.models.explorer.CloudFolder
import app.editors.manager.mvp.models.list.Footer
import app.editors.manager.mvp.models.explorer.UploadFile
import app.editors.manager.mvp.models.list.Header
import app.editors.manager.ui.adapters.ExplorerAdapter
import app.editors.manager.ui.adapters.holders.BaseViewHolderExplorer

interface TypeFactory {
    fun type(file: CloudFile): Int
    fun type(folder: CloudFolder): Int
    fun type(header: Header): Int
    fun type(header: Footer): Int
    fun type(uploadFile: UploadFile): Int

    fun createViewHolder(parent: View, type: Int, adapter: ExplorerAdapter):
            BaseViewHolderExplorer<*>?
}