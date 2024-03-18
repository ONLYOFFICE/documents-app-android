package app.documents.core.network.storages.onedrive.api

import app.documents.core.network.storages.IStorageProvider
import app.documents.core.network.storages.onedrive.models.request.ChangeFileRequest
import app.documents.core.network.storages.onedrive.models.request.CopyItemRequest
import app.documents.core.network.storages.onedrive.models.request.CreateFolderRequest
import app.documents.core.network.storages.onedrive.models.request.ExternalLinkRequest
import app.documents.core.network.storages.onedrive.models.request.RenameRequest
import app.documents.core.network.storages.onedrive.models.request.UploadRequest
import com.jakewharton.rxrelay2.BehaviorRelay
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import okhttp3.ResponseBody
import retrofit2.HttpException
import retrofit2.Response

sealed class OneDriveResponse {
    class Success(val response: Any) : OneDriveResponse()
    class Error(val error: Throwable) : OneDriveResponse()
}

class OneDriveProvider(
    private val oneDriveService: OneDriveService,
    private val oneDriveErrorHandler: BehaviorRelay<OneDriveResponse.Error>? = null
) : IStorageProvider {

    fun getFiles(map: Map<String, String>): Single<OneDriveResponse> {
        return oneDriveService.getFiles(map)
            .map { fetchResponse(it) }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    fun getChildren(itemId: String, map: Map<String, String>): Single<OneDriveResponse> {
        return oneDriveService.getChildren(itemId, map)
            .map { fetchResponse(it) }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    fun getRoot(): Single<OneDriveResponse> {
        return oneDriveService.getRoot()
            .map { fetchResponse(it) }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    fun download(itemId: String): Single<Response<ResponseBody>> {
        return oneDriveService.download(itemId)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    fun deleteItem(itemId: String): Single<Response<ResponseBody>> {
        return oneDriveService.deleteItem(itemId)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    fun renameItem(itemId: String, request: RenameRequest): Single<Response<ResponseBody>> {
        return oneDriveService.renameItem(itemId, request)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    fun createFolder(
        itemId: String,
        request: CreateFolderRequest
    ): Single<OneDriveResponse> {
        return oneDriveService.createFolder(itemId, request)
            .map { fetchResponse(it) }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    fun createFile(
        itemId: String,
        fileName: String,
        opts: Map<String, String>
    ): Single<OneDriveResponse> {
        return oneDriveService.createFile(itemId, fileName, opts)
            .map { fetchResponse(it) }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    fun updateFile(itemId: String, request: ChangeFileRequest): Single<Response<ResponseBody>> {
        return oneDriveService.updateFile(itemId, request)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    fun uploadFile(folderId: String, fileName: String, request: UploadRequest): Single<OneDriveResponse> {
        return oneDriveService.uploadFile(folderId, fileName, request)
            .map { fetchResponse(it) }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())

    }

    fun copyItem(
        itemId: String,
        request: CopyItemRequest
    ): Single<Response<ResponseBody>> {
        return oneDriveService.copyItem(itemId, request)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    fun moveItem(
        itemId: String,
        request: CopyItemRequest
    ): Single<Response<ResponseBody>> {
        return oneDriveService.moveItem(itemId, request)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    fun getPhoto(): Single<Response<ResponseBody>> {
        return oneDriveService.getPhoto()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    fun filter(value: String, map: Map<String, String>): Single<OneDriveResponse> {
        return oneDriveService.filter(value, map)
            .map { fetchResponse(it) }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    fun getExternalLink(
        itemId: String,
        request: ExternalLinkRequest
    ): Single<OneDriveResponse> {
        return oneDriveService.getExternalLink(itemId, request)
            .map { fetchResponse(it) }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    private fun <T> fetchResponse(response: Response<T>): OneDriveResponse {
        return if (response.isSuccessful && response.body() != null) {
            OneDriveResponse.Success(response.body()!!)
        } else {
            val error = OneDriveResponse.Error(HttpException(response))
            oneDriveErrorHandler?.accept(error)
            return error
        }
    }

}