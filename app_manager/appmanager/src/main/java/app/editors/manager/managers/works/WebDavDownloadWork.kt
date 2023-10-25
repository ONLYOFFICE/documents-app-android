package app.editors.manager.managers.works

import android.content.Context
import androidx.work.WorkerParameters
import app.editors.manager.app.webDavApi
import okhttp3.ResponseBody
import retrofit2.Response

class WebDavDownloadWork(
    private val context: Context,
    workerParameters: WorkerParameters
) : BaseDownloadWork(context, workerParameters) {

    override fun download(): Response<ResponseBody> = context.webDavApi.download(id.orEmpty()).blockingGet()
}