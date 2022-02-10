package app.editors.manager.storages.onedrive.managers.works

import android.content.Context
import android.net.Uri
import androidx.work.WorkerParameters
import app.editors.manager.app.getOneDriveServiceProvider
import app.editors.manager.storages.onedrive.onedrive.OneDriveResponse
import app.editors.manager.storages.base.work.BaseStorageDownloadWork
import lib.toolkit.base.managers.utils.FileUtils
import lib.toolkit.base.managers.utils.PathUtils
import lib.toolkit.base.managers.utils.StringUtils
import okhttp3.ResponseBody

class DownloadWork(context: Context, workerParameters: WorkerParameters): BaseStorageDownloadWork(context, workerParameters) {

    companion object {
        private val TAG = DownloadWork::class.java.simpleName


        const val DOWNLOAD_ZIP_NAME = "onedrive.zip"
    }

    override fun doWork(): Result {
        getArgs()
        val response = id?.let { applicationContext.getOneDriveServiceProvider().download(it).blockingGet() }
        if (response is OneDriveResponse.Success) {
            FileUtils.writeFromResponseBody((response.response as ResponseBody), to!!, applicationContext, object: FileUtils.Progress {
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
        } else if(response is OneDriveResponse.Error){
            throw response.error
        }


        return Result.success()
    }
}