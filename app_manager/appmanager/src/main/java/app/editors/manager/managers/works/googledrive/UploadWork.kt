package app.editors.manager.managers.works.googledrive

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.work.WorkerParameters
import app.editors.manager.app.App
import app.documents.core.network.storages.googledrive.models.request.CreateItemRequest
import app.documents.core.network.manager.models.explorer.CloudFile
import app.editors.manager.ui.fragments.base.BaseStorageDocsFragment
import app.editors.manager.managers.works.BaseStorageUploadWork
import app.editors.manager.managers.receivers.GoogleDriveUploadReceiver
import app.documents.core.network.storages.googledrive.models.GoogleDriveFile
import app.editors.manager.app.googleDriveProvider
import lib.toolkit.base.managers.utils.FileUtils
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
    private var fileId: String? = null

    override fun doWork(): Result {
        getArgs()

        when(tag) {
            BaseStorageDocsFragment.KEY_UPLOAD -> {
                title = ContentResolverUtils.getName(applicationContext, file?.uri ?: Uri.EMPTY)
            }
            BaseStorageDocsFragment.KEY_UPDATE, BaseStorageDocsFragment.KEY_CREATE -> {
                title = path?.let { FileUtils.getFileName(it, true) }.toString()
            }
        }

        val request = title?.let { title ->
            @Suppress("UNCHECKED_CAST")
            (CreateItemRequest(
        name = title,
        parents = listOf(folderId) as List<String>
    ))
        }

        val response = when(tag) {
            BaseStorageDocsFragment.KEY_UPLOAD -> request?.let { request ->
                applicationContext.googleDriveProvider.upload(
                    request
                ).blockingGet()
            }
            BaseStorageDocsFragment.KEY_CREATE -> request?.let { request ->
                applicationContext.googleDriveProvider.createFile(
                    request
                ).blockingGet()
            }
            BaseStorageDocsFragment.KEY_UPDATE -> fileId?.let { applicationContext.googleDriveProvider.update(it).blockingGet() }
            else -> request?.let { applicationContext.googleDriveProvider.upload(it).blockingGet() }
        }

        if(response?.isSuccessful == true) {
            when(tag) {
                BaseStorageDocsFragment.KEY_UPLOAD, BaseStorageDocsFragment.KEY_UPDATE -> {
                    from?.let { from -> response.headers().get(HEADER_LOCATION)?.let { url -> uploadSession(url, from) } }
                }
                BaseStorageDocsFragment.KEY_CREATE -> {
                    sendUploadItemId((response.body() as GoogleDriveFile).id)
                    file?.delete()
                }
            }
        } else {
            notificationUtils.showUploadErrorNotification(id.hashCode(), title)
            title?.let { sendBroadcastUnknownError(it, path) }
            if(tag == BaseStorageDocsFragment.KEY_UPDATE || tag == BaseStorageDocsFragment.KEY_CREATE) {
                file?.delete()
            }
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
                notificationUtils.removeNotification(id.hashCode())
                if (tag == BaseStorageDocsFragment.KEY_UPLOAD) {
                    notificationUtils.showUploadCompleteNotification(id.hashCode(), title)
                    title?.let { sendBroadcastUploadComplete(path, it, CloudFile(), path) }
                } else if( tag == BaseStorageDocsFragment.KEY_UPDATE) {
                    file?.delete()
                }
            } else {
                notificationUtils.removeNotification(id.hashCode())
                notificationUtils.showUploadErrorNotification(id.hashCode(), title)
                title?.let { sendBroadcastUnknownError(it, path) }
                if(tag == BaseStorageDocsFragment.KEY_UPDATE) {
                    file?.delete()
                }
            }
        } catch (e: Exception) {
            notificationUtils.showUploadErrorNotification(id.hashCode(), title)
            title?.let { sendBroadcastUnknownError(it, path) }
            throw e
        }
    }

    private fun sendUploadItemId(itemId: String) {
        val intent = Intent(GoogleDriveUploadReceiver.UPLOAD_ITEM_ID)
        intent.putExtra(GoogleDriveUploadReceiver.KEY_ITEM_ID, itemId)
        LocalBroadcastManager.getInstance(App.getApp()).sendBroadcast(intent)
    }

    override fun getArgs() {
        super.getArgs()
        fileId = data?.getString(KEY_FILE_ID)
        file = from?.let { DocumentFile.fromSingleUri(applicationContext, it) }
        path = from?.path
    }
}

