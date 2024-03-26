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
import app.editors.manager.ui.adapters.base.BaseAdapter
import app.editors.manager.ui.adapters.holders.BaseViewHolderExplorer
import app.editors.manager.ui.adapters.holders.FileViewHolder
import app.editors.manager.ui.adapters.holders.FolderViewHolder
import app.editors.manager.ui.adapters.holders.FooterViewHolder
import app.editors.manager.ui.adapters.holders.HeaderViewHolder
import app.editors.manager.ui.adapters.holders.UploadFileViewHolder
import app.editors.manager.ui.adapters.holders.factory.TypeFactoryExplorer
import lib.toolkit.base.ui.adapters.factory.inflate
import javax.inject.Inject

class ExplorerAdapter(private val factory: TypeFactoryExplorer) : BaseAdapter<Entity>() {

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

    private val footer: Footer = Footer()

    init {
        getApp().appComponent.inject(this)
    }

    override fun onCreateViewHolder(view: ViewGroup, type: Int):
            BaseViewHolderExplorer<*> {
        return factory.createViewHolder(view.inflate(type), type, this)
    }

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
        } else {
            if (holder is UploadFileViewHolder) {
                payloads[0]?.let { payload ->
                    if (payload is UploadFile) {
                        holder.updateProgress(payload)
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
            FooterViewHolder.LAYOUT
        } else {
            when (itemList[position]) {
                is CloudFile -> FileViewHolder.LAYOUT
                is CloudFolder -> FolderViewHolder.LAYOUT
                is UploadFile -> UploadFileViewHolder.LAYOUT
                is Header -> HeaderViewHolder.LAYOUT
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

    fun getUploadFileById(id: String): UploadFile? {
        mList?.let { list ->
            for (file in list) {
                if (file is UploadFile && file.id == id)
                    return file
            }
        }
        return null
    }

    fun removeUploadItemById(id: String) {
        mList?.let { list ->
            for (file in list) {
                if (file is UploadFile && file.id == id) {
                    mList.remove(file)
                    notifyItemRemoved(mList.indexOf(file))
                    break
                }
            }
        }
    }

    fun checkHeaders() {
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
    }
}