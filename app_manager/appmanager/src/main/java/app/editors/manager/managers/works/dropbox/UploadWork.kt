package app.editors.manager.managers.works.dropbox

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import androidx.work.WorkerParameters
import app.documents.core.network.common.contracts.ApiContract
import app.documents.core.network.common.utils.DropboxUtils
import app.documents.core.manager.ProgressRequestBody
import app.documents.core.network.manager.models.explorer.CloudFile
import app.documents.core.network.storages.dropbox.login.DropboxResponse
import app.documents.core.network.storages.dropbox.models.explorer.DropboxItem
import app.editors.manager.app.App
import app.editors.manager.app.dropboxProvider
import app.editors.manager.managers.works.BaseStorageUploadWork
import app.editors.manager.ui.fragments.base.BaseStorageDocsFragment
import lib.toolkit.base.managers.utils.ContentResolverUtils
import lib.toolkit.base.managers.utils.NetworkUtils
import lib.toolkit.base.managers.utils.StringUtils
import okhttp3.Headers
import okhttp3.MultipartBody
import java.io.IOException
import java.util.*

class UploadWork(context: Context, workerParams: WorkerParameters) : BaseStorageUploadWork(context, workerParams) {

    companion object {
        val TAG: String = UploadWork::class.java.simpleName

        const val KEY_REVISION = "KEY_REVISION"

        const val MODE_ADD = "add"
        const val MODE_OVERWRITE = "overwrite"
    }

    private val headers: Headers by lazy {
        Headers.Builder()
            .addUnsafeNonAscii(ApiContract.HEADER_CONTENT_OPERATION_TYPE, "application/octet-stream")
            .build()
    }

    private val api = applicationContext.dropboxProvider

    override fun doWork(): Result {
        getArgs()

        path = from?.path
        title = ContentResolverUtils.getName(applicationContext, from ?: Uri.EMPTY)
        try {
            val request = "{\"path\":\"${DropboxUtils.encodeUnicodeSymbolsDropbox(folderId?.trim() + title!!)}\",\"mode\":\"${
                if (tag == BaseStorageDocsFragment.KEY_UPLOAD || tag == BaseStorageDocsFragment.KEY_CREATE) {
                    MODE_ADD
                } else {
                    MODE_OVERWRITE
                }
            }\",\"autorename\":true,\"mute\":true,\"strict_conflict\":false}"

            when (val response = api.upload(request, createMultipartBody(from)).blockingGet()) {
                is DropboxResponse.Success -> {
                    notificationUtils.removeNotification(id.hashCode())
                    if(tag == BaseStorageDocsFragment.KEY_UPLOAD) {
                        notificationUtils.showUploadCompleteNotification(id.hashCode(), title)
                        sendBroadcastUploadComplete(
                            path,
                            title!!,
                            getCloudFile((response.response as DropboxItem)),
                            path
                        )
                    } else {
                        from?.let { DocumentFile.fromSingleUri(applicationContext, it)?.delete() }
                    }
                }
                is DropboxResponse.Error -> {
                    if (tag == BaseStorageDocsFragment.KEY_UPLOAD) {
                        notificationUtils.showUploadErrorNotification(id.hashCode(), title)
                    } else {
                        from?.let { DocumentFile.fromSingleUri(applicationContext, it)?.delete() }
                    }
                    sendBroadcastUnknownError(title!!, path)
                    if (!NetworkUtils.isOnline(applicationContext)) {
                        return Result.retry()
                    }
                }
            }
        } catch (e: IOException) {
            if (isStopped) {
                notificationUtils.showCanceledUploadNotification(id.hashCode(), title)
                sendBroadcastUploadCanceled(path)
            } else {
                if(tag == BaseStorageDocsFragment.KEY_UPLOAD) {
                    notificationUtils.showUploadErrorNotification(id.hashCode(), title)
                } else {
                    from?.let { DocumentFile.fromSingleUri(applicationContext, it)?.delete() }
                }
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
            fileExst = StringUtils.getExtensionFromPath(item.name.lowercase(Locale.getDefault()))
            updated = StringUtils.getDate("yyyy-MM-dd'T'HH:mm:ss", item.client_modified)
        }

    private fun createMultipartBody(uri: Uri?): MultipartBody.Part {
        return MultipartBody.Part.create(headers, createRequestBody(uri))
    }

    private fun createRequestBody(uri: Uri?): ProgressRequestBody {
        val requestBody = ProgressRequestBody(App.getApp(), uri ?: Uri.EMPTY)
        requestBody.setOnUploadCallbacks { total: Long, progress: Long ->
            if (!isStopped) {
                if(tag == BaseStorageDocsFragment.KEY_UPLOAD) {
                    showProgress(total, progress)
                }
            } else {
                //call.cancel()
            }
        }
        return requestBody
    }
}