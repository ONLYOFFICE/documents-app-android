package app.editors.manager.storages.onedrive.onedrive

import app.editors.manager.storages.onedrive.mvp.models.request.*
import com.jakewharton.rxrelay2.BehaviorRelay
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import okhttp3.ResponseBody
import retrofit2.HttpException
import retrofit2.Response

class OneDriveServiceProvider(
    private val oneDriveService: OneDriveService,
    private val oneDriveErrorHandler: BehaviorRelay<OneDriveResponse.Error>? = null
): IOneDriveServiceProvider {

    override fun authorization(parameters: Map<String, String>): Single<OneDriveResponse> {
        return oneDriveService.authorization(parameters)
            .map { fetchResponse(it) }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    override fun getFiles(map: Map<String, String>): Single<OneDriveResponse> {
        return oneDriveService.getFiles(map)
            .map { fetchResponse(it) }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    override fun getChildren(itemId: String, map: Map<String, String>): Single<OneDriveResponse> {
        return oneDriveService.getChildren(itemId, map)
            .map { fetchResponse(it) }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    override fun getRoot(): Single<OneDriveResponse> {
        return oneDriveService.getRoot()
            .map { fetchResponse(it) }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    override fun download(itemId: String): Single<OneDriveResponse> {
        return oneDriveService.download(itemId)
            .map { fetchResponse(it) }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    override fun deleteItem(itemId: String): Single<Response<ResponseBody>> {
        return oneDriveService.deleteItem(itemId)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    override fun renameItem(itemId: String, request: RenameRequest): Single<Response<ResponseBody>> {
        return oneDriveService.renameItem(itemId, request)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    override fun createFolder(
        itemId: String,
        request: CreateFolderRequest
    ): Single<OneDriveResponse> {
        return oneDriveService.createFolder(itemId, request)
            .map { fetchResponse(it) }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    override fun createFile(
        itemId: String,
        fileName: String,
        opts: Map<String, String>
    ): Single<OneDriveResponse> {
        return oneDriveService.createFile(itemId, fileName, opts)
            .map{fetchResponse(it)}
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    override fun updateFile(itemId: String, request: ChangeFileRequest): Single<Response<ResponseBody>> {
        return oneDriveService.updateFile(itemId, request)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    override fun uploadFile(folderId: String, fileName: String, request: UploadRequest): Single<OneDriveResponse> {
        return oneDriveService.uploadFile(folderId, fileName, request)
            .map { fetchResponse(it) }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())

    }

    override fun copyItem(
        itemId: String,
        request: CopyItemRequest
    ): Single<Response<ResponseBody>> {
        return oneDriveService.copyItem(itemId, request)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    override fun moveItem(
        itemId: String,
        request: CopyItemRequest
    ): Single<Response<ResponseBody>> {
        return oneDriveService.moveItem(itemId, request)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    override fun getPhoto(): Single<Response<ResponseBody>> {
        return oneDriveService.getPhoto()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    override fun filter(value: String, map: Map<String, String>): Single<OneDriveResponse> {
        return oneDriveService.filter(value, map)
            .map { fetchResponse(it) }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    override fun getExternalLink(
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