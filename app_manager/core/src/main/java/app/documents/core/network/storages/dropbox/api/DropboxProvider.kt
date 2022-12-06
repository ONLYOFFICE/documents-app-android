package app.documents.core.network.storages.dropbox.api

import app.documents.core.network.common.utils.DropboxUtils
import app.documents.core.network.storages.IStorageProvider
import app.documents.core.network.storages.dropbox.login.DropboxResponse
import app.documents.core.network.storages.dropbox.models.operations.MoveCopyBatchCheck
import app.documents.core.network.storages.dropbox.models.request.*
import com.jakewharton.rxrelay2.BehaviorRelay
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.HttpException
import retrofit2.Response

class DropboxProvider(
    private val dropBoxService: DropboxService,
    private val dropBoxContentService: DropboxContentService,
    private val dropboxErrorHandler: BehaviorRelay<DropboxResponse.Error>? = null
) : IStorageProvider {

    fun getFiles(request: ExplorerRequest): Single<DropboxResponse> {
        return dropBoxService.getFiles(request)
            .map { fetchResponse(it) }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    fun getNextFileList(request: ExplorerContinueRequest): Single<DropboxResponse> {
        return dropBoxService.getNextFileList(request)
            .map { fetchResponse(it) }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    fun download(request: String): Single<Response<ResponseBody>> {
        return dropBoxContentService.download(request)
            //.map { fetchResponse(it) }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    fun downloadFolder(request: String): Single<Response<ResponseBody>> {
        return dropBoxContentService.downloadFolder(request)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    fun delete(request: PathRequest): Single<Response<ResponseBody>> {
        return dropBoxService.delete(request)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    fun createFolder(request: CreateFolderRequest): Single<DropboxResponse> {
        return dropBoxService.createFolder(request)
            .map { fetchResponse(it) }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    fun getExternalLink(request: PathRequest): Single<DropboxResponse> {
        return dropBoxService.getExternalLink(request)
            .map { fetchResponse(it) }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    fun move(request: MoveRequest): Single<DropboxResponse> {
        return dropBoxService.move(request)
            .map { fetchResponse(it) }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    fun moveBatch(request: MoveCopyBatchRequest): Single<DropboxResponse> {
        return dropBoxService.moveBatch(request)
            .map { fetchResponse(it) }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    fun copy(request: MoveRequest): Single<DropboxResponse> {
        return dropBoxService.copy(request)
            .map { fetchResponse(it) }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    fun copyBatch(request: MoveCopyBatchRequest): Single<DropboxResponse> {
        return dropBoxService.copyBatch(request)
            .map { fetchResponse(it) }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    fun copyBatchCheck(request: MoveCopyBatchCheck): Single<Response<ResponseBody>> {
        return dropBoxService.copyBatchCheck(request)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    fun moveBatchCheck(request: MoveCopyBatchCheck): Single<Response<ResponseBody>> {
        return dropBoxService.moveBatchCheck(request)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    fun search(request: SearchRequest): Single<DropboxResponse> {
        return dropBoxService.search(request)
            .map { fetchResponse(it) }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    fun searchNextList(request: ExplorerContinueRequest): Single<DropboxResponse> {
        return dropBoxService.searchNextList(request)
            .map { fetchResponse(it) }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    fun upload(request: String, part: MultipartBody.Part): Single<DropboxResponse> {
        return dropBoxContentService.upload(request, part)
            .map { fetchResponse(it) }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    private fun <T> fetchResponse(response: Response<T>): DropboxResponse {
        return if (response.isSuccessful && response.body() != null) {
            DropboxResponse.Success(response.body()!!)
        } else {
            val error = DropboxUtils.getErrorMessage(response)
            dropboxErrorHandler?.accept(error)
            return error
        }
    }

}