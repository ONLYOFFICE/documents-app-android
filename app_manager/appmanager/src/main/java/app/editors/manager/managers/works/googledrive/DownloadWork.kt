package app.editors.manager.managers.works.googledrive

import android.content.Context
import androidx.work.WorkerParameters
import app.documents.core.providers.GoogleDriveFileProvider
import app.editors.manager.app.googleDriveProvider
import app.editors.manager.managers.works.BaseDownloadWork
import app.editors.manager.managers.works.DownloadException
import lib.toolkit.base.managers.utils.FileUtils
import okhttp3.ResponseBody
import retrofit2.HttpException
import retrofit2.Response

class DownloadWork(
    context: Context,
    workerParameters: WorkerParameters
): BaseDownloadWork(context, workerParameters) {

    companion object {
        const val GOOGLE_MIME_TYPE = "mime_type"
    }

    override fun download(): Response<ResponseBody> =
        with(applicationContext.googleDriveProvider) {
            val googleMimeType = data?.getString(GOOGLE_MIME_TYPE)
            when {
                !googleMimeType.isNullOrEmpty() -> export(
                    id.orEmpty(),
                    GoogleDriveFileProvider.getCommonMimeType(googleMimeType)
                )
                else -> download(id.orEmpty())
            }
        }.blockingGet()

    override fun writeFromResponse(response: Response<ResponseBody>) {
        try {
            val responseBody = response.body()
            if (response.isSuccessful && responseBody != null) {
                val bytes = responseBody.bytes()
                val length = bytes.size.toLong()
                if (!FileUtils.isEnoughFreeSpace(length)) {
                    onError(DownloadException.NotEnoughFreeSpace)
                } else {
                    FileUtils.writeFromResponseBody(
                        stream = bytes.inputStream(),
                        length = length,
                        to = to,
                        context = applicationContext,
                        progress = ::showProgress,
                        finish = ::onFinish,
                        error = ::onError
                    )
                }
            } else {
                throw HttpException(response)
            }
        } catch (error: Throwable) {
            onError(error)
        }
    }
}