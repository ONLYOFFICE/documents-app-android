package app.editors.manager.ui.adapters.holders.factory

import android.view.View
import app.editors.manager.ui.adapters.ExplorerAdapter
import app.editors.manager.ui.adapters.holders.BaseViewHolderExplorer
import app.editors.manager.ui.adapters.holders.explorer.ListFileViewHolder
import app.editors.manager.ui.adapters.holders.explorer.ListFolderViewHolder
import app.editors.manager.ui.adapters.holders.explorer.ListFooterViewHolder
import app.editors.manager.ui.adapters.holders.explorer.GridFileViewHolder
import app.editors.manager.ui.adapters.holders.explorer.GridFolderViewHolder
import app.editors.manager.ui.adapters.holders.explorer.GridFooterViewHolder
import app.editors.manager.ui.adapters.holders.explorer.GridRoomViewHolder
import app.editors.manager.ui.adapters.holders.explorer.GridTemplateViewHolder
import app.editors.manager.ui.adapters.holders.explorer.ListRoomViewHolder
import app.editors.manager.ui.adapters.holders.explorer.ListTemplateViewHolder
import app.editors.manager.ui.adapters.holders.explorer.RecentViaLinkViewHolder
import app.editors.manager.ui.adapters.holders.explorer.TemplatesFolderViewHolder

class TypeFactoryExplorer private constructor() {

    fun createViewHolder(parent: View, type: Int, adapter: ExplorerAdapter):
            BaseViewHolderExplorer<*> = when (type) {
        ListFileViewHolder.LAYOUT -> ListFileViewHolder(parent, adapter)
        ListFolderViewHolder.LAYOUT -> ListFolderViewHolder(parent, adapter)
        ListFooterViewHolder.LAYOUT -> ListFooterViewHolder(parent, adapter)
        ListRoomViewHolder.LAYOUT -> ListRoomViewHolder(parent, adapter)
        ListTemplateViewHolder.LAYOUT -> ListTemplateViewHolder(parent, adapter)
        GridFileViewHolder.LAYOUT -> GridFileViewHolder(parent, adapter)
        GridFolderViewHolder.LAYOUT -> GridFolderViewHolder(parent, adapter)
        GridRoomViewHolder.LAYOUT -> GridRoomViewHolder(parent, adapter)
        GridTemplateViewHolder.LAYOUT -> GridTemplateViewHolder(parent, adapter)
        GridFooterViewHolder.LAYOUT -> GridFooterViewHolder(parent, adapter)
        RecentViaLinkViewHolder.LAYOUT -> RecentViaLinkViewHolder(parent, adapter)
        TemplatesFolderViewHolder.LAYOUT -> TemplatesFolderViewHolder(parent, adapter)
        else -> throw RuntimeException("Unknown type is unacceptable: $type")
    }

    companion object {
        private var typeFactoryExplorer: TypeFactoryExplorer? = null

        @JvmStatic
        val factory: TypeFactoryExplorer
            get() = typeFactoryExplorer ?: TypeFactoryExplorer().also { typeFactoryExplorer = it }

    }
}