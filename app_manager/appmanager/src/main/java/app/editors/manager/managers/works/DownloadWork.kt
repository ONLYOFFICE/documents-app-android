package app.editors.manager.managers.works

import android.accounts.Account
import android.annotation.SuppressLint
import android.content.Context
import androidx.work.WorkerParameters
import app.documents.core.network.common.contracts.ApiContract
import app.documents.core.network.manager.ManagerService
import app.documents.core.network.manager.models.explorer.Operation
import app.documents.core.network.manager.models.request.RequestDownload
import app.editors.manager.R
import app.editors.manager.app.accountOnline
import app.editors.manager.app.api
import com.google.gson.Gson
import lib.toolkit.base.managers.utils.*
import okhttp3.ResponseBody
import retrofit2.HttpException
import retrofit2.Response

class DownloadWork(
    context: Context,
    workerParams: WorkerParameters
) : BaseDownloadWork(context, workerParams) {

    companion object {
        private const val KEY_ERROR_INFO = "error"
        private const val KEY_ERROR_INFO_MESSAGE = "message"
    }

    private val token: String?
        get() = AccountUtils.getToken(
            applicationContext,
            Account(
                applicationContext.accountOnline?.getAccountName(),
                applicationContext.getString(lib.toolkit.base.R.string.account_type)
            )
        )

    private val api: ManagerService = applicationContext.api

    @SuppressLint("MissingPermission")
    override fun download(): Response<ResponseBody> {
        val url = Gson().fromJson(
            data?.getString(REQUEST_DOWNLOAD),
            RequestDownload::class.java
        )?.let { requestDownload ->
            getArchiveUrl(requestDownload)
        } ?: StringUtils.getEncodedString(data?.getString(URL_KEY)).orEmpty()

        return api.downloadFile(
            url = url,
            cookie = ApiContract.COOKIE_HEADER + token
        ).blockingGet()
    }

    private fun getArchiveUrl(requestDownload: RequestDownload): String? {
        try {
            val response = api
                .downloadFiles(requestDownload)
                .blockingGet()

            for (download in response.response) {
                while (!isStopped) {
                    val operations: List<Operation> = api.status().blockingGet().response
                    if (operations.isNotEmpty()) {
                        if (operations[0].error == null) {
                            showProgress(
                                FileUtils.LOAD_MAX_PROGRESS.toLong(), operations[0].progress
                                    .toLong(), true
                            )
                            if (operations[0].finished && operations[0].id == download.id) {
                                id = operations[0].id
                                notificationUtils.removeNotification(0)
                                return operations[0].url
                            }
                        } else {
                            notificationUtils.showErrorNotification(
                                id.hashCode(),
                                file?.name.orEmpty()
                            )
                            onError(getExceedFileException(operations[0].error.orEmpty()))
                            file?.delete()
                            break
                        }
                    }
                }
                if (isStopped) {
                    notificationUtils.showCanceledNotification(id.hashCode(), file?.name.orEmpty())
                    file?.delete()
                }
            }
        } catch (e: Exception) {
            if (e is HttpException) {
                onHttpError(e)
            }
        }
        return null
    }

    private fun onHttpError(error: HttpException) {
        try {
            val errorMessage = error.response()?.errorBody()?.string()?.let { response ->
                StringUtils.getJsonObject(response)?.let json@ { jsonObject ->
                    return@json jsonObject
                        .getJSONObject(KEY_ERROR_INFO)
                        .getString(KEY_ERROR_INFO_MESSAGE)
                }
            }
            onError(DownloadException.Unknown(errorMessage))
        } catch (error: Exception) {
            onError(error)
        }
    }

    private fun getExceedFileException(errorMessage: String): DownloadException {
        val message = when (errorMessage) {
            ApiContract.Errors.EXCEED_FILE_SIZE_100 -> R.string.download_manager_exceed_size_100
            ApiContract.Errors.EXCEED_FILE_SIZE_25 -> R.string.download_manager_exceed_size_25
            else -> return DownloadException.Unknown()
        }
        return DownloadException.ExceedFileSize(message)
    }

}