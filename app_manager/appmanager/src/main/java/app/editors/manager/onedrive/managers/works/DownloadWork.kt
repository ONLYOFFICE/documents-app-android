package app.editors.manager.onedrive.managers.works

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.documentfile.provider.DocumentFile
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import app.documents.core.network.ApiContract
import app.editors.manager.R
import app.editors.manager.app.App
import app.editors.manager.app.getOneDriveServiceProvider
import app.editors.manager.onedrive.onedrive.OneDriveResponse
import app.editors.manager.managers.receivers.DownloadReceiver
import app.editors.manager.managers.utils.FirebaseUtils
import app.editors.manager.managers.utils.NewNotificationUtils
import app.editors.manager.managers.works.DownloadWork
import lib.toolkit.base.managers.utils.FileUtils
import lib.toolkit.base.managers.utils.PathUtils
import lib.toolkit.base.managers.utils.StringUtils
import okhttp3.ResponseBody
import org.json.JSONException

class DownloadWork(context: Context, workerParameters: WorkerParameters): Worker(context, workerParameters) {

    companion object {
        private val TAG = DownloadWork::class.java.simpleName

        const val FILE_ID_KEY = "FILE_ID_KEY"
        const val FILE_URI_KEY = "FILE_URI_KEY"

        private const val KEY_ERROR_INFO = "error"
        private const val KEY_ERROR_INFO_MESSAGE = "message"

        const val DOWNLOAD_ZIP_NAME = "download.zip"

        fun sendBroadcastDownloadComplete(
            id: String?, url: String?, title: String?,
            path: String?, mime: String?
        ) {
            val intent = Intent(DownloadReceiver.DOWNLOAD_ACTION_COMPLETE)
            intent.putExtra(DownloadReceiver.EXTRAS_KEY_ID, id)
            intent.putExtra(DownloadReceiver.EXTRAS_KEY_URL, url)
            intent.putExtra(DownloadReceiver.EXTRAS_KEY_TITLE, title)
            intent.putExtra(DownloadReceiver.EXTRAS_KEY_PATH, path)
            intent.putExtra(DownloadReceiver.EXTRAS_KEY_MIME_TYPE, mime)
            LocalBroadcastManager.getInstance(App.getApp()).sendBroadcast(intent)
        }

        fun sendBroadcastUnknownError(id: String?, url: String?, title: String?) {
            val intent = Intent(DownloadReceiver.DOWNLOAD_ACTION_ERROR)
            intent.putExtra(DownloadReceiver.EXTRAS_KEY_ID, id)
            intent.putExtra(DownloadReceiver.EXTRAS_KEY_URL, url)
            intent.putExtra(DownloadReceiver.EXTRAS_KEY_TITLE, title)
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

    private val notificationUtils: NewNotificationUtils = NewNotificationUtils(applicationContext, TAG)
    private var file: DocumentFile? = null
    private var id: String? = null
    private var to: Uri? = null
    private var timeMark = 0L

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
                        )
                    )
                }

            }, object: FileUtils.Error {
                override fun onError(message: String) {
                    notificationUtils.removeNotification(id.hashCode())
                    if (isStopped) {
                        notificationUtils.showCanceledNotification(id.hashCode(), file?.name)
                    } else {
                        notificationUtils.showErrorNotification(id.hashCode(), file?.name)
                        sendBroadcastUnknownError(id, "", file?.name)
                    }
                    file?.delete()
                }
            })
        } else if(response is OneDriveResponse.Error){
            throw response.error
        }


        return Result.success()
    }

    private fun showProgress(total: Long, progress: Long, isArchiving: Boolean) {
        val deltaTime = System.currentTimeMillis() - timeMark
        if (deltaTime > 25) {
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

    private fun onError(responseBody: ResponseBody?) {
        val errorMessage: String
        var responseMessage: String? = null
        responseMessage = try {
            responseBody?.string()
        } catch (e: Exception) {
            sendBroadcastUnknownError(id, "", file!!.name)
            file?.delete()
            return
        }
        if (responseMessage != null) {
            val jsonObject = StringUtils.getJsonObject(responseMessage)
            if (jsonObject != null) {
                try {
                    errorMessage = jsonObject.getJSONObject(KEY_ERROR_INFO).getString(
                        KEY_ERROR_INFO_MESSAGE
                    )
                    DownloadWork.sendBroadcastError(id, "", file?.name, errorMessage)
                } catch (e: JSONException) {
                    Log.e(TAG, "onErrorHandle()", e)
                    FirebaseUtils.addCrash(e)
                }
            } else {
                DownloadWork.sendBroadcastUnknownError(id, "", file?.name)
                file?.delete()
                return
            }
        }
    }

    private fun onError(errorMessage: String) {
        when (errorMessage) {
            ApiContract.Errors.EXCEED_FILE_SIZE_100 -> DownloadWork.sendBroadcastError(
                id,
                "",
                file?.name,
                applicationContext.getString(R.string.download_manager_exceed_size_100)
            )
            ApiContract.Errors.EXCEED_FILE_SIZE_25 -> DownloadWork.sendBroadcastError(
                id,
                "",
                file?.name,
                applicationContext.getString(R.string.download_manager_exceed_size_25)
            )
            else -> DownloadWork.sendBroadcastError(id, "", file?.name, errorMessage)
        }
    }

    private fun getArgs() {
        val data = inputData
        to = Uri.parse(data.getString(FILE_URI_KEY))
        file = DocumentFile.fromSingleUri(applicationContext, to!!)
        id = data.getString(FILE_ID_KEY)
    }
}