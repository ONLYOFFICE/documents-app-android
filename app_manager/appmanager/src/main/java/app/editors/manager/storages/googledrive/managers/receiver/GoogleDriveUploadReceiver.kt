package app.editors.manager.storages.googledrive.managers.receiver

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import app.editors.manager.managers.receivers.UploadReceiver

class GoogleDriveUploadReceiver(): UploadReceiver() {

    companion object {
        const val UPLOAD_ITEM_ID = "UPLOAD_ITEM_ID"

        const val KEY_ITEM_ID = "KEY_ITEM_ID"
    }

    interface OnGoogleDriveUploadListener {
        fun onItemId(itemId: String)
    }

    private var onUploadListener: OnGoogleDriveUploadListener? = null

    override fun onReceive(context: Context?, intent: Intent?) {
        super.onReceive(context, intent)

        if(intent?.action == UPLOAD_ITEM_ID) {
            intent.getStringExtra(KEY_ITEM_ID)?.let { onUploadListener?.onItemId(it) }
        }

    }

    fun setUploadListener(onUploadListener: OnGoogleDriveUploadListener?) {
        this.onUploadListener = onUploadListener
    }

    override fun getFilter(): IntentFilter {
        val intentFilter = super.getFilter()
        intentFilter.addAction(UPLOAD_ITEM_ID)
        return intentFilter
    }
}