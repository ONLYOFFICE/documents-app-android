package app.editors.manager.storages.dropbox.managers.works

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import app.editors.manager.app.App
import app.editors.manager.app.getDropboxServiceProvider
import app.editors.manager.storages.dropbox.mvp.models.request.PathRequest
import app.editors.manager.managers.receivers.DownloadReceiver
import app.editors.manager.managers.utils.NewNotificationUtils
import app.editors.manager.managers.works.DownloadWork
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import lib.toolkit.base.managers.utils.FileUtils
import lib.toolkit.base.managers.utils.PathUtils
import lib.toolkit.base.managers.utils.StringUtils

class DownloadWork(context: Context, workerParameters: WorkerParameters): Worker(context, workerParameters) {

    companion object {
        private val TAG = DownloadWork::class.java.simpleName

        const val FILE_ID_KEY = "FILE_ID_KEY"
        const val FILE_URI_KEY = "FILE_URI_KEY"
        const val DOWNLOADABLE_ITEM_KEY = "DOWNLOADABLE_ITEM_KEY"

        const val DOWNLOADABLE_ITEM_FILE = "file"
        const val DOWNLOADABLE_ITEM_FOLDER = "folder"

        private const val KEY_ERROR_INFO = "error"
        private const val KEY_ERROR_INFO_MESSAGE = "message"

        private const val LOAD_PROGRESS_UPDATE = 15

        const val DOWNLOAD_ZIP_NAME = "dropbox.zip"

    }

    private val notificationUtils: NewNotificationUtils = NewNotificationUtils(applicationContext, TAG)
    private var file: DocumentFile? = null
    private var id: String? = null
    private var to: Uri? = null
    private var timeMark = 0L
    private var downloadableItem = ""

    override fun doWork(): Result {
        getArgs()
        val request = id?.let { PathRequest( path = it) }
        val response = if (downloadableItem == DOWNLOADABLE_ITEM_FILE)
            applicationContext.getDropboxServiceProvider().download(Json.encodeToString(request))
                .blockingGet() else
            applicationContext.getDropboxServiceProvider().downloadFolder(Json.encodeToString(request))
                .blockingGet()
        response.body()?.let { responseBody ->
            FileUtils.writeFromResponseBody(responseBody, to!!, applicationContext, object: FileUtils.Progress {
                override fun onProgress(total: Long, progress: Long): Boolean {
                    showProgress(total, progress, false)
                    return isStopped
                }

            }, object: FileUtils.Finish {
                override fun onFinish() {
                    notificationUtils.removeNotification(id.hashCode())
                    notificationUtils.showCompleteNotification(id.hashCode(), file!!.name, to)
                    sendBroadcastDownloadComplete(
                        id,
                        "",
                        file?.name,
                        PathUtils.getPath(applicationContext, to ?: Uri.EMPTY),
                        StringUtils.getMimeTypeFromPath(
                            file?.name ?: ""
                        ),
                        to
                    )
                }

            }, object: FileUtils.Error {
                override fun onError(message: String) {
                    notificationUtils.removeNotification(id.hashCode())
                    if (isStopped) {
                        notificationUtils.showCanceledNotification(id.hashCode(), file?.name)
                    } else {
                        notificationUtils.showErrorNotification(id.hashCode(), file?.name)
                        sendBroadcastUnknownError(id, "", file?.name, to)
                    }
                    file?.delete()
                }
            })
        }
        return Result.success()
    }

    private fun getArgs() {
        val data = inputData
        to = Uri.parse(data.getString(FILE_URI_KEY))
        file = DocumentFile.fromSingleUri(applicationContext, to!!)
        id = data.getString(FILE_ID_KEY)
        downloadableItem = data.getString(DOWNLOADABLE_ITEM_KEY).toString()
    }

    private fun showProgress(total: Long, progress: Long, isArchiving: Boolean) {
        val deltaTime = System.currentTimeMillis() - timeMark
        if (deltaTime > LOAD_PROGRESS_UPDATE) {
            timeMark = System.currentTimeMillis()
            val percent = FileUtils.getPercentOfLoading(total, progress)
            val id = id.hashCode()
            val tag = getId().toString()
            if (!isArchiving) {
                notificationUtils.showProgressNotification(id, tag, file!!.name!!, percent)
            } else {
                notificationUtils.showArchivingProgressNotification(id, tag, file!!.name!!, percent)
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

    fun sendBroadcastError(id: String?, url: String?, title: String?, error: String?) {
        val intent = Intent(DownloadReceiver.DOWNLOAD_ACTION_ERROR)
        intent.putExtra(DownloadReceiver.EXTRAS_KEY_ID, id)
        intent.putExtra(DownloadReceiver.EXTRAS_KEY_URL, url)
        intent.putExtra(DownloadReceiver.EXTRAS_KEY_TITLE, title)
        intent.putExtra(DownloadReceiver.EXTRAS_KEY_ERROR, error)
        LocalBroadcastManager.getInstance(App.getApp()).sendBroadcast(intent)
    }
}