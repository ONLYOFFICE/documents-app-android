package app.editors.manager.managers.works.dropbox

import android.content.Context
import androidx.work.WorkerParameters
import app.documents.core.network.common.utils.DropboxUtils
import app.editors.manager.app.dropboxProvider
import app.editors.manager.managers.works.BaseDownloadWork
import okhttp3.ResponseBody
import retrofit2.Response

class DownloadWork(
    context: Context,
    workerParameters: WorkerParameters
): BaseDownloadWork(context, workerParameters) {

    companion object {
        const val DOWNLOADABLE_ITEM_KEY = "DOWNLOADABLE_ITEM_KEY"
        const val DOWNLOADABLE_ITEM_FILE = "file"
        const val DOWNLOADABLE_ITEM_FOLDER = "folder"
    }

    override fun download(): Response<ResponseBody> =
        with(applicationContext.dropboxProvider) {
            when (DOWNLOADABLE_ITEM_FILE) {
                data?.getString(DOWNLOADABLE_ITEM_KEY) -> download(id.orEmpty())
                else -> downloadFolder(id.orEmpty())
            }
        }.blockingGet()
}