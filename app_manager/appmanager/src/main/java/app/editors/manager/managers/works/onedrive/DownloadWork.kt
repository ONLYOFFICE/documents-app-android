package app.editors.manager.managers.works.onedrive

import android.content.Context
import androidx.work.WorkerParameters
import app.editors.manager.app.getOneDriveServiceProvider
import app.editors.manager.managers.works.BaseDownloadWork
import okhttp3.ResponseBody
import retrofit2.Response

class DownloadWork(
    context: Context,
    workerParameters: WorkerParameters
) : BaseDownloadWork(context, workerParameters) {

    override fun download(): Response<ResponseBody> = applicationContext
        .getOneDriveServiceProvider()
        .download(id.orEmpty())
        .blockingGet()

}