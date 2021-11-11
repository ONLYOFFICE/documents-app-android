package app.editors.manager.onedrive.managers.works

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import app.editors.manager.app.App
import app.editors.manager.app.getOneDriveServiceProvider
import app.editors.manager.onedrive.onedrive.OneDriveResponse
import app.editors.manager.managers.receivers.UploadReceiver
import app.editors.manager.managers.utils.NewNotificationUtils
import app.editors.manager.managers.works.UploadWork
import app.editors.manager.mvp.models.explorer.CloudFile
import app.editors.manager.onedrive.managers.utils.OneDriveUtils
import app.editors.manager.onedrive.mvp.models.request.UploadRequest
import app.editors.manager.onedrive.mvp.models.response.UploadResponse
import app.editors.manager.onedrive.ui.fragments.DocsOneDriveFragment
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

        private const val HEADER_NAME = "Content-Disposition"
    }

    private val mNotificationUtils: NewNotificationUtils = NewNotificationUtils(applicationContext, TAG)
    private var path: String? = null
    private var folderId: String? = null
    private var from: Uri? = null
    private var timeMark = 0L
    private var file: DocumentFile? = null
    private var tag = ""
    private var fileName = ""

    override fun doWork(): Result {
        getArgs()

        when(tag) {
            DocsOneDriveFragment.KEY_UPLOAD -> {
                fileName = file?.name!!
            }
            DocsOneDriveFragment.KEY_UPDATE -> {
                fileName = path?.let { FileUtils.getFileName(it, true) }!!
            }
        }

        val request = UploadRequest()
        val response = folderId?.let {
            applicationContext.getOneDriveServiceProvider().uploadFile(
                it, fileName,
                when (tag) {
                    DocsOneDriveFragment.KEY_UPLOAD -> request.copy(
                        item = app.editors.manager.onedrive.mvp.models.other.Item(
                            OneDriveUtils.VAL_CONFLICT_BEHAVIOR_RENAME
                        )
                    )
                    DocsOneDriveFragment.KEY_UPDATE -> request.copy(
                        item = app.editors.manager.onedrive.mvp.models.other.Item(
                            OneDriveUtils.VAL_CONFLICT_BEHAVIOR_REPLACE
                        )
                    )
                    else -> request.copy(
                        item = app.editors.manager.onedrive.mvp.models.other.Item(
                            OneDriveUtils.VAL_CONFLICT_BEHAVIOR_FAIL
                        )
                    )
                }
            ).blockingGet()
        }

        when(response) {
            is OneDriveResponse.Success -> {
                from?.let { uploadSession((response.response as UploadResponse).uploadUrl, it) }
            }
            is OneDriveResponse.Error -> {
                mNotificationUtils.showUploadErrorNotification(id.hashCode(), fileName)
                sendBroadcastUnknownError(fileName, path)
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
                if(tag == DocsOneDriveFragment.KEY_UPLOAD) {
                    count.toLong().let { progress ->
                        file?.length()?.let { total -> showProgress(total, progress) }
                    }
                }
            }
            if(connection.responseCode == 200 || connection.responseCode == 201) {
                mNotificationUtils.removeNotification(id.hashCode())
                if (tag == DocsOneDriveFragment.KEY_UPLOAD) {
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
        }
        file = from?.let { DocumentFile.fromSingleUri(applicationContext, it) }
        path = from?.let { PathUtils.getPath(applicationContext, it) }
    }


}