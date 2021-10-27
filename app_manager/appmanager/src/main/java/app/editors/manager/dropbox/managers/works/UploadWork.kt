package app.editors.manager.dropbox.managers.works

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import app.documents.core.network.ApiContract
import app.editors.manager.app.App
import app.editors.manager.app.getDropboxServiceProvider
import app.editors.manager.dropbox.dropbox.login.DropboxResponse
import app.editors.manager.dropbox.mvp.models.explorer.DropboxItem
import app.editors.manager.dropbox.mvp.models.request.UploadRequest
import app.editors.manager.dropbox.ui.fragments.DocsDropboxFragment
import app.editors.manager.managers.receivers.UploadReceiver
import app.editors.manager.managers.retrofit.ProgressRequestBody
import app.editors.manager.managers.utils.NewNotificationUtils
import app.editors.manager.mvp.models.explorer.CloudFile
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import lib.toolkit.base.managers.utils.ContentResolverUtils
import lib.toolkit.base.managers.utils.FileUtils
import lib.toolkit.base.managers.utils.NetworkUtils
import lib.toolkit.base.managers.utils.StringUtils
import okhttp3.Headers
import okhttp3.MultipartBody
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class UploadWork(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {

    companion object {
        val TAG: String = UploadWork::class.java.simpleName

        const val TAG_UPLOAD_FILES = "TAG_UPLOAD_FILES"
        const val TAG_FOLDER_ID = "TAG_FOLDER_ID"
        const val KEY_TAG = "KEY_TAG"
        const val KEY_REVISION = "KEY_REVISION"

        const val MODE_ADD = "add"
        const val MODE_OVERWRITE = "overwrite"
    }

    private val mNotificationUtils: NewNotificationUtils = NewNotificationUtils(applicationContext, app.editors.manager.managers.works.UploadWork.TAG)
    private var action: String? = null
    private var path: String? = null
    private var folderId: String? = null
    private var title: String? = null
    private var from: Uri? = null
    private var timeMark = 0L

    private val headers: Headers by lazy {
        Headers.Builder()
            .addUnsafeNonAscii(ApiContract.HEADER_CONTENT_TYPE, "application/octet-stream")
            .build()
    }

    private val api = applicationContext.getDropboxServiceProvider()

    override fun doWork(): Result {
        getArgs()

        path = from?.path
        title = ContentResolverUtils.getName(applicationContext, from ?: Uri.EMPTY)

        try {
            val request = UploadRequest(
                path = folderId?.trim() + title,
                mode = MODE_ADD,
                autorename = true,
                mute = true,
                strict_conflict = false
            )
            val response = when (action) {
                DocsDropboxFragment.KEY_UPLOAD, DocsDropboxFragment.KEY_CREATE -> {
                    api.upload(Json.encodeToString(request) ,createMultipartBody(from)).blockingGet()
                }
                DocsDropboxFragment.KEY_UPDATE -> {
                    api.upload(Json.encodeToString(request.copy(mode = MODE_OVERWRITE)) ,createMultipartBody(from)).blockingGet()
                }
                else -> {
                    api.upload(Json.encodeToString(request) ,createMultipartBody(from)).blockingGet()
                }
            }
            when(response) {
                is DropboxResponse.Success -> {
                    mNotificationUtils.removeNotification(id.hashCode())
                    if(action == DocsDropboxFragment.KEY_UPLOAD) {
                        mNotificationUtils.showUploadCompleteNotification(id.hashCode(), title)
                        sendBroadcastUploadComplete(
                            path,
                            title!!,
                            getCloudFile((response.response as DropboxItem)),
                            path
                        )
                    }
                }
                is DropboxResponse.Error -> {
                    mNotificationUtils.showUploadErrorNotification(id.hashCode(), title)
                    sendBroadcastUnknownError(title!!, path)
                    if (!NetworkUtils.isOnline(applicationContext)) {
                        return Result.retry()
                    }
                }
            }
        } catch (e: IOException) {
            if (isStopped) {
                mNotificationUtils.showCanceledUploadNotification(id.hashCode(), title)
                sendBroadcastUploadCanceled(path)
            } else {
                mNotificationUtils.showUploadErrorNotification(id.hashCode(), title)
                sendBroadcastUnknownError(title!!, path)
                if (!NetworkUtils.isOnline(applicationContext)) {
                    return Result.retry()
                }
            }
        }
        return Result.success()
    }

    private fun getCloudFile(item: DropboxItem): CloudFile = CloudFile().apply {
            id = item.path_display
            title = item.name
            versionGroup = item.rev
            pureContentLength = item.size.toLong()
            fileExst = StringUtils.getExtensionFromPath(item.name.toLowerCase())
            updated = SimpleDateFormat(
                "yyyy-MM-dd'T'HH:mm:ss",
                Locale.getDefault()
            ).parse(item.client_modified)
        }

    private fun getArgs() {
        inputData.let {
            action = it.getString(KEY_TAG)
            from = Uri.parse(it.getString(TAG_UPLOAD_FILES))
            folderId = it.getString(TAG_FOLDER_ID)
        }
    }

    private fun createMultipartBody(uri: Uri?): MultipartBody.Part {
        return MultipartBody.Part.create(headers, createRequestBody(uri))
    }

    private fun createRequestBody(uri: Uri?): ProgressRequestBody {
        val requestBody = ProgressRequestBody(App.getApp(), uri)
        requestBody.setOnUploadCallbacks { total: Long, progress: Long ->
            if (!isStopped) {
                if(action == DocsDropboxFragment.KEY_UPLOAD) {
                    showProgress(total, progress)
                }
            } else {
                //call.cancel()
            }
        }
        return requestBody
    }

    private fun showProgress(total: Long, progress: Long) {
        val deltaTime = System.currentTimeMillis() - timeMark
        if (deltaTime > FileUtils.LOAD_PROGRESS_UPDATE) {
            timeMark = System.currentTimeMillis()
            val percent = FileUtils.getPercentOfLoading(total, progress)
            val id = id.hashCode()
            val tag = getId().toString()
            mNotificationUtils.showUploadProgressNotification(id, tag, title!!, percent)
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
}