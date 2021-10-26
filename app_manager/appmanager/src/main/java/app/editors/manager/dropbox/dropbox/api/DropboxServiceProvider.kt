package app.editors.manager.dropbox.dropbox.api

import app.editors.manager.dropbox.dropbox.login.DropboxResponse
import app.editors.manager.dropbox.mvp.models.request.CreateFolderRequest
import app.editors.manager.dropbox.mvp.models.request.DeleteRequest
import app.editors.manager.dropbox.mvp.models.request.ExplorerRequest
import app.editors.manager.dropbox.mvp.models.request.MoveRequest
import com.jakewharton.rxrelay2.BehaviorRelay
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import okhttp3.ResponseBody
import retrofit2.HttpException
import retrofit2.Response

class DropboxServiceProvider(
    private val dropBoxService: DropboxService,
    private val dropboxErrorHandler: BehaviorRelay<DropboxResponse.Error>? = null
): IDropboxServiceProvider {

    override fun getFiles(request: ExplorerRequest): Single<DropboxResponse> {
        return dropBoxService.getFiles(request)
            .map { fetchResponse(it) }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    override fun download(request: String): Single<Response<ResponseBody>> {
        return dropBoxService.download(request)
            //.map { fetchResponse(it) }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    override fun downloadFolder(request: String): Single<Response<ResponseBody>> {
        return dropBoxService.downloadFolder(request)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    override fun delete(request: DeleteRequest): Single<Response<ResponseBody>> {
        return dropBoxService.delete(request)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    override fun createFolder(request: CreateFolderRequest): Single<DropboxResponse> {
        return dropBoxService.createFolder(request)
            .map { fetchResponse(it) }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    override fun getExternalLink(request: DeleteRequest): Single<DropboxResponse> {
        return dropBoxService.getExternalLink(request)
            .map { fetchResponse(it) }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    override fun move(request: MoveRequest): Single<DropboxResponse> {
        return dropBoxService.move(request)
            .map { fetchResponse(it) }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    private fun <T> fetchResponse(response: Response<T>): DropboxResponse {
        return if (response.isSuccessful && response.body() != null) {
            DropboxResponse.Success(response.body()!!)
        } else {
            val error = DropboxResponse.Error(HttpException(response))
            dropboxErrorHandler?.accept(error)
            return error
        }
    }
}