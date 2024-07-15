package app.editors.manager.ui.adapters

import android.content.Context
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import app.documents.core.network.common.contracts.ApiContract
import app.documents.core.network.manager.models.base.Entity
import app.documents.core.network.manager.models.explorer.CloudFile
import app.documents.core.network.manager.models.explorer.CloudFolder
import app.documents.core.network.manager.models.explorer.UploadFile
import app.editors.manager.app.App.Companion.getApp
import app.editors.manager.app.accountOnline
import app.editors.manager.managers.tools.PreferenceTool
import app.editors.manager.mvp.models.list.Footer
import app.editors.manager.mvp.models.list.Header
import app.editors.manager.mvp.models.list.RecentViaLink
import app.editors.manager.ui.adapters.base.BaseAdapter
import app.editors.manager.ui.adapters.holders.BaseViewHolderExplorer
import app.editors.manager.ui.adapters.holders.FileViewHolder
import app.editors.manager.ui.adapters.holders.FolderViewHolder
import app.editors.manager.ui.adapters.holders.FooterViewHolder
import app.editors.manager.ui.adapters.holders.GridFileViewHolder
import app.editors.manager.ui.adapters.holders.GridFolderViewHolder
import app.editors.manager.ui.adapters.holders.GridFooterViewHolder
import app.editors.manager.ui.adapters.holders.HeaderViewHolder
import app.editors.manager.ui.adapters.holders.RecentViaLinkViewHolder
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
        if (holder is FooterViewHolder) {
            holder.bind(footer)
        } else {
            setFileFavoriteStatus(position)
            (holder as BaseViewHolderExplorer<Entity>).bind(mList[position])
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int, payloads: List<*>) {
        if (payloads.isEmpty()) {
            onBindViewHolder(holder, position)
        }
    }

    override fun getItemCount(): Int {
        return super.getItemCount() + 1
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == itemCount - 1) {
            if (isGridView) GridFooterViewHolder.LAYOUT else FooterViewHolder.LAYOUT
        } else {
            when (itemList[position]) {
                is CloudFile -> if (isGridView) GridFileViewHolder.LAYOUT else FileViewHolder.LAYOUT
                is CloudFolder -> if (isGridView) GridFolderViewHolder.LAYOUT else FolderViewHolder.LAYOUT
                is Header -> HeaderViewHolder.LAYOUT
                RecentViaLink -> RecentViaLinkViewHolder.LAYOUT
                else -> 0
            }
        }
    }

    private fun setFileFavoriteStatus(position: Int) {
        val file = mList[position]
        if (file is CloudFile && file.fileStatus.isNotEmpty()) {
            val favoriteMask = file.fileStatus.toInt() and ApiContract.FileStatus.FAVORITE
            file.favorite = favoriteMask != 0
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