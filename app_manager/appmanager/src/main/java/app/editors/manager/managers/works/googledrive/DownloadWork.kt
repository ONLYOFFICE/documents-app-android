package app.editors.manager.managers.works.googledrive

import android.content.Context
import android.net.Uri
import androidx.work.WorkerParameters
import app.editors.manager.app.googleDriveProvider
import app.editors.manager.managers.works.BaseStorageDownloadWork
import lib.toolkit.base.managers.utils.FileUtils
import lib.toolkit.base.managers.utils.PathUtils
import lib.toolkit.base.managers.utils.StringUtils

class DownloadWork(context: Context, workerParameters: WorkerParameters): BaseStorageDownloadWork(context, workerParameters) {

    companion object {
        private val TAG = DownloadWork::class.java.simpleName

        const val DOWNLOADABLE_ITEM_KEY = "DOWNLOADABLE_ITEM_KEY"

        const val DOWNLOADABLE_ITEM_FILE = "file"
        const val DOWNLOADABLE_ITEM_FOLDER = "folder"

        const val DOWNLOAD_ZIP_NAME = "googledrive.zip"

    }

    private var downloadableItem = ""

    override fun getArgs() {
        super.getArgs()
        downloadableItem = data?.getString(DOWNLOADABLE_ITEM_KEY).toString()
    }

    override fun doWork(): Result {
        getArgs()

        val response = applicationContext.googleDriveProvider.download(id ?: "").blockingGet()
        response.body()?.let { body ->
            FileUtils.writeFromResponseBody(body, to!!, applicationContext, object: FileUtils.Progress {
                override fun onProgress(total: Long, progress: Long): Boolean {
                    showProgress(total, progress, false)
                    return isStopped
                }

            }, object: FileUtils.Finish {
                override fun onFinish() {
                    notificationUtils.removeNotification(id.hashCode())
                    notificationUtils.showCompleteNotification(id.hashCode(), file?.name, to)
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
        response?.errorBody()?.let {
            notificationUtils.removeNotification(id.hashCode())
            notificationUtils.showErrorNotification(id.hashCode(), file?.name)
            sendBroadcastUnknownError(id, "", file?.name, to)
            file?.delete()
        }
        return Result.success()
    }
}