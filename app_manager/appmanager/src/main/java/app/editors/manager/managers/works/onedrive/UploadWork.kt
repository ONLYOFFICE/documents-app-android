package app.editors.manager.managers.works.onedrive

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import androidx.work.WorkerParameters
import app.documents.core.network.common.utils.OneDriveUtils
import app.documents.core.network.manager.models.explorer.CloudFile
import app.documents.core.network.storages.onedrive.api.OneDriveResponse
import app.documents.core.network.storages.onedrive.models.response.UploadResponse
import app.editors.manager.app.App
import app.editors.manager.app.oneDriveProvider
import app.editors.manager.managers.works.BaseStorageUploadWork
import app.editors.manager.managers.works.UploadWork
import app.editors.manager.ui.fragments.base.BaseStorageDocsFragment
import lib.toolkit.base.managers.utils.FileUtils
import java.io.DataOutputStream
import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.URL
import kotlin.math.min

class UploadWork(context: Context, workerParameters: WorkerParameters): BaseStorageUploadWork(context, workerParameters) {

    companion object {
        val TAG: String = UploadWork::class.java.simpleName

        private const val HEADER_NAME = "Content-Disposition"
    }

    private var file: DocumentFile? = null

    override fun doWork(): Result {
        getArgs()

        when(tag) {
            BaseStorageDocsFragment.KEY_UPLOAD -> {
                title = file?.name.toString()
            }
            BaseStorageDocsFragment.KEY_UPDATE -> {
                title = path?.let { FileUtils.getFileName(it, true) }.toString()
            }
        }

        val request = app.documents.core.network.storages.onedrive.models.request.UploadRequest()
        val response = folderId?.let { folderId ->
            title?.let { title ->
                applicationContext.oneDriveProvider.uploadFile(
                    folderId, title,
                    when (tag) {
                        BaseStorageDocsFragment.KEY_UPLOAD -> request.copy(
                            item = app.documents.core.network.storages.onedrive.models.other.Item(
                                OneDriveUtils.VAL_CONFLICT_BEHAVIOR_RENAME
                            )
                        )
                        BaseStorageDocsFragment.KEY_UPDATE -> request.copy(
                            item = app.documents.core.network.storages.onedrive.models.other.Item(
                                OneDriveUtils.VAL_CONFLICT_BEHAVIOR_REPLACE
                            )
                        )
                        else -> request.copy(
                            item = app.documents.core.network.storages.onedrive.models.other.Item(
                                OneDriveUtils.VAL_CONFLICT_BEHAVIOR_FAIL
                            )
                        )
                    }
                ).blockingGet()
            }
        }

        when(response) {
            is OneDriveResponse.Success -> {
                from?.let { uploadSession((response.response as UploadResponse).uploadUrl, it) }
            }
            is OneDriveResponse.Error -> {
                notificationUtils.showUploadErrorNotification(id.hashCode(), title)
                title?.let { sendBroadcastUnknownError(it, path) }
                if(tag == BaseStorageDocsFragment.KEY_UPDATE) {
                    file?.delete()
                }
            }
            else -> {
                // Stub
            }
        }

        return Result.success()
    }


    private fun uploadSession(url: String, uri: Uri) {
        val connection = URL(url).openConnection() as HttpURLConnection
        val fileInputStream = App.getApp().contentResolver.openInputStream(uri)
        val maxBufferSize = 2 * 1024 * 1024
        val boundary = "*****"
        var outputStream: OutputStream? = null
        val bytesAvailable = fileInputStream?.available()
        connection.doInput = true
        connection.doOutput = true
        connection.useCaches = false
        connection.requestMethod = "PUT"
        connection.setRequestProperty("Connection", "Keep-Alive")
        connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=$boundary")
        try {
            outputStream = DataOutputStream(connection.outputStream)
            val bufferSize = min(bytesAvailable!!, maxBufferSize)
            val buffer = ByteArray(bufferSize)
            var count = 0
            var bytesRead = 0
            while (fileInputStream.read(buffer).also { bytesRead = it } != -1) {
                outputStream.write(buffer, 0, bytesRead)
                count += bytesRead
                if(tag == BaseStorageDocsFragment.KEY_UPLOAD) {
                    count.toLong().let { progress ->
                        file?.length()?.let { total -> showProgress(total, progress) }
                    }
                }
            }
            if(connection.responseCode == 200 || connection.responseCode == 201) {
                notificationUtils.removeNotification(id.hashCode())
                if (tag == BaseStorageDocsFragment.KEY_UPLOAD) {
                    notificationUtils.showUploadCompleteNotification(id.hashCode(), title)
                    title?.let { sendBroadcastUploadComplete(path, it, CloudFile(), path) }
                } else {
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
            if(tag == BaseStorageDocsFragment.KEY_UPDATE) {
                file?.delete()
            }
            throw e
        } finally {
            fileInputStream?.close()
            outputStream?.close()
        }
    }

    override fun getArgs() {
        super.getArgs()
        file = from?.let { DocumentFile.fromSingleUri(applicationContext, it) }
        path = from?.path
    }


}