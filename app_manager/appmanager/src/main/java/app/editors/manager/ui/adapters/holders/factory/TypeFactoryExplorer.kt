package app.editors.manager.ui.adapters.holders.factory

import android.view.View
import app.editors.manager.mvp.models.explorer.CloudFile
import app.editors.manager.mvp.models.explorer.CloudFolder
import app.editors.manager.mvp.models.list.Footer
import app.editors.manager.mvp.models.explorer.UploadFile
import app.editors.manager.mvp.models.list.Header
import app.editors.manager.ui.adapters.ExplorerAdapter
import app.editors.manager.ui.adapters.holders.*
import java.lang.RuntimeException

class TypeFactoryExplorer private constructor() : TypeFactory {
    override fun type(file: CloudFile) = FileViewHolder.LAYOUT
    override fun type(folder: CloudFolder) = FolderViewHolder.LAYOUT
    override fun type(header: Header) = HeaderViewHolder.LAYOUT
    override fun type(header: Footer) = FooterViewHolder.LAYOUT
    override fun type(uploadFile: UploadFile) = UploadFileViewHolder.LAYOUT

    override fun createViewHolder(parent: View, type: Int, adapter: ExplorerAdapter):
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
            get() = typeFactoryExplorer ?: TypeFactoryExplorer()

    }
}