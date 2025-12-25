package app.editors.manager.ui.adapters

import android.content.Context
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ItemTouchHelper.DOWN
import androidx.recyclerview.widget.ItemTouchHelper.UP
import androidx.recyclerview.widget.RecyclerView
import app.documents.core.network.manager.models.base.Entity
import app.documents.core.network.manager.models.explorer.CloudFile
import app.documents.core.network.manager.models.explorer.CloudFolder
import app.documents.core.network.manager.models.explorer.Item
import app.editors.manager.app.App.Companion.getApp
import app.editors.manager.app.accountOnline
import app.editors.manager.managers.tools.PreferenceTool
import app.editors.manager.mvp.models.list.Footer
import app.editors.manager.mvp.models.list.Templates
import app.editors.manager.mvp.presenters.main.PickerMode
import app.editors.manager.ui.adapters.base.BaseAdapter
import app.editors.manager.ui.adapters.diffutilscallback.EntityDiffUtilsCallback
import app.editors.manager.ui.adapters.holders.BaseViewHolderExplorer
import app.editors.manager.ui.adapters.holders.explorer.GridFileViewHolder
import app.editors.manager.ui.adapters.holders.explorer.GridFolderViewHolder
import app.editors.manager.ui.adapters.holders.explorer.GridFooterViewHolder
import app.editors.manager.ui.adapters.holders.explorer.GridRoomViewHolder
import app.editors.manager.ui.adapters.holders.explorer.GridTemplateViewHolder
import app.editors.manager.ui.adapters.holders.explorer.ListFileViewHolder
import app.editors.manager.ui.adapters.holders.explorer.ListFolderViewHolder
import app.editors.manager.ui.adapters.holders.explorer.ListFooterViewHolder
import app.editors.manager.ui.adapters.holders.explorer.ListRoomViewHolder
import app.editors.manager.ui.adapters.holders.explorer.ListTemplateViewHolder
import app.editors.manager.ui.adapters.holders.explorer.TemplatesFolderViewHolder
import app.editors.manager.ui.adapters.holders.factory.TypeFactoryExplorer
import lib.toolkit.base.ui.adapters.factory.inflate
import java.util.Collections
import javax.inject.Inject

interface ExplorerTouchAdapter {

    fun onMove(from: Int, to: Int): List<Item>
    fun onMoveFinished()
}

interface AdapterState {

    val accountId: String?
    val sortBy: String?

    var currentFolderId: String
    var sectionType: Int
    var isRoot: Boolean
    var isFooter: Boolean
    var isTrash: Boolean
    var isIndexing: Boolean
    var isGridView: Boolean
}

