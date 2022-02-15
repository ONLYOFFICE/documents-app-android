package app.editors.manager.googledrive.managers.works

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import app.editors.manager.app.App
import app.editors.manager.app.getGoogleDriveServiceProvider
import app.editors.manager.googledrive.mvp.models.request.CreateItemRequest
import app.editors.manager.googledrive.ui.fragments.DocsGoogleDriveFragment.Companion.KEY_CREATE
import app.editors.manager.googledrive.ui.fragments.DocsGoogleDriveFragment.Companion.KEY_UPDATE
import app.editors.manager.googledrive.ui.fragments.DocsGoogleDriveFragment.Companion.KEY_UPLOAD
import app.editors.manager.managers.receivers.UploadReceiver
import app.editors.manager.managers.utils.NotificationUtils
import app.editors.manager.mvp.models.explorer.CloudFile
import lib.toolkit.base.managers.utils.FileUtils
import lib.toolkit.base.managers.utils.PathUtils
import java.io.DataOutputStream
import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.URL
import kotlin.math.min

class UploadWork(context: Context, workerParameters: WorkerParameters): Worker(context, workerParameters) {


    companion object {
        val TAG: String = UploadWork::class.java.simpleName

        const val KEY_FROM = "KEY_FROM"
        const val KEY_FOLDER_ID = "KEY_FOLDER_ID"
        const val KEY_TAG = "KEY_TAG"
        const val KEY_FILE_ID = "KEY_FILE_ID"
        const val HEADER_LOCATION = "Location"

        private const val HEADER_NAME = "Content-Disposition"
    }

    private val mNotificationUtils: NotificationUtils = NotificationUtils(applicationContext, app.editors.manager.onedrive.managers.works.UploadWork.TAG)
    private var path: String? = null
    private var folderId: String? = null
    private var from: Uri? = null
    private var timeMark = 0L
    private var file: DocumentFile? = null
    private var tag = ""
    private var fileName = ""
    private var fileId: String? = null

    override fun doWork(): Result {
        getArgs()

        when(tag) {
            KEY_UPLOAD -> {
                fileName = file?.name!!
            }
            KEY_UPDATE, KEY_CREATE -> {
                fileName = path?.let { FileUtils.getFileName(it, true) }!!
            }
        }

        val request = CreateItemRequest(
            name = fileName,
            parents = listOf(folderId!!)
        )

        val response = when(tag) {
            KEY_UPLOAD, KEY_CREATE -> applicationContext.getGoogleDriveServiceProvider().upload(request).blockingGet()
            KEY_UPDATE -> fileId?.let { applicationContext.getGoogleDriveServiceProvider().update(it).blockingGet() }
            else -> applicationContext.getGoogleDriveServiceProvider().upload(request).blockingGet()
        }

        if(response?.isSuccessful == true) {
            from?.let { from -> response.headers().get(HEADER_LOCATION)?.let { url -> uploadSession(url, from) } }
        } else {
            mNotificationUtils.showUploadErrorNotification(id.hashCode(), fileName)
            sendBroadcastUnknownError(fileName, path)
        }

        return Result.success()
    }


    private fun uploadSession(url: String, uri: Uri) {
        val connection = URL(url).openConnection() as HttpURLConnection
        val fileInputStream = App.getApp().contentResolver.openInputStream(uri)
        val maxBufferSize = 2 * 1024 * 1024
        var outputStream: OutputStream? = null
        val bytesAvailable = fileInputStream?.available()
        connection.doInput = true
        connection.doOutput = true
        connection.useCaches = false
        connection.requestMethod = "PUT"
        connection.setRequestProperty("Connection", "Keep-Alive")
        connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8")
        try {
            outputStream = DataOutputStream(connection.outputStream)
            val bufferSize = min(bytesAvailable!!, maxBufferSize)
            val buffer = ByteArray(bufferSize)
            var count = 0
            var bytesRead = 0
            while (fileInputStream.read(buffer).also { bytesRead = it } != -1) {
                outputStream.write(buffer, 0, bytesRead)
                count += bytesRead
                if (tag == KEY_UPLOAD) {
                    count.toLong().let { progress ->
                        file?.length()?.let { total -> showProgress(total, progress) }
                    }
                }
            }
            if (connection.responseCode == 200 || connection.responseCode == 201) {
                mNotificationUtils.removeNotification(id.hashCode())
                if (tag == KEY_UPLOAD) {
                    mNotificationUtils.showUploadCompleteNotification(id.hashCode(), fileName)
                    sendBroadcastUploadComplete(path, fileName, CloudFile(), path)
                }
            } else {
                mNotificationUtils.removeNotification(id.hashCode())
                mNotificationUtils.showUploadErrorNotification(id.hashCode(), fileName)
                sendBroadcastUnknownError(fileName, path)
            }
        } catch (e: Exception) {
            mNotificationUtils.showUploadErrorNotification(id.hashCode(), fileName)
            sendBroadcastUnknownError(fileName, path)
            throw e
        }
    }

    private fun showProgress(total: Long, progress: Long) {
        val deltaTime = System.currentTimeMillis() - timeMark
        if (deltaTime > FileUtils.LOAD_PROGRESS_UPDATE) {
            timeMark = System.currentTimeMillis()
            val percent = FileUtils.getPercentOfLoading(total, progress)
            val id = id.hashCode()
            val tag = getId().toString()
            mNotificationUtils.showUploadProgressNotification(id, tag, fileName, percent)
            sendBroadcastProgress(percent, path, folderId)
        }
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

    private fun getArgs() {
        inputData.let {
            from = Uri.parse(it.getString(KEY_FROM))
            folderId = it.getString(KEY_FOLDER_ID)
            tag = it.getString(KEY_TAG)!!
            fileId = it.getString(KEY_FILE_ID)
        }
        file = from?.let { DocumentFile.fromSingleUri(applicationContext, it) }
        path = from?.let { PathUtils.getPath(applicationContext, it) }
    }
}

