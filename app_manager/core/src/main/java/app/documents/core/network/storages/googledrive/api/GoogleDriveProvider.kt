package app.documents.core.network.storages.googledrive.api

import app.documents.core.network.storages.IStorageProvider
import app.documents.core.network.storages.googledrive.models.GoogleDriveFile
import app.documents.core.network.storages.googledrive.models.request.CreateItemRequest
import app.documents.core.network.storages.googledrive.models.request.RenameRequest
import app.documents.core.network.storages.googledrive.models.request.ShareRequest
import com.jakewharton.rxrelay2.BehaviorRelay
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import okhttp3.ResponseBody
import retrofit2.HttpException
import retrofit2.Response

sealed class GoogleDriveResponse {
    class Success(val response: Any) : GoogleDriveResponse()
    class Error(val error: Throwable) : GoogleDriveResponse()
}

class GoogleDriveProvider(
    private val googleDriveService: GoogleDriveService,
    private val googleDriveErrorHandle: BehaviorRelay<GoogleDriveResponse.Error>? = null
) : IStorageProvider {

    fun getFiles(map: Map<String, String>, intMap: Map<String, Int>): Single<GoogleDriveResponse> {
        return googleDriveService.getFiles(map, intMap)
            .map { fetchResponse(it) }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    fun getFileInfo(
        fileId: String,
        map: Map<String, String>
    ): Single<GoogleDriveResponse> {
        return googleDriveService.getFileInfo(fileId, map)
            .map { fetchResponse(it) }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    fun download(
        fileId: String,
        map: Map<String, String> = mapOf("alt" to "media")
    ): Single<Response<ResponseBody>> {
        return googleDriveService.download(fileId, map)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    fun delete(fileId: String): Single<Response<ResponseBody>> {
        return googleDriveService.deleteItem(fileId)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    fun rename(fileId: String, request: RenameRequest): Single<GoogleDriveResponse> {
        return googleDriveService.rename(fileId, request)
            .map { fetchResponse(it) }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    fun move(fileId: String, map: Map<String, String?>): Single<GoogleDriveResponse> {
        return googleDriveService.move(fileId, map)
            .map { fetchResponse(it) }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    fun copy(fileId: String): Single<GoogleDriveResponse> {
        return googleDriveService.copy(fileId)
            .map { fetchResponse(it) }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    fun create(request: CreateItemRequest): Single<GoogleDriveResponse> {
        return googleDriveService.createItem(request)
            .map { fetchResponse(it) }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    fun createFile(request: CreateItemRequest): Single<Response<GoogleDriveFile>> {
        return googleDriveService.createItem(request)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    fun upload(request: CreateItemRequest, map: Map<String, String> = mapOf()): Single<Response<ResponseBody>> {
        return googleDriveService.upload(request, map)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    fun update(
        fileId: String,
        map: Map<String, String> = mapOf()
    ): Single<Response<ResponseBody>> {
        return googleDriveService.update(fileId, map)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    fun share(fileId: String, request: ShareRequest): Single<Response<ResponseBody>> {
        return googleDriveService.share(fileId, request)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    fun export(fileId: String, mimeType: String): Single<Response<ResponseBody>> {
        return googleDriveService.export(fileId, mapOf("mimeType" to mimeType))
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    private fun <T> fetchResponse(response: Response<T>): GoogleDriveResponse {
        return if (response.isSuccessful && response.body() != null) {
            GoogleDriveResponse.Success(response.body()!!)
        } else {
            val error = GoogleDriveResponse.Error(HttpException(response))
            googleDriveErrorHandle?.accept(error)
            return error
        }
    }

    fun getUserInfo(token: String): Single<GoogleDriveResponse> {
        return googleDriveService.getUserInfo(token)
            .map { fetchResponse(it) }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

}