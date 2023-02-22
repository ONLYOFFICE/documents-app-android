package app.editors.manager.storages.googledrive.managers.works

import android.content.Context
import androidx.work.WorkerParameters
import app.editors.manager.app.getGoogleDriveServiceProvider
import app.editors.manager.managers.works.BaseDownloadWork
import app.editors.manager.storages.googledrive.managers.providers.GoogleDriveFileProvider
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
        with(applicationContext.getGoogleDriveServiceProvider()) {
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