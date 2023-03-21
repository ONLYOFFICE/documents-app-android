package app.editors.manager.managers.works.googledrive

import android.content.Context
import androidx.work.WorkerParameters
import app.documents.core.providers.GoogleDriveFileProvider
import app.editors.manager.app.googleDriveProvider
import app.editors.manager.managers.works.BaseDownloadWork
import okhttp3.ResponseBody
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

    override fun getContentSize(responseBody: ResponseBody?): Long? {
        return responseBody?.bytes()?.size?.toLong()
    }
}