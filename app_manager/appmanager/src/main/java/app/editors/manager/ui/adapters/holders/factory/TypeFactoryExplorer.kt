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
import app.editors.manager.ui.adapters.holders.explorer.ListRoomViewHolder
import app.editors.manager.ui.adapters.holders.explorer.RecentViaLinkViewHolder

class TypeFactoryExplorer private constructor() {

    fun createViewHolder(parent: View, type: Int, adapter: ExplorerAdapter):
            BaseViewHolderExplorer<*> = when (type) {
        ListFileViewHolder.LAYOUT -> ListFileViewHolder(parent, adapter)
        ListFolderViewHolder.LAYOUT -> ListFolderViewHolder(parent, adapter)
        ListFooterViewHolder.LAYOUT -> ListFooterViewHolder(parent, adapter)
        ListRoomViewHolder.LAYOUT -> ListRoomViewHolder(parent, adapter)
        GridFileViewHolder.LAYOUT -> GridFileViewHolder(parent, adapter)
        GridFolderViewHolder.LAYOUT -> GridFolderViewHolder(parent, adapter)
        GridRoomViewHolder.LAYOUT -> GridRoomViewHolder(parent, adapter)
        GridFooterViewHolder.LAYOUT -> GridFooterViewHolder(parent, adapter)
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