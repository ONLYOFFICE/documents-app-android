package app.editors.manager.managers.works

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.work.Data
import androidx.work.Worker
import androidx.work.WorkerParameters
import app.editors.manager.app.App
import app.editors.manager.managers.receivers.DownloadReceiver
import app.editors.manager.managers.utils.NotificationUtils
import lib.toolkit.base.managers.utils.FileUtils

open class BaseStorageDownloadWork(context: Context, workerParameters: WorkerParameters): Worker(context, workerParameters) {

    companion object {
        private val TAG = BaseStorageDownloadWork::class.java.simpleName

        const val FILE_ID_KEY = "FILE_ID_KEY"
        const val FILE_URI_KEY = "FILE_URI_KEY"

        private const val LOAD_PROGRESS_UPDATE = 15
    }


    protected val notificationUtils: NotificationUtils = NotificationUtils(applicationContext, TAG)
    protected var file: DocumentFile? = null
    protected var id: String? = null
    protected var to: Uri = Uri.EMPTY
    private var timeMark = 0L
    protected var data: Data? = null
    override fun doWork() = Result.success()

    fun showProgress(total: Long, progress: Long, isArchiving: Boolean) {
        val deltaTime = System.currentTimeMillis() - timeMark
        if (deltaTime > LOAD_PROGRESS_UPDATE) {
            timeMark = System.currentTimeMillis()
            val percent = FileUtils.getPercentOfLoading(total, progress)
            val id = id.hashCode()
            val tag = getId().toString()
            if (!isArchiving) {
                file?.name?.let { name -> notificationUtils.showProgressNotification(id, tag, name, percent) }
            } else {
                file?.name?.let { name ->
                    notificationUtils.showArchivingProgressNotification(id, tag,
                        name, percent)
                }
            }
        }
    }

    fun sendBroadcastDownloadComplete(
        id: String?, url: String?, title: String?,
        path: String?, mime: String?, uri: Uri?
    ) {
        val intent = Intent(DownloadReceiver.DOWNLOAD_ACTION_COMPLETE)
        intent.putExtra(DownloadReceiver.EXTRAS_KEY_ID, id)
        intent.putExtra(DownloadReceiver.EXTRAS_KEY_URL, url)
        intent.putExtra(DownloadReceiver.EXTRAS_KEY_TITLE, title)
        intent.putExtra(DownloadReceiver.EXTRAS_KEY_PATH, path)
        intent.putExtra(DownloadReceiver.EXTRAS_KEY_MIME_TYPE, mime)
        intent.putExtra(DownloadReceiver.EXTRAS_KEY_URI, uri.toString())
        LocalBroadcastManager.getInstance(App.getApp()).sendBroadcast(intent)
    }

    fun sendBroadcastUnknownError(id: String?, url: String?, title: String?, uri: Uri?) {
        val intent = Intent(DownloadReceiver.DOWNLOAD_ACTION_ERROR)
        intent.putExtra(DownloadReceiver.EXTRAS_KEY_ID, id)
        intent.putExtra(DownloadReceiver.EXTRAS_KEY_URL, url)
        intent.putExtra(DownloadReceiver.EXTRAS_KEY_TITLE, title)
        intent.putExtra(DownloadReceiver.EXTRAS_KEY_URI, uri.toString())
        LocalBroadcastManager.getInstance(App.getApp()).sendBroadcast(intent)
    }

    protected open fun getArgs() {
        data = inputData
        to = Uri.parse(data?.getString(FILE_URI_KEY))
        file = DocumentFile.fromSingleUri(applicationContext, to)
        id = data?.getString(FILE_ID_KEY)
    }

}