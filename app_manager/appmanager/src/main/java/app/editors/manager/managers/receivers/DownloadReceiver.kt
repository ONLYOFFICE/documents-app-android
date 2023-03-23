package app.editors.manager.managers.receivers

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import app.editors.manager.R
import app.editors.manager.managers.utils.FirebaseUtils.addCrash

class DownloadReceiver : BaseReceiver<Intent?>() {

    companion object {
        const val DOWNLOAD_ACTION_ERROR = "DOWNLOAD_ACTION_ERROR"
        const val DOWNLOAD_ACTION_ERROR_FREE_SPACE = "DOWNLOAD_ACTION_ERROR_FREE_SPACE"
        const val DOWNLOAD_ACTION_ERROR_URL_INIT = "DOWNLOAD_ACTION_ERROR_URL_INIT"
        const val DOWNLOAD_ACTION_PROGRESS = "DOWNLOAD_ACTION_PROGRESS"
        const val DOWNLOAD_ACTION_COMPLETE = "DOWNLOAD_ACTION_COMPLETE"
        const val DOWNLOAD_ACTION_REPEAT = "DOWNLOAD_ACTION_REPEAT"
        const val DOWNLOAD_ACTION_CANCELED = "DOWNLOAD_ACTION_CANCELED"

        const val EXTRAS_KEY_ID = "EXTRAS_KEY_ID"
        const val EXTRAS_KEY_URL = "EXTRAS_KEY_URL"
        const val EXTRAS_KEY_TITLE = "EXTRAS_KEY_TITLE"
        const val EXTRAS_KEY_TOTAL = "EXTRAS_KEY_TOTAL"
        const val EXTRAS_KEY_PATH = "EXTRAS_KEY_PATH"
        const val EXTRAS_KEY_MIME_TYPE = "EXTRAS_KEY_MIME_TYPE"
        const val EXTRAS_KEY_PROGRESS = "EXTRAS_KEY_PROGRESS"
        const val EXTRAS_KEY_CANCELED = "EXTRAS_KEY_CANCELED"
        const val EXTRAS_KEY_ERROR = "EXTRAS_KEY_ERROR"
        const val EXTRAS_KEY_URI = "EXTRAS_KEY_URI"

        const val EXTRAS_VALUE_CANCELED = 0
        const val EXTRAS_VALUE_CANCELED_NOT_FOUND = 1
    }

    interface OnDownloadListener {
        fun onDownloadError(info: String?)
        fun onDownloadProgress(id: String?, total: Int, progress: Int)
        fun onDownloadCanceled(id: String?, info: String?)
        fun onDownloadRepeat(id: String?, title: String?, info: String?)
        fun onDownloadComplete(
            id: String?,
            url: String?,
            title: String?,
            info: String?,
            path: String?,
            mime: String?,
            uri: Uri?
        )
    }

    private var onDownloadListener: OnDownloadListener? = null

    override fun onReceive(context: Context, intent: Intent) {
        try {
            if (onDownloadListener != null) {
                val id = intent.getStringExtra(EXTRAS_KEY_ID)
                val title = intent.getStringExtra(EXTRAS_KEY_TITLE)

                when (intent.action) {
                    DOWNLOAD_ACTION_ERROR -> {
                        onDownloadListener?.onDownloadError(info = intent.getStringExtra(EXTRAS_KEY_ERROR))
                    }
                    DOWNLOAD_ACTION_ERROR_FREE_SPACE -> {
                        onDownloadListener?.onDownloadError(
                            info = context.getString(R.string.download_manager_error_free_space)
                        )
                    }
                    DOWNLOAD_ACTION_ERROR_URL_INIT -> {
                        onDownloadListener?.onDownloadError(
                            info = context.getString(R.string.download_manager_error_url)
                        )
                    }
                    DOWNLOAD_ACTION_PROGRESS -> {
                        onDownloadListener?.onDownloadProgress(
                            id = id,
                            total = intent.getIntExtra(EXTRAS_KEY_TOTAL, 0),
                            progress = intent.getIntExtra(EXTRAS_KEY_PROGRESS, 0)
                        )
                    }
                    DOWNLOAD_ACTION_COMPLETE -> {
                        onDownloadListener?.onDownloadComplete(
                            id = id,
                            url = intent.getStringExtra(EXTRAS_KEY_URL),
                            title = title,
                            info = context.getString(R.string.download_manager_complete),
                            path = intent.getStringExtra(EXTRAS_KEY_PATH),
                            mime = intent.getStringExtra(EXTRAS_KEY_MIME_TYPE),
                            uri = Uri.parse(intent.getStringExtra(EXTRAS_KEY_URI))
                        )
                    }
                    DOWNLOAD_ACTION_REPEAT -> {
                        onDownloadListener?.onDownloadRepeat(
                            id = id,
                            title = title,
                            info = context.getString(R.string.download_manager_repeat)
                        )
                    }
                    DOWNLOAD_ACTION_CANCELED -> {
                        onDownloadListener?.onDownloadCanceled(
                            id = id,
                            info = context.getString(R.string.download_manager_cancel)
                        )
                    }
                }
            }
        } catch (e: RuntimeException) {
            addCrash(e)
        }
    }

    override fun getFilter(): IntentFilter {
        return IntentFilter().apply {
            addAction(DOWNLOAD_ACTION_ERROR)
            addAction(DOWNLOAD_ACTION_ERROR_FREE_SPACE)
            addAction(DOWNLOAD_ACTION_ERROR_URL_INIT)
            addAction(DOWNLOAD_ACTION_PROGRESS)
            addAction(DOWNLOAD_ACTION_COMPLETE)
            addAction(DOWNLOAD_ACTION_REPEAT)
            addAction(DOWNLOAD_ACTION_CANCELED)
        }
    }

    fun setOnDownloadListener(onDownloadListener: OnDownloadListener?) {
        this.onDownloadListener = onDownloadListener
    }
}