package app.editors.manager.ui.adapters.holders

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import app.documents.core.network.common.contracts.ApiContract
import app.editors.manager.R
import app.editors.manager.databinding.ThirdpartyItemLayoutBinding
import app.editors.manager.mvp.models.user.Thirdparty

class ThirdpartyViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    private val viewBinding = ThirdpartyItemLayoutBinding.bind(view)

    fun bind(item: Thirdparty) {
        with(viewBinding) {
            titleItem.text = item.title
            imageItem.setImageResource(when (item.providerKey) {
                ApiContract.Storage.BOXNET -> R.drawable.ic_storage_box
                ApiContract.Storage.DROPBOX -> R.drawable.ic_storage_dropbox
                ApiContract.Storage.SHAREPOINT -> R.drawable.ic_storage_sharepoint
                ApiContract.Storage.GOOGLEDRIVE -> R.drawable.ic_storage_google
                ApiContract.Storage.ONEDRIVE -> R.drawable.ic_storage_onedrive
                ApiContract.Storage.YANDEX -> R.drawable.ic_storage_yandex
                ApiContract.Storage.OWNCLOUD -> R.drawable.ic_storage_owncloud
                ApiContract.Storage.NEXTCLOUD -> R.drawable.ic_storage_nextcloud
                ApiContract.Storage.WEBDAV -> R.drawable.ic_storage_webdav
                else -> 0
            })
        }
    }
}