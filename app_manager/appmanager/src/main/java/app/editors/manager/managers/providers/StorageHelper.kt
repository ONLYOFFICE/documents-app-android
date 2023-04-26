package app.editors.manager.managers.providers

import android.net.Uri
import androidx.work.Data
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import app.documents.core.network.storages.IStorageProvider
import app.documents.core.network.storages.IStorageHelper
import app.editors.manager.app.App
import app.editors.manager.managers.works.BaseStorageUploadWork
import app.editors.manager.ui.fragments.base.BaseStorageDocsFragment
import io.reactivex.Observable

abstract class StorageHelper<T : IStorageProvider>(
    private val work: Class<out BaseStorageUploadWork>
) : IStorageHelper<T> {

    private val workManager = WorkManager.getInstance(App.getApp().applicationContext)

    override fun upload(folderId: String, uris: List<Uri?>): Observable<Int> {
        return Observable.fromIterable(uris)
            .flatMap {
                val data = Data.Builder()
                    .putString(BaseStorageUploadWork.TAG_FOLDER_ID, folderId)
                    .putString(BaseStorageUploadWork.TAG_UPLOAD_FILES, it.toString())
                    .putString(BaseStorageUploadWork.KEY_TAG, BaseStorageDocsFragment.KEY_CREATE)
                    .build()

                val request = OneTimeWorkRequest.Builder(work)
                    .setInputData(data)
                    .build()

                workManager.enqueue(request)
                return@flatMap Observable.just(1)
            }
    }

}