package app.documents.core.network.storages.googledrive.api

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
    private val googleDriveServiceProvider: GoogleDriveService,
    private val googleDriveErrorHandle: BehaviorRelay<GoogleDriveResponse.Error>? = null
) {

    fun getFiles(map: Map<String, String>): Single<GoogleDriveResponse> {
        return googleDriveServiceProvider.getFiles(map)
            .map { fetchResponse(it) }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    fun getFileInfo(
        fileId: String,
        map: Map<String, String>
    ): Single<GoogleDriveResponse> {
        return googleDriveServiceProvider.getFileInfo(fileId, map)
            .map { fetchResponse(it) }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    fun download(
        fileId: String,
        map: Map<String, String> = mapOf()
    ): Single<Response<ResponseBody>> {
        return googleDriveServiceProvider.download(fileId, map)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    fun delete(fileId: String): Single<Response<ResponseBody>> {
        return googleDriveServiceProvider.deleteItem(fileId)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    fun rename(fileId: String, request: RenameRequest): Single<GoogleDriveResponse> {
        return googleDriveServiceProvider.rename(fileId, request)
            .map { fetchResponse(it) }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    fun move(fileId: String, map: Map<String, String?>): Single<GoogleDriveResponse> {
        return googleDriveServiceProvider.move(fileId, map)
            .map { fetchResponse(it) }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    fun copy(fileId: String): Single<GoogleDriveResponse> {
        return googleDriveServiceProvider.copy(fileId)
            .map { fetchResponse(it) }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    fun create(request: CreateItemRequest): Single<GoogleDriveResponse> {
        return googleDriveServiceProvider.createItem(request)
            .map { fetchResponse(it) }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    fun createFile(request: CreateItemRequest): Single<Response<GoogleDriveFile>> {
        return googleDriveServiceProvider.createItem(request)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    fun upload(request: CreateItemRequest, map: Map<String, String> = mapOf()): Single<Response<ResponseBody>> {
        return googleDriveServiceProvider.upload(request, map)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    fun update(
        fileId: String,
        map: Map<String, String> = mapOf()
    ): Single<Response<ResponseBody>> {
        return googleDriveServiceProvider.update(fileId, map)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    fun share(fileId: String, request: ShareRequest): Single<Response<ResponseBody>> {
        return googleDriveServiceProvider.share(fileId, request)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    fun export(fileId: String, mimeType: String): Single<Response<ResponseBody>> {
        return googleDriveServiceProvider.export(fileId, mapOf("mimeType" to mimeType))
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
        return googleDriveServiceProvider.getUserInfo(token)
            .map { fetchResponse(it) }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

}