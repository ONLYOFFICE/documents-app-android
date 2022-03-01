package app.editors.manager.storages.base.work

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.work.Data
import androidx.work.Worker
import androidx.work.WorkerParameters
import app.editors.manager.app.App
import app.editors.manager.managers.receivers.UploadReceiver
import app.editors.manager.managers.utils.NotificationUtils
import app.editors.manager.mvp.models.explorer.CloudFile
import lib.toolkit.base.managers.utils.FileUtils

open class BaseStorageUploadWork(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {

    companion object {
        private val TAG = BaseStorageUploadWork::class.java.simpleName

        const val TAG_UPLOAD_FILES = "TAG_UPLOAD_FILES"
        const val TAG_FOLDER_ID = "TAG_FOLDER_ID"
        const val KEY_TAG = "KEY_TAG"
    }

    protected val mNotificationUtils: NotificationUtils = NotificationUtils(applicationContext, TAG)
    protected var tag: String? = null
    protected var path: String? = null
    protected var folderId: String? = null
    protected var title: String? = null
    protected var from: Uri? = null
    private var timeMark = 0L
    protected var data: Data? = null

    override fun doWork() = Result.success()

    protected fun showProgress(total: Long, progress: Long) {
        val deltaTime = System.currentTimeMillis() - timeMark
        if (deltaTime > FileUtils.LOAD_PROGRESS_UPDATE) {
            timeMark = System.currentTimeMillis()
            val percent = FileUtils.getPercentOfLoading(total, progress)
            val id = id.hashCode()
            val tag = getId().toString()
            mNotificationUtils.showUploadProgressNotification(id, tag, title!!, percent)
            sendBroadcastProgress(percent, path, folderId)
        }
    }

    protected fun sendBroadcastUnknownError(title: String, uploadFile: String?) {
        val intent = Intent(UploadReceiver.UPLOAD_ACTION_ERROR)
        intent.putExtra(UploadReceiver.EXTRAS_KEY_FILE, uploadFile)
        intent.putExtra(UploadReceiver.EXTRAS_KEY_TITLE, title)
        LocalBroadcastManager.getInstance(App.getApp()).sendBroadcast(intent)
    }

    protected fun sendBroadcastUploadComplete(path: String?, title: String, file: CloudFile, id: String?) {
        val intent = Intent(UploadReceiver.UPLOAD_ACTION_COMPLETE)
        intent.putExtra(UploadReceiver.EXTRAS_KEY_PATH, path)
        intent.putExtra(UploadReceiver.EXTRAS_KEY_ID, id)
        intent.putExtra(UploadReceiver.EXTRAS_KEY_TITLE, title)
        intent.putExtra(UploadReceiver.EXTRAS_KEY_FILE, file)
        LocalBroadcastManager.getInstance(App.getApp()).sendBroadcast(intent)
    }

    protected fun sendBroadcastUploadCanceled(path: String?) {
        val intent = Intent(UploadReceiver.UPLOAD_ACTION_CANCELED)
        intent.putExtra(UploadReceiver.EXTRAS_KEY_PATH, path)
        intent.putExtra(UploadReceiver.EXTRAS_KEY_ID, path)
        LocalBroadcastManager.getInstance(App.getApp()).sendBroadcast(intent)
    }

    private fun sendBroadcastProgress(progress: Int, file: String?, folderId: String?) {
        val intent = Intent(UploadReceiver.UPLOAD_ACTION_PROGRESS)
        intent.putExtra(UploadReceiver.EXTRAS_KEY_FILE, file)
        intent.putExtra(UploadReceiver.EXTRAS_FOLDER_ID, folderId)
        intent.putExtra(UploadReceiver.EXTRAS_KEY_PROGRESS, progress)
        LocalBroadcastManager.getInstance(App.getApp()).sendBroadcast(intent)
    }
    open fun getArgs() {
        data = inputData
        tag = data?.getString(KEY_TAG)
        from = Uri.parse(data?.getString(TAG_UPLOAD_FILES))
        folderId = data?.getString(TAG_FOLDER_ID)
    }
}