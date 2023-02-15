package app.editors.manager.managers.works

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import app.editors.manager.app.App
import app.editors.manager.app.api
import app.editors.manager.managers.receivers.UploadReceiver
import app.documents.core.network.login.models.request.ProgressRequestBody
import app.editors.manager.managers.utils.NotificationUtils
import app.documents.core.network.manager.models.explorer.CloudFile
import app.documents.core.network.manager.models.explorer.UploadFile
import app.documents.core.network.manager.models.response.ResponseFile
import lib.toolkit.base.managers.utils.ContentResolverUtils
import lib.toolkit.base.managers.utils.FileUtils
import lib.toolkit.base.managers.utils.NetworkUtils
import okhttp3.Headers
import okhttp3.MultipartBody
import retrofit2.Call
import java.io.IOException
import java.util.*

class UploadWork(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {

    companion object {
        val TAG: String = UploadWork::class.java.simpleName

        const val TAG_UPLOAD_FILES = "TAG_UPLOAD_FILES"
        const val TAG_FOLDER_ID = "TAG_FOLDER_ID"
        const val ACTION_UPLOAD = "ACTION_UPLOAD"
        const val ACTION_UPLOAD_MY = "ACTION_UPLOAD_MY"

        private const val HEADER_NAME = "Content-Disposition"

        private val mapUploadFiles = Collections.synchronizedMap(HashMap<String, ArrayList<UploadFile>>())

        @JvmStatic
        fun getUploadFiles(id: String?): ArrayList<UploadFile>? {
            return mapUploadFiles[id]
        }

        @JvmStatic
        fun putNewUploadFiles(id: String?, uploadFiles: ArrayList<UploadFile>) {
            val oldFiles = mapUploadFiles[id]
            if (oldFiles != null) {
                uploadFiles.removeAll(oldFiles)
                oldFiles.addAll(uploadFiles)
                mapUploadFiles[id] = oldFiles
            } else {
                mapUploadFiles[id] = uploadFiles
            }
        }
    }

    private val notificationUtils: NotificationUtils = NotificationUtils(applicationContext, TAG)
    private var action: String? = null
    private var path: String? = null
    private var folderId: String? = null
    private var title: String? = null
    private var from: Uri? = null
    private var timeMark = 0L

    private val headers: Headers by lazy {
        Headers.Builder()
            .addUnsafeNonAscii(HEADER_NAME, "form-data; name=$title; filename=$title")
            .build()
    }
    private var call: Call<ResponseFile>? = null

    override fun doWork(): Result {
        getArgs()
        val api = applicationContext.api
        val responseFile: ResponseFile
        path = from?.path
        title = ContentResolverUtils.getName(applicationContext, from ?: Uri.EMPTY)

        call = if (action == ACTION_UPLOAD_MY) {
            createMultipartBody(from)?.let { api.uploadFileToMy(it) }
        } else {
            createMultipartBody(from)?.let { api.uploadFile(folderId ?: "", it) }
        }
        try {
            val response = call?.execute()
            if (response?.isSuccessful == true && response.body() != null) {
                responseFile = response.body()!!
                notificationUtils.removeNotification(id.hashCode())
                notificationUtils.showUploadCompleteNotification(id.hashCode(), title)
                sendBroadcastUploadComplete(path, title!!, responseFile.response, path)
                removeUploadFile(from)
            } else {
                notificationUtils.showUploadErrorNotification(id.hashCode(), title)
                sendBroadcastUnknownError(title!!, path)
                if (!NetworkUtils.isOnline(applicationContext)) {
                    return Result.retry()
                } else {
                    removeUploadFile(from)
                }
            }
        } catch (e: IOException) {
            if (isStopped) {
                notificationUtils.showCanceledUploadNotification(id.hashCode(), title)
                sendBroadcastUploadCanceled(path)
                removeUploadFile(from)
            } else {
                notificationUtils.showUploadErrorNotification(id.hashCode(), title)
                sendBroadcastUnknownError(title!!, path)
                if (!NetworkUtils.isOnline(applicationContext)) {
                    return Result.retry()
                } else {
                    removeUploadFile(from)
                }
            }
        }
        return Result.success()
    }

    private fun getArgs() {
        inputData.let {
            action = it.getString(ACTION_UPLOAD_MY)
            from = Uri.parse(it.getString(TAG_UPLOAD_FILES))
            folderId = it.getString(TAG_FOLDER_ID)
        }
    }

    private fun createMultipartBody(uri: Uri?): MultipartBody.Part? {
        return title?.let { MultipartBody.Part.createFormData(it, it, createRequestBody(uri)) }
    }

    private fun createRequestBody(uri: Uri?): ProgressRequestBody {
        val requestBody = ProgressRequestBody(App.getApp(), uri ?: Uri.EMPTY)
        requestBody.setOnUploadCallbacks { total: Long, progress: Long ->
            if (!isStopped) {
                showProgress(total, progress)
            } else {
                call?.cancel()
            }
        }
        return requestBody
    }

    private fun removeUploadFile(completeFile: Uri?) {
        val files = getUploadFiles(folderId)
        if (files?.isEmpty() == true) {
            return
        }
        files?.let {
            for (file in files) {
                if (file.id == completeFile!!.path) {
                    files.remove(file)
                    break
                }
            }
            putUploadFiles(folderId, files)
        }
    }

    private fun showProgress(total: Long, progress: Long) {
        val deltaTime = System.currentTimeMillis() - timeMark
        if (deltaTime > FileUtils.LOAD_PROGRESS_UPDATE) {
            timeMark = System.currentTimeMillis()
            val percent = FileUtils.getPercentOfLoading(total, progress)
            val id = id.hashCode()
            val tag = getId().toString()
            notificationUtils.showUploadProgressNotification(id, tag, title!!, percent)
            sendBroadcastProgress(percent, path, folderId)
        }
    }

    private fun putUploadFiles(id: String?, uploadFiles: ArrayList<UploadFile>) {
        mapUploadFiles[id] = uploadFiles
    }

    private fun sendBroadcastUnknownError(title: String, uploadFile: String?) {
        val intent = Intent(UploadReceiver.UPLOAD_ACTION_ERROR)
        intent.putExtra(UploadReceiver.EXTRAS_KEY_FILE, uploadFile)
        intent.putExtra(UploadReceiver.EXTRAS_KEY_TITLE, title)
        LocalBroadcastManager.getInstance(App.getApp()).sendBroadcast(intent)
    }

    private fun sendBroadcastUploadComplete(path: String?, title: String, file: CloudFile, id: String?) {
        val intent = Intent(UploadReceiver.UPLOAD_ACTION_COMPLETE)
        intent.putExtra(UploadReceiver.EXTRAS_KEY_PATH, path)
        intent.putExtra(UploadReceiver.EXTRAS_KEY_ID, id)
        intent.putExtra(UploadReceiver.EXTRAS_KEY_TITLE, title)
        intent.putExtra(UploadReceiver.EXTRAS_KEY_FILE, file)
        LocalBroadcastManager.getInstance(App.getApp()).sendBroadcast(intent)
    }

    private fun sendBroadcastUploadCanceled(path: String?) {
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

}