package app.editors.manager.ui.adapters

import android.content.Context
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import app.documents.core.network.manager.models.base.Entity
import app.documents.core.network.manager.models.explorer.CloudFile
import app.documents.core.network.manager.models.explorer.CloudFolder
import app.editors.manager.app.App.Companion.getApp
import app.editors.manager.app.accountOnline
import app.editors.manager.managers.tools.PreferenceTool
import app.editors.manager.mvp.models.list.Footer
import app.editors.manager.mvp.models.list.Header
import app.editors.manager.mvp.models.list.RecentViaLink
import app.editors.manager.ui.adapters.base.BaseAdapter
import app.editors.manager.ui.adapters.holders.BaseViewHolderExplorer
import app.editors.manager.ui.adapters.holders.explorer.GridFileViewHolder
import app.editors.manager.ui.adapters.holders.explorer.GridFolderViewHolder
import app.editors.manager.ui.adapters.holders.explorer.GridFooterViewHolder
import app.editors.manager.ui.adapters.holders.explorer.GridRoomViewHolder
import app.editors.manager.ui.adapters.holders.explorer.ListFileViewHolder
import app.editors.manager.ui.adapters.holders.explorer.ListFolderViewHolder
import app.editors.manager.ui.adapters.holders.explorer.ListFooterViewHolder
import app.editors.manager.ui.adapters.holders.explorer.ListRoomViewHolder
import app.editors.manager.ui.adapters.holders.explorer.RecentViaLinkViewHolder
import app.editors.manager.ui.adapters.holders.factory.TypeFactoryExplorer
import lib.toolkit.base.ui.adapters.factory.inflate
import javax.inject.Inject

class ExplorerAdapter(private val factory: TypeFactoryExplorer, initialGridView: Boolean) : BaseAdapter<Entity>() {

    @Inject
    lateinit var context: Context

    @Inject
    lateinit var preferenceTool: PreferenceTool

    val accountId: String? by lazy { context.accountOnline?.id }

    var isRoot: Boolean = false
    var isFooter: Boolean = false
    var isSectionMy: Boolean = false
    var isTrash: Boolean = false

    var isSelectMode = false
        set(isSelectMode) {
            field = isSelectMode
            notifyDataSetChanged()
        }

    var isFoldersMode = false
        set(isFoldersMode) {
            field = isFoldersMode
            notifyDataSetChanged()
        }

    var isGridView: Boolean = initialGridView

    private val footer: Footer = Footer()

    init {
        getApp().appComponent.inject(this)
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

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int, payloads: List<*>) {
        if (payloads.isEmpty()) {
            onBindViewHolder(holder, position)
            return
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
                RecentViaLink -> RecentViaLinkViewHolder.LAYOUT
                else -> 0
            }
        }
    }

    private fun getFolderLayout(item: CloudFolder): Int {
        return if (isGridView) {
            if (item.isRoom) {
                GridRoomViewHolder.LAYOUT
            } else {
                GridFolderViewHolder.LAYOUT
            }
        } else {
            if (item.isRoom) {
                ListRoomViewHolder.LAYOUT
            } else {
                ListFolderViewHolder.LAYOUT
            }
        }
    }

    fun isLoading(isShow: Boolean) {
        isFooter = isShow
        notifyItemChanged(itemCount - 1)
    }

    fun checkHeaders() {
        try {
            if (mList != null) {
                for (i in mList.indices) {
                    if (mList[i] is Header) {
                        val header = mList[i] as Header
                        val position = mList.indexOf(header)
                        if (position + 1 < mList.size - 1) {
                            if (mList[i + 1] is Header) {
                                mList.remove(header)
                                notifyItemRemoved(position)
                            }
                        } else if (mList.lastIndexOf(header) == mList.size - 1) {
                            mList.remove(header)
                            notifyItemRemoved(position)
                        }
                    }
                }
                if (mList.size == 1 && mList[0] is Header) {
                    mList.clear()
                }
            }
        } catch (error: Throwable) {
            // stub
        }

    }
}