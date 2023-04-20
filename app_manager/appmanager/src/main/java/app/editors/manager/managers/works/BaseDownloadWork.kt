package app.editors.manager.managers.works

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.work.Data
import androidx.work.Worker
import androidx.work.WorkerParameters
import app.editors.manager.R
import app.editors.manager.app.App
import app.editors.manager.managers.receivers.DownloadReceiver
import app.editors.manager.managers.utils.FirebaseUtils
import app.editors.manager.managers.utils.NotificationUtils
import lib.toolkit.base.managers.utils.FileUtils
import lib.toolkit.base.managers.utils.PathUtils
import lib.toolkit.base.managers.utils.StringUtils
import okhttp3.ResponseBody
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

sealed class DownloadException(val errorMessage: Int?) : IOException() {
    object NotEnoughFreeSpace : DownloadException(R.string.download_manager_error_free_space)
    class ExceedFileSize(errorMessage: Int) : DownloadException(errorMessage)
    class Unknown(val errorString: String? = null) : DownloadException(null)
}

abstract class BaseDownloadWork(
    context: Context,
    workerParams: WorkerParameters
) : Worker(context, workerParams) {

    companion object {
        private val TAG = BaseDownloadWork::class.java.simpleName

        const val FILE_ID_KEY = "FILE_ID_KEY"
        const val FILE_URI_KEY = "FILE_URI_KEY"
        const val URL_KEY = "URL_KEY"
        const val REQUEST_DOWNLOAD = "REQUEST_DOWNLOAD"
    }

    protected val notificationUtils: NotificationUtils = NotificationUtils(applicationContext, TAG)

    protected var data: Data? = null
    protected var file: DocumentFile? = null
    protected var id: String? = null
    protected var to: Uri = Uri.EMPTY
    private var timeMark = 0L

    abstract fun download(): Response<ResponseBody>

    override fun doWork(): Result {
        getArgs()
        writeFromResponse(download())
        return Result.success()
    }

    protected open fun writeFromResponse(response: Response<ResponseBody>) {
        try {
            val responseBody = response.body()
            if (response.isSuccessful && responseBody != null) {
                if (!FileUtils.isEnoughFreeSpace(responseBody.contentLength())) {
                    onError(DownloadException.NotEnoughFreeSpace)
                } else {
                    FileUtils.writeFromResponseBody(
                        response = responseBody,
                        to = to,
                        context = applicationContext,
                        onProgress = ::showProgress,
                        onFinish = ::onFinish,
                        onError = ::onError
                    )
                }
            } else {
                throw HttpException(response)
            }
        } catch (error: Throwable) {
            onError(error)
        }
    }

    protected fun showProgress(total: Long, progress: Long, isArchiving: Boolean): Boolean {
        val deltaTime = System.currentTimeMillis() - timeMark
        if (deltaTime > FileUtils.LOAD_PROGRESS_UPDATE) {
            timeMark = System.currentTimeMillis()
            val percent = FileUtils.getPercentOfLoading(total, progress)
            val id = id.hashCode()
            val tag = getId().toString()
            if (!isArchiving) {
                notificationUtils.showProgressNotification(id, tag, file?.name.orEmpty(), percent)
            } else {
                notificationUtils.showArchivingProgressNotification(
                    id,
                    tag,
                    file?.name.orEmpty(),
                    percent
                )
            }
        }
        return isStopped
    }

    protected fun onError(error: Throwable) {
        if (isStopped) {
            notificationUtils.showCanceledNotification(id.hashCode(), file?.name)
        } else {
            sendBroadcastError(error)
            FirebaseUtils.addCrash(error)
            notificationUtils.removeNotification(id.hashCode())
            notificationUtils.showErrorNotification(id.hashCode(), file?.name)
            file?.delete()
        }
    }

    protected fun getArgs() {
        data = inputData
        to = Uri.parse(data?.getString(FILE_URI_KEY))
        file = DocumentFile.fromSingleUri(applicationContext, to)
        id = data?.getString(FILE_ID_KEY)
    }

    protected fun onFinish() {
        notificationUtils.removeNotification(id.hashCode())
        notificationUtils.showCompleteNotification(id.hashCode(), file?.name, to)
        sendBroadcastDownloadComplete(
            id = id,
            title = file?.name,
            path = PathUtils.getPath(applicationContext, to),
            mime = StringUtils.getMimeTypeFromPath(file?.name.orEmpty()),
            uri = to
        )
    }

    private fun sendBroadcastDownloadComplete(
        id: String?,
        title: String?,
        path: String?,
        mime: String?,
        uri: Uri?
    ) {
        with(Intent(DownloadReceiver.DOWNLOAD_ACTION_COMPLETE)) {
            putExtra(DownloadReceiver.EXTRAS_KEY_ID, id)
            putExtra(DownloadReceiver.EXTRAS_KEY_TITLE, title)
            putExtra(DownloadReceiver.EXTRAS_KEY_PATH, path)
            putExtra(DownloadReceiver.EXTRAS_KEY_MIME_TYPE, mime)
            putExtra(DownloadReceiver.EXTRAS_KEY_URI, uri.toString())
            LocalBroadcastManager.getInstance(App.getApp()).sendBroadcast(this)
        }
    }

    private fun sendBroadcastError(errorMessage: String? = null) {
        val intent = Intent(DownloadReceiver.DOWNLOAD_ACTION_ERROR)
        intent.putExtra(DownloadReceiver.EXTRAS_KEY_ERROR, errorMessage)
        LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(intent)
    }

    private fun sendBroadcastError(error: Throwable) {
        when (error) {
            is DownloadException.Unknown -> sendBroadcastError(error.errorString)
            is DownloadException -> sendBroadcastError(error.errorMessage?.let(applicationContext::getString))
            else -> sendBroadcastError()
        }
    }
}
