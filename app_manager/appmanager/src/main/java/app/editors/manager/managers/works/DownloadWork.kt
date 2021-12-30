package app.editors.manager.managers.works

import android.accounts.Account
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.documentfile.provider.DocumentFile
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import app.documents.core.network.ApiContract
import app.editors.manager.R
import app.editors.manager.app.Api
import app.editors.manager.app.App
import app.editors.manager.di.component.DaggerApiComponent
import app.editors.manager.managers.receivers.DownloadReceiver
import app.editors.manager.managers.utils.FirebaseUtils
import app.editors.manager.managers.utils.NewNotificationUtils
import app.editors.manager.mvp.models.explorer.Operation
import app.editors.manager.mvp.models.request.RequestDownload
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import lib.toolkit.base.managers.utils.AccountUtils
import lib.toolkit.base.managers.utils.FileUtils
import lib.toolkit.base.managers.utils.FileUtils.Finish
import lib.toolkit.base.managers.utils.FileUtils.Progress
import lib.toolkit.base.managers.utils.PathUtils
import lib.toolkit.base.managers.utils.StringUtils
import okhttp3.ResponseBody
import org.json.JSONException
import retrofit2.Call
import retrofit2.HttpException
import java.io.IOException

class DownloadWork(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {

    companion object {
        private val TAG = DownloadWork::class.java.simpleName

        const val URL_KEY = "URL_KEY"
        const val FILE_ID_KEY = "FILE_ID_KEY"
        const val FILE_URI_KEY = "FILE_URI_KEY"
        const val REQUEST_DOWNLOAD = "REQUEST_DOWNLOAD"

        private const val KEY_ERROR_INFO = "error"
        private const val KEY_ERROR_INFO_MESSAGE = "message"

        fun sendBroadcastDownloadComplete(
            id: String?, url: String?, title: String?,
            path: String?, mime: String?, uri: Uri?
        ) {
            val intent = Intent(DownloadReceiver.DOWNLOAD_ACTION_COMPLETE)
            intent.putExtra(DownloadReceiver.EXTRAS_KEY_ID, id)
            intent.putExtra(DownloadReceiver.EXTRAS_KEY_URL, url)
            intent.putExtra(DownloadReceiver.EXTRAS_KEY_TITLE, title)
            intent.putExtra(DownloadReceiver.EXTRAS_KEY_PATH, path)
            intent.putExtra(DownloadReceiver.EXTRAS_KEY_MIME_TYPE, mime)
            intent.putExtra(DownloadReceiver.EXTRAS_KEY_URI, uri.toString())
            LocalBroadcastManager.getInstance(App.getApp()).sendBroadcast(intent)
        }

        fun sendBroadcastUnknownError(id: String?, url: String?, title: String?, uri: Uri?) {
            val intent = Intent(DownloadReceiver.DOWNLOAD_ACTION_ERROR)
            intent.putExtra(DownloadReceiver.EXTRAS_KEY_ID, id)
            intent.putExtra(DownloadReceiver.EXTRAS_KEY_URL, url)
            intent.putExtra(DownloadReceiver.EXTRAS_KEY_TITLE, title)
            intent.putExtra(DownloadReceiver.EXTRAS_KEY_URI, uri.toString())
            LocalBroadcastManager.getInstance(App.getApp()).sendBroadcast(intent)
        }

        fun sendBroadcastError(id: String?, url: String?, title: String?, error: String?) {
            val intent = Intent(DownloadReceiver.DOWNLOAD_ACTION_ERROR)
            intent.putExtra(DownloadReceiver.EXTRAS_KEY_ID, id)
            intent.putExtra(DownloadReceiver.EXTRAS_KEY_URL, url)
            intent.putExtra(DownloadReceiver.EXTRAS_KEY_TITLE, title)
            intent.putExtra(DownloadReceiver.EXTRAS_KEY_ERROR, error)
            LocalBroadcastManager.getInstance(App.getApp()).sendBroadcast(intent)
        }
    }

    private val notificationUtils: NewNotificationUtils = NewNotificationUtils(applicationContext, TAG)
    private var url: String? = null
    private var file: DocumentFile? = null
    private var fileName: String? = null
    private var id: String? = null
    private var to: Uri? = null
    private var timeMark = 0L
    private var downloadRequest: RequestDownload? = null
    private var token: String

    private val api: Api = runBlocking(Dispatchers.Default) {
        App.getApp().appComponent.accountsDao.getAccountOnline()?.let { account ->
            AccountUtils.getToken(
                applicationContext,
                Account(account.getAccountName(), applicationContext.getString(lib.toolkit.base.R.string.account_type))
            )?.let {
                token = it
                return@runBlocking DaggerApiComponent.builder()
                    .appComponent(App.getApp().appComponent)
                    .build().api
            }
        } ?: run {
            throw Error("No account")
        }
    }

    @SuppressLint("MissingPermission")
    override fun doWork(): Result {
        getArgs()
        var successArchiving = true
        if (downloadRequest != null) {
            successArchiving = downloadFiles()
        }
        if(successArchiving) {
            val call: Call<ResponseBody> =
                api.downloadFile(url ?: "", ApiContract.COOKIE_HEADER + token)
            try {
                val response = call.execute()
                if (response.isSuccessful && response.body() != null) {
                    FileUtils.writeFromResponseBody(
                        response.body(),
                        to ?: Uri.EMPTY,
                        applicationContext,
                        object : Progress {
                            override fun onProgress(total: Long, progress: Long): Boolean {
                                showProgress(total, progress, false)
                                return isStopped
                            }
                        },
                        object : Finish {
                            override fun onFinish() {
                                notificationUtils.removeNotification(id.hashCode())
                                notificationUtils.showCompleteNotification(id.hashCode(), fileName, file?.uri)
                                sendBroadcastDownloadComplete(
                                    id,
                                    url,
                                    fileName,
                                    PathUtils.getPath(applicationContext, to ?: Uri.EMPTY),
                                    StringUtils.getMimeTypeFromPath(
                                        fileName ?: ""
                                    ),
                                    to
                                )
                            }
                        },
                        object : FileUtils.Error {
                            override fun onError(message: String) {
                                notificationUtils.removeNotification(id.hashCode())
                                if (isStopped) {
                                    notificationUtils.showCanceledNotification(
                                        id.hashCode(),
                                        fileName
                                    )
                                } else {
                                    notificationUtils.showErrorNotification(id.hashCode(), fileName)
                                    sendBroadcastUnknownError(id, url, fileName, to)
                                }
                                file?.delete()
                            }
                        })
                } else {
                    notificationUtils.showErrorNotification(id.hashCode(), fileName)
                    sendBroadcastUnknownError(id, url, fileName, to)
                    file?.delete()
                }
            } catch (e: IOException) {
                file?.delete()
            }
            return Result.success()
        }
        return Result.success()
    }

    private fun showProgress(total: Long, progress: Long, isArchiving: Boolean) {
        val deltaTime = System.currentTimeMillis() - timeMark
        if (deltaTime > FileUtils.LOAD_PROGRESS_UPDATE) {
            timeMark = System.currentTimeMillis()
            val percent = FileUtils.getPercentOfLoading(total, progress)
            val id = id.hashCode()
            val tag = getId().toString()
            if (!isArchiving) {
                notificationUtils.showProgressNotification(id, tag, fileName ?: "", percent)
            } else {
                notificationUtils.showArchivingProgressNotification(id, tag, fileName ?: "", percent)
            }
        }
    }

    private fun downloadFiles(): Boolean {
        try {
            val response = api.downloadFiles(downloadRequest ?: throw Exception("No download request")).blockingGet()
            val downloads = response.response
            for (download in downloads) {
                do {
                    if (!isStopped) {
                        val operations: List<Operation> = api.status().blockingGet().response
                        if (operations.isNotEmpty()) {
                            if (operations[0].error == null) {
                                showProgress(
                                    FileUtils.LOAD_MAX_PROGRESS.toLong(), operations[0].progress
                                        .toLong(), true
                                )
                                if (operations[0].finished && operations[0].id == download.id) {
                                    url = operations[0].url
                                    id = operations[0].id
                                    notificationUtils.removeNotification(0)
                                    break
                                }
                            } else {
                                notificationUtils.showErrorNotification(id.hashCode(), fileName)
                                onError(operations[0].error)
                                file?.delete()
                                break
                            }
                        } else {
                            break
                        }
                    } else {
                        notificationUtils.showCanceledNotification(id.hashCode(), fileName)
                        file?.delete()
                        break
                    }
                } while (true)
            }
        } catch (e: Exception) {
            if (e is HttpException) {
                onError(e.response()!!.errorBody())
                return false
            }
        }
        return true
    }

    private fun onError(responseBody: ResponseBody?) {
        val errorMessage: String
        var responseMessage: String? = null
        responseMessage = try {
            responseBody?.string()
        } catch (e: Exception) {
            sendBroadcastUnknownError(id, url, fileName, to)
            file?.delete()
            return
        }
        if (responseMessage != null) {
            val jsonObject = StringUtils.getJsonObject(responseMessage)
            if (jsonObject != null) {
                try {
                    errorMessage = jsonObject.getJSONObject(KEY_ERROR_INFO).getString(KEY_ERROR_INFO_MESSAGE)
                    file?.delete()
                    sendBroadcastError(id, url, fileName, errorMessage)
                } catch (e: JSONException) {
                    Log.e(TAG, "onErrorHandle()", e)
                    FirebaseUtils.addCrash(e)
                }
            } else {
                sendBroadcastUnknownError(id, url, fileName, to)
                file?.delete()
                return
            }
        }
    }

    private fun onError(errorMessage: String) {
        when (errorMessage) {
            ApiContract.Errors.EXCEED_FILE_SIZE_100 -> sendBroadcastError(
                id,
                url,
                fileName,
                applicationContext.getString(R.string.download_manager_exceed_size_100)
            )
            ApiContract.Errors.EXCEED_FILE_SIZE_25 -> sendBroadcastError(
                id,
                url,
                fileName,
                applicationContext.getString(R.string.download_manager_exceed_size_25)
            )
            else -> sendBroadcastError(id, url, fileName, errorMessage)
        }
    }

    private fun getArgs() {
        val data = inputData
        val gson = Gson()
        url = StringUtils.getEncodedString(data.getString(URL_KEY))
        id = data.getString(FILE_ID_KEY)
        downloadRequest = gson.fromJson(data.getString(REQUEST_DOWNLOAD), RequestDownload::class.java)
        to = Uri.parse(data.getString(FILE_URI_KEY))
        file = DocumentFile.fromSingleUri(applicationContext, to!!)
        fileName = file?.name
    }

}