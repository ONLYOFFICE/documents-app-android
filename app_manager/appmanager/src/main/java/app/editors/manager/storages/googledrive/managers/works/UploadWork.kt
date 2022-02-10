package app.editors.manager.storages.googledrive.managers.works

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import app.editors.manager.app.App
import app.editors.manager.app.getGoogleDriveServiceProvider
import app.editors.manager.storages.googledrive.mvp.models.request.CreateItemRequest
import app.editors.manager.managers.receivers.UploadReceiver
import app.editors.manager.managers.utils.NewNotificationUtils
import app.editors.manager.mvp.models.explorer.CloudFile
import app.editors.manager.storages.base.fragment.BaseStorageDocsFragment
import app.editors.manager.storages.base.work.BaseStorageUploadWork
import lib.toolkit.base.managers.utils.FileUtils
import lib.toolkit.base.managers.utils.PathUtils
import java.io.DataOutputStream
import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.URL
import kotlin.math.min

class UploadWork(context: Context, workerParameters: WorkerParameters): BaseStorageUploadWork(context, workerParameters) {


    companion object {
        val TAG: String = UploadWork::class.java.simpleName

        const val KEY_FILE_ID = "KEY_FILE_ID"
        const val HEADER_LOCATION = "Location"

        private const val HEADER_NAME = "Content-Disposition"
    }

    private var file: DocumentFile? = null
    private var fileName = ""
    private var fileId: String? = null

    override fun doWork(): Result {
        getArgs()

        when(tag) {
            BaseStorageDocsFragment.KEY_UPLOAD -> {
                fileName = file?.name!!
            }
            BaseStorageDocsFragment.KEY_UPDATE, BaseStorageDocsFragment.KEY_CREATE -> {
                fileName = path?.let { FileUtils.getFileName(it, true) }!!
            }
        }

        val request = CreateItemRequest(
            name = fileName,
            parents = listOf(folderId!!)
        )

        val response = when(tag) {
            BaseStorageDocsFragment.KEY_UPLOAD, BaseStorageDocsFragment.KEY_CREATE -> applicationContext.getGoogleDriveServiceProvider().upload(request).blockingGet()
            BaseStorageDocsFragment.KEY_UPDATE -> fileId?.let { applicationContext.getGoogleDriveServiceProvider().update(it).blockingGet() }
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
                if (tag == BaseStorageDocsFragment.KEY_UPLOAD) {
                    count.toLong().let { progress ->
                        file?.length()?.let { total -> showProgress(total, progress) }
                    }
                }
            }
            if (connection.responseCode == 200 || connection.responseCode == 201) {
                mNotificationUtils.removeNotification(id.hashCode())
                if (tag == BaseStorageDocsFragment.KEY_UPLOAD) {
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

    override fun getArgs() {
        super.getArgs()
        fileId = data?.getString(KEY_FILE_ID)
        file = from?.let { DocumentFile.fromSingleUri(applicationContext, it) }
        path = from?.let { PathUtils.getPath(applicationContext, it) }
    }
}

