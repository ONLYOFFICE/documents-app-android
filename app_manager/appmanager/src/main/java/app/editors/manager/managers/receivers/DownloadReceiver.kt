package app.editors.manager.managers.receivers

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import app.editors.manager.R
import app.editors.manager.managers.utils.FirebaseUtils.addCrash
import javax.inject.Inject
import javax.inject.Singleton
import androidx.core.net.toUri

@Singleton
class DownloadReceiver @Inject constructor() : BaseReceiver<Intent?>() {

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

    interface BaseOnDownloadListener {
        fun onDownloadError(info: String?)
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

    interface OnDownloadListener : BaseOnDownloadListener {
        fun onDownloadCanceled(id: String?, info: String?)
        fun onDownloadProgress(id: String?, total: Int, progress: Int)
        fun onDownloadRepeat(id: String?, title: String?, info: String?)
    }

    private val onDownloadListeners = mutableListOf<BaseOnDownloadListener>()

    override fun onReceive(context: Context, intent: Intent) {
        try {
            if (onDownloadListeners.isNotEmpty()) {
                val id = intent.getStringExtra(EXTRAS_KEY_ID)
                val title = intent.getStringExtra(EXTRAS_KEY_TITLE)

                when (intent.action) {
                    DOWNLOAD_ACTION_ERROR -> {
                        val info = intent.getStringExtra(EXTRAS_KEY_ERROR)
                        onDownloadListeners.forEach { it.onDownloadError(info) }
                    }
                    DOWNLOAD_ACTION_ERROR_FREE_SPACE -> {
                        val info = context.getString(R.string.download_manager_error_free_space)
                        onDownloadListeners.forEach { it.onDownloadError(info) }
                    }
                    DOWNLOAD_ACTION_ERROR_URL_INIT -> {
                        val info = context.getString(R.string.download_manager_error_url)
                        onDownloadListeners.forEach { it.onDownloadError(info) }
                    }
                    DOWNLOAD_ACTION_PROGRESS -> {
                        val total = intent.getIntExtra(EXTRAS_KEY_TOTAL, 0)
                        val progress = intent.getIntExtra(EXTRAS_KEY_PROGRESS, 0)
                        onDownloadListeners.filterIsInstance<OnDownloadListener>().forEach {
                            it.onDownloadProgress(id, total, progress)
                        }
                    }
                    DOWNLOAD_ACTION_COMPLETE -> {
                        val url = intent.getStringExtra(EXTRAS_KEY_URL)
                        val info = context.getString(R.string.download_manager_complete)
                        val path = intent.getStringExtra(EXTRAS_KEY_PATH)
                        val mime = intent.getStringExtra(EXTRAS_KEY_MIME_TYPE)
                        val uri = intent.getStringExtra(EXTRAS_KEY_URI)?.toUri()
                        onDownloadListeners.forEach {
                            it.onDownloadComplete(
                                id = id,
                                url = url,
                                title = title,
                                info = info,
                                path = path,
                                mime = mime,
                                uri = uri
                            )
                        }
                    }
                    DOWNLOAD_ACTION_REPEAT -> {
                        val info = context.getString(R.string.download_manager_repeat)
                        onDownloadListeners.filterIsInstance<OnDownloadListener>().forEach {
                            it.onDownloadRepeat(id, title, info)
                        }
                    }
                    DOWNLOAD_ACTION_CANCELED -> {
                        val info = context.getString(R.string.download_manager_cancel)
                        onDownloadListeners.filterIsInstance<OnDownloadListener>().forEach {
                            it.onDownloadCanceled(id, info)
                        }
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

    fun addListener(listener: BaseOnDownloadListener) {
        onDownloadListeners.add(listener)
    }

    fun removeListener(listener: BaseOnDownloadListener) {
        onDownloadListeners.remove(listener)
    }
}