class ExplorerAdapter(
    private val factory: TypeFactoryExplorer,
    initialGridView: Boolean
) : BaseAdapter<Entity>(), AdapterState, ExplorerTouchAdapter {

    companion object {

        fun getTouchHelperCallback(
            adapter: ExplorerTouchAdapter,
            onMove: (items: List<Item>) -> Unit
        ): ItemTouchHelper.SimpleCallback {
            return object : ItemTouchHelper.SimpleCallback(UP or DOWN, 0) {

                override fun onMove(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                    target: RecyclerView.ViewHolder
                ): Boolean {
                    val items = adapter.onMove(
                        viewHolder.absoluteAdapterPosition,
                        target.absoluteAdapterPosition
                    )
                    onMove(items)
                    return false
                }

                override fun onSwiped(
                    viewHolder: RecyclerView.ViewHolder,
                    direction: Int
                ) {
                }

                override fun clearView(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder
                ) {
                    super.clearView(recyclerView, viewHolder)
                    adapter.onMoveFinished()
                }
            }
        }
    }

    @Inject
    lateinit var context: Context

    @Inject
    lateinit var preferenceTool: PreferenceTool

    override val accountId: String? by lazy { context.accountOnline?.id }

    override var currentFolderId: String = ""
    override var sectionType: Int = -1
    override var isRoot: Boolean = false
    override var isFooter: Boolean = false
    override var isTrash: Boolean = false
    override var isIndexing: Boolean = false
    override var isGridView: Boolean = initialGridView

    override val sortBy: String? get() = preferenceTool.sortBy

    var onDragStartListener: (RecyclerView.ViewHolder) -> Unit = {}

    var isSelectMode = false
        set(isSelectMode) {
            field = isSelectMode
            notifyItemRangeChanged(0, itemCount - 1, ExplorerPayload.SELECTION)
        }

    var pickerMode: PickerMode = PickerMode.None

    private val footer: Footer = Footer()

    init {
        getApp().appComponent.inject(this)
    }

    fun updateItems(list: List<Entity>?, currentId: String? = null) {
        if (list == null) return
        val listCurrentId = currentId ?: currentFolderId
        if (itemList != null && listCurrentId == currentFolderId) {
            val callback = EntityDiffUtilsCallback(list, itemList)
            val result = DiffUtil.calculateDiff(callback)
            super.setData(list)
            result.dispatchUpdatesTo(this)
        } else {
            currentFolderId = listCurrentId
            super.setItems(list)
        }
    }

    fun updateItemById(id: String, payload: ExplorerPayload) {
        val position = itemList.indexOfFirst { it.getEntityId() == id }
        if (position != -1) notifyItemChanged(position, payload)
    }

    override fun onCreateViewHolder(view: ViewGroup, type: Int): BaseViewHolderExplorer<*> {
        return factory.createViewHolder(view.inflate(type), type, this)
    }

    @Suppress("UNCHECKED_CAST")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is ListFooterViewHolder) {
            holder.bind(footer)
        } else {
            (holder as BaseViewHolderExplorer<Entity>).bind(mList[position])
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int,
        payloads: List<*>
    ) {
        if (payloads.isEmpty()) {
            onBindViewHolder(holder, position)
            return
        }
        for (payload in payloads) {
            when (payload) {
                ExplorerPayload.SELECTION -> {
                    (holder as? BaseViewHolderExplorer<Entity>)?.bindSelection(mList[position])
                }
                ExplorerPayload.THUMBNAIL -> {
                    if (!isGridView) return
                    (mList[position] as? CloudFile?)?.let { file ->
                        (holder as? GridFileViewHolder)?.bindThumbnail(file)
                    }
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return super.getItemCount() + 1
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == itemCount - 1) {
            if (isGridView) GridFooterViewHolder.LAYOUT else ListFooterViewHolder.LAYOUT
        } else {
            when (val item = itemList[position]) {
                is CloudFile -> if (isGridView) GridFileViewHolder.LAYOUT else ListFileViewHolder.LAYOUT
                is CloudFolder -> getFolderLayout(item)
                Templates -> TemplatesFolderViewHolder.LAYOUT
                else -> 0
            }
        }
    }

    private fun getFolderLayout(item: CloudFolder): Int {
        return if (isGridView) {
            when {
                item.isTemplate -> GridTemplateViewHolder.LAYOUT
                item.isRoom -> GridRoomViewHolder.LAYOUT
                else -> GridFolderViewHolder.LAYOUT
            }
        } else {
            when {
                item.isTemplate -> ListTemplateViewHolder.LAYOUT
                item.isRoom -> ListRoomViewHolder.LAYOUT
                else -> ListFolderViewHolder.LAYOUT
            }
        }
    }

    fun isLoading(isShow: Boolean) {
        isFooter = isShow
        notifyItemChanged(itemCount - 1)
    }

    override fun onMove(from: Int, to: Int): List<Item> {
        val lastIndex = itemList.lastIndex
        if (from !in 0..lastIndex || to !in 0..lastIndex) return emptyList()
        Collections.swap(itemList, from, to)
        notifyItemMoved(from, to)
        swapIndexes(from, to)
        return listOf(itemList[from] as Item, itemList[to] as Item)
    }

    override fun onMoveFinished() {
        notifyDataSetChanged()
    }

    private fun swapIndexes(from: Int, to: Int) {
        val list = itemList
        val indexFrom = (list[from] as Item).index
        val indexTo = (list[to] as Item).index
        (list[from] as Item).index = indexTo
        (list[to] as Item).index = indexFrom
    }

    enum class ExplorerPayload {
        SELECTION, THUMBNAIL
    }
}