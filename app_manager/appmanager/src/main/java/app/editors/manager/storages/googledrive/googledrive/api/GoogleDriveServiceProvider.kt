package app.editors.manager.storages.googledrive.googledrive.api

import app.editors.manager.storages.googledrive.googledrive.login.GoogleDriveResponse
import app.editors.manager.storages.googledrive.mvp.models.GoogleDriveFile
import app.editors.manager.storages.googledrive.mvp.models.request.CreateItemRequest
import app.editors.manager.storages.googledrive.mvp.models.request.RenameRequest
import app.editors.manager.storages.googledrive.mvp.models.request.ShareRequest
import com.jakewharton.rxrelay2.BehaviorRelay
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import okhttp3.ResponseBody
import retrofit2.HttpException
import retrofit2.Response

class GoogleDriveServiceProvider(
    private val googleDriveServiceProvider: GoogleDriveService,
    private val googleDriveErrorHandle: BehaviorRelay<GoogleDriveResponse.Error>? = null
): IGoogleDriveServiceProvider {

    override fun getFiles(map: Map<String, String>): Single<GoogleDriveResponse> {
        return googleDriveServiceProvider.getFiles(map)
            .map { fetchResponse(it) }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    override fun getFileInfo(
        fileId: String,
        map: Map<String, String>
    ): Single<GoogleDriveResponse> {
        return googleDriveServiceProvider.getFileInfo(fileId, map)
            .map { fetchResponse(it) }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    override fun download(
        fileId: String,
        map: Map<String, String>
    ): Single<Response<ResponseBody>> {
        return googleDriveServiceProvider.download(fileId, map)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    override fun delete(fileId: String): Single<Response<ResponseBody>> {
        return googleDriveServiceProvider.deleteItem(fileId)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    override fun rename(fileId: String, request: RenameRequest): Single<GoogleDriveResponse> {
        return googleDriveServiceProvider.rename(fileId, request)
            .map { fetchResponse(it) }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    override fun move(fileId: String, map: Map<String, String?>): Single<GoogleDriveResponse> {
        return googleDriveServiceProvider.move(fileId, map)
            .map { fetchResponse(it) }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    override fun copy(fileId: String): Single<GoogleDriveResponse> {
        return googleDriveServiceProvider.copy(fileId)
            .map { fetchResponse(it) }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    override fun create(request: CreateItemRequest): Single<GoogleDriveResponse> {
        return googleDriveServiceProvider.createItem(request)
            .map { fetchResponse(it) }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    override fun createFile(request: CreateItemRequest): Single<Response<GoogleDriveFile>> {
        return googleDriveServiceProvider.createItem(request)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    override fun upload(request: CreateItemRequest, map: Map<String, String>): Single<Response<ResponseBody>> {
        return googleDriveServiceProvider.upload(request, map)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    override fun update(
        fileId: String,
        map: Map<String, String>
    ): Single<Response<ResponseBody>> {
        return googleDriveServiceProvider.update(fileId, map)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    override fun share(fileId: String, request: ShareRequest): Single<Response<ResponseBody>> {
        return googleDriveServiceProvider.share(fileId, request)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    override fun export(fileId: String, mimeType: String): Single<Response<ResponseBody>> {
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

    override fun getUserInfo(token: String): Single<GoogleDriveResponse> {
        return googleDriveServiceProvider.getUserInfo(token)
            .map { fetchResponse(it) }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

}