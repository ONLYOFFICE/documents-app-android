package app.editors.manager.ui.adapters.holders

import android.view.Gravity
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import app.documents.core.network.common.contracts.ApiContract
import app.editors.manager.R
import app.editors.manager.databinding.ListStorageSelectItemBinding
import lib.toolkit.base.managers.utils.UiUtils
import lib.toolkit.base.ui.adapters.BaseAdapter

class StorageViewHolder(view: View, clickListener: BaseAdapter.OnItemClickListener)
    : RecyclerView.ViewHolder(view) {

    private val viewBinding = ListStorageSelectItemBinding.bind(view)
    private val context = view.context

    init {
        viewBinding.storageItemLayout.setOnClickListener { v ->
            clickListener.onItemClick(v, layoutPosition)
        }
    }

    fun bind(id: String) {
        with(viewBinding) {
            val padding = context.resources.getDimension(lib.toolkit.base.R.dimen.image_padding_icon).toInt()
            storageItemTitle.gravity = Gravity.START or Gravity.CENTER_VERTICAL
            storageItemImage.setPadding(0, 0, 0, 0)
            storageItemImage.alpha = 1.0f
            when (id) {
                ApiContract.Storage.BOXNET -> {
                    storageItemImage.setImageResource(R.drawable.ic_storage_box)
                    storageItemTitle.setText(R.string.storage_select_box)
                }
                ApiContract.Storage.DROPBOX -> {
                    storageItemImage.setImageResource(R.drawable.ic_storage_dropbox)
                    storageItemTitle.setText(R.string.storage_select_drop_box)
                }
                ApiContract.Storage.SHAREPOINT -> {
                    storageItemImage.setImageResource(R.drawable.ic_storage_sharepoint)
                    storageItemTitle.setText(R.string.storage_select_share_point)
                }
                ApiContract.Storage.GOOGLEDRIVE -> {
                    storageItemImage.setImageResource(R.drawable.ic_storage_google)
                    storageItemTitle.setText(R.string.storage_select_google_drive)
                }
                ApiContract.Storage.ONEDRIVE -> {
                    storageItemImage.setImageResource(R.drawable.ic_storage_onedrive)
                    storageItemTitle.setText(R.string.storage_select_one_drive)
                }
                ApiContract.Storage.YANDEX -> {
                    storageItemImage.setImageResource(R.drawable.ic_storage_yandex)
                    storageItemTitle.setText(R.string.storage_select_yandex)
                }
                ApiContract.Storage.OWNCLOUD -> {
                    storageItemImage.setImageResource(R.drawable.ic_storage_owncloud)
                    storageItemTitle.setText(R.string.storage_select_own_cloud)
                }
                ApiContract.Storage.NEXTCLOUD -> {
                    storageItemImage.setImageResource(R.drawable.ic_storage_nextcloud)
                    storageItemTitle.setText(R.string.storage_select_next_cloud)
                }
                ApiContract.Storage.KDRIVE -> {
                    storageItemImage.setImageResource(R.drawable.ic_storage_kdrive)
                    storageItemTitle.text = id
                }
                ApiContract.Storage.WEBDAV -> {
                    storageItemImage.setImageResource(R.drawable.ic_storage_webdav)
                    storageItemImage.alpha = UiUtils.getFloatResource(context, lib.toolkit.base.R.dimen.alpha_medium)
                    storageItemImage.setPadding(padding, padding, padding, padding)
                    storageItemTitle.setText(R.string.storage_select_web_dav)
                }
                else -> {
                    storageItemImage.setImageResource(R.drawable.ic_storage_webdav)
                    storageItemImage.alpha = UiUtils.getFloatResource(context, lib.toolkit.base.R.dimen.alpha_medium                    )
                    storageItemImage.setPadding(padding, padding, padding, padding)
                    storageItemTitle.text = id
                }
            }
        }
    }
}