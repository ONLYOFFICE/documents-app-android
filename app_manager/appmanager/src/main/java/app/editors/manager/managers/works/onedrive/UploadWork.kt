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
import java.net.HttpURLConnection
import java.net.URL

class UploadWork(context: Context, workerParameters: WorkerParameters) :
    BaseStorageUploadWork(context, workerParameters) {

    companion object {
        val TAG: String = UploadWork::class.java.simpleName
    }

    private var file: DocumentFile? = null

    override fun doWork(): Result {
        getArgs()

        when (tag) {
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

        when (response) {
            is OneDriveResponse.Success -> {
                from?.let { uploadSession((response.response as UploadResponse).uploadUrl, it) }
            }

            is OneDriveResponse.Error -> {
                notificationUtils.showUploadErrorNotification(id.hashCode(), title)
                title?.let { sendBroadcastUnknownError(it, path) }
                if (tag == BaseStorageDocsFragment.KEY_UPDATE) {
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
        val totalFileSize = applicationContext.contentResolver.openAssetFileDescriptor(uri, "r")?.use {
            return@use it.length
        } ?: 0

        try {
            App.getApp().contentResolver.openInputStream(uri).use { fileInputStream ->
                val chunkSize = 327680
                val chunkNumber = (totalFileSize / chunkSize).toInt()
                val chunkLeftover = (totalFileSize % chunkSize).toInt()

                for (i in 0..chunkNumber) {
                    val startIndex = i * chunkSize
                    val endIndex = if (i == chunkNumber) startIndex + chunkLeftover else startIndex + chunkSize
                    val chunkData = ByteArray(endIndex - startIndex)
                    fileInputStream?.read(chunkData)

                    val headers = mapOf(
                        "Content-Type" to "multipart/form-data; boundary=*****",
                        "Connection" to "Keep-Alive",
                        "Content-Length" to chunkData.size.toString(),
                        "Content-Range" to "bytes $startIndex-${endIndex - 1}/$totalFileSize"
                    )

                    val connection = (URL(url).openConnection() as HttpURLConnection)
                        .apply {
                            requestMethod = "PUT"
                            headers.forEach { (key, value) -> setRequestProperty(key, value) }
                            doOutput = true
                            outputStream.write(chunkData)
                        }
                    connection.content
                    if (connection.responseCode == 202) continue
                    if (connection.responseCode == 200 || connection.responseCode == 201) {
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
                        return
                    }

                }
            }
        } catch (error: Throwable) {
            notificationUtils.showUploadErrorNotification(id.hashCode(), title)
            title?.let { sendBroadcastUnknownError(it, path) }
            if(tag == BaseStorageDocsFragment.KEY_UPDATE) {
                file?.delete()
            }
            throw error
        }
    }

    override fun getArgs() {
        super.getArgs()
        file = from?.let { DocumentFile.fromSingleUri(applicationContext, it) }
        path = from?.path
    }

}