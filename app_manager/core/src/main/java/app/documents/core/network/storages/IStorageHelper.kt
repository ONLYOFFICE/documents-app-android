package app.documents.core.network.storages

import android.net.Uri
import io.reactivex.Observable

interface IStorageHelper<T : IStorageProvider> {

    val api: T

    fun upload(folderId: String, uris: List<Uri?>): Observable<Int>
}