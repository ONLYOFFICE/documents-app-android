package app.editors.manager.ui.adapters

import android.content.Context
import app.editors.manager.app.App.Companion.getApp
import lib.toolkit.base.managers.utils.UiUtils.getFloatResource
import app.editors.manager.managers.tools.PreferenceTool
import app.editors.manager.mvp.models.list.Footer
import android.view.ViewGroup
import app.editors.manager.ui.adapters.holders.BaseViewHolderExplorer
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import app.editors.manager.ui.adapters.holders.FooterViewHolder
import app.editors.manager.ui.adapters.holders.UploadFileViewHolder
import app.editors.manager.mvp.models.explorer.UploadFile
import app.editors.manager.mvp.models.explorer.CloudFile
import app.documents.core.network.ApiContract
import androidx.annotation.DrawableRes
import app.editors.manager.R
import app.editors.manager.mvp.models.base.Entity
import app.editors.manager.mvp.models.explorer.CloudFolder
import app.editors.manager.mvp.models.list.Header
import app.editors.manager.ui.adapters.base.BaseAdapter
import app.editors.manager.ui.adapters.holders.factory.TypeFactory
import lib.toolkit.base.ui.adapters.factory.inflate
import javax.inject.Inject

class ExplorerAdapter(private val factory: TypeFactory) : BaseAdapter<Entity>() {

    @Inject
    lateinit var context: Context

    @Inject
    lateinit var preferenceTool: PreferenceTool

    var isRoot: Boolean = false
    var isFooter: Boolean = false
    var isSectionMy: Boolean = false

    var isSelectMode = false
        private set(isSelectMode) {
            field = isSelectMode
            notifyDataSetChanged()
        }

    var isFoldersMode = false
        private set(isFoldersMode) {
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
            ?: throw RuntimeException("ViewHolder can not be null")
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
            itemList[position].getType(factory)
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

    private fun setFileFavoriteStatus(position: Int) {
        val file = mList[position]
        if (file is CloudFile && file.fileStatus.isNotEmpty()) {
            val favoriteMask = file.fileStatus.toInt() and ApiContract.FileStatus.FAVORITE
            file.favorite = favoriteMask != 0
        }
    }

    fun setFolderIcon(view: ImageView, folder: CloudFolder,) {
        @DrawableRes var resId = R.drawable.ic_type_folder
        if (folder.shared && folder.providerKey.isEmpty()) {
            resId = R.drawable.ic_type_folder_shared
        } else if (isRoot && folder.providerItem && !folder.providerKey.isEmpty()) {
            when (folder.providerKey) {
                ApiContract.Storage.BOXNET -> resId = R.drawable.ic_storage_box
                ApiContract.Storage.DROPBOX -> resId = R.drawable.ic_storage_dropbox
                ApiContract.Storage.SHAREPOINT -> resId = R.drawable.ic_storage_sharepoint
                ApiContract.Storage.GOOGLEDRIVE -> resId = R.drawable.ic_storage_google
                ApiContract.Storage.ONEDRIVE, ApiContract.Storage.SKYDRIVE -> resId =
                    R.drawable.ic_storage_onedrive
                ApiContract.Storage.YANDEX -> resId = R.drawable.ic_storage_yandex
                ApiContract.Storage.WEBDAV -> {
                    resId = R.drawable.ic_storage_webdav
                    view.setImageResource(resId)
                    view.alpha =
                        getFloatResource(context, R.dimen.alpha_medium)
                    return
                }
            }
            view.setImageResource(resId)
            view.alpha = 1.0f
            view.clearColorFilter()
            return
        }
        view.setImageResource(resId)
        view.alpha =
            getFloatResource(context, R.dimen.alpha_medium)
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