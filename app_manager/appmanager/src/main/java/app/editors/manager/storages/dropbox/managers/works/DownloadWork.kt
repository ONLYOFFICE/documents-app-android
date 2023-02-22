package app.editors.manager.storages.dropbox.managers.works

import android.content.Context
import androidx.work.WorkerParameters
import app.editors.manager.app.getDropboxServiceProvider
import app.editors.manager.managers.works.BaseDownloadWork
import app.editors.manager.storages.dropbox.managers.utils.DropboxUtils
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
        with(applicationContext.getDropboxServiceProvider()) {
            val request = "{\"path\":\"${DropboxUtils.encodeUnicodeSymbolsDropbox(id!!)}\"}"
            when (DOWNLOADABLE_ITEM_FILE) {
                data?.getString(DOWNLOADABLE_ITEM_KEY) -> download(request)
                else -> downloadFolder(request)
            }
        }.blockingGet()
}