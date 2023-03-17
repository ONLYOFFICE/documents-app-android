package app.editors.manager.ui.adapters.holders.factory

import android.view.View
import app.documents.core.network.manager.models.explorer.CloudFile
import app.documents.core.network.manager.models.explorer.CloudFolder
import app.editors.manager.mvp.models.list.Footer
import app.documents.core.network.manager.models.explorer.UploadFile
import app.editors.manager.mvp.models.list.Header
import app.editors.manager.ui.adapters.ExplorerAdapter
import app.editors.manager.ui.adapters.holders.*
import java.lang.RuntimeException

class TypeFactoryExplorer private constructor() {

    fun createViewHolder(parent: View, type: Int, adapter: ExplorerAdapter):
            BaseViewHolderExplorer<*> = when (type) {
        FileViewHolder.LAYOUT -> FileViewHolder(parent, adapter)
        FolderViewHolder.LAYOUT -> FolderViewHolder(parent, adapter)
        HeaderViewHolder.LAYOUT -> HeaderViewHolder(parent, adapter)
        FooterViewHolder.LAYOUT -> FooterViewHolder(parent, adapter)
        UploadFileViewHolder.LAYOUT -> UploadFileViewHolder(parent, adapter)
        else -> throw RuntimeException("Unknown type is unacceptable: $type")
    }

    companion object {
        private var typeFactoryExplorer: TypeFactoryExplorer? = null

        @JvmStatic
        val factory: TypeFactoryExplorer
            get() = typeFactoryExplorer ?: TypeFactoryExplorer().also { typeFactoryExplorer = it }

    }
}