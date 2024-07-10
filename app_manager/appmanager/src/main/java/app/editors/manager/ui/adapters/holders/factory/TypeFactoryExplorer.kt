package app.editors.manager.ui.adapters.holders.factory

import android.view.View
import app.editors.manager.ui.adapters.ExplorerAdapter
import app.editors.manager.ui.adapters.holders.BaseViewHolderExplorer
import app.editors.manager.ui.adapters.holders.FileViewHolder
import app.editors.manager.ui.adapters.holders.FolderViewHolder
import app.editors.manager.ui.adapters.holders.FooterViewHolder
import app.editors.manager.ui.adapters.holders.GridFileViewHolder
import app.editors.manager.ui.adapters.holders.GridFolderViewHolder
import app.editors.manager.ui.adapters.holders.GridFooterViewHolder
import app.editors.manager.ui.adapters.holders.HeaderViewHolder
import app.editors.manager.ui.adapters.holders.RecentViaLinkViewHolder
import app.editors.manager.ui.adapters.holders.UploadFileViewHolder

class TypeFactoryExplorer private constructor() {

    fun createViewHolder(parent: View, type: Int, adapter: ExplorerAdapter):
            BaseViewHolderExplorer<*> = when (type) {
        FileViewHolder.LAYOUT -> FileViewHolder(parent, adapter)
        GridFileViewHolder.LAYOUT -> GridFileViewHolder(parent, adapter)
        FolderViewHolder.LAYOUT -> FolderViewHolder(parent, adapter)
        GridFolderViewHolder.LAYOUT -> GridFolderViewHolder(parent, adapter)
        FooterViewHolder.LAYOUT -> FooterViewHolder(parent, adapter)
        GridFooterViewHolder.LAYOUT -> GridFooterViewHolder(parent, adapter)
        HeaderViewHolder.LAYOUT -> HeaderViewHolder(parent, adapter)
        UploadFileViewHolder.LAYOUT -> UploadFileViewHolder(parent, adapter)
        RecentViaLinkViewHolder.LAYOUT -> RecentViaLinkViewHolder(parent, adapter)
        else -> throw RuntimeException("Unknown type is unacceptable: $type")
    }

    companion object {
        private var typeFactoryExplorer: TypeFactoryExplorer? = null

        @JvmStatic
        val factory: TypeFactoryExplorer
            get() = typeFactoryExplorer ?: TypeFactoryExplorer().also { typeFactoryExplorer = it }

    }
}