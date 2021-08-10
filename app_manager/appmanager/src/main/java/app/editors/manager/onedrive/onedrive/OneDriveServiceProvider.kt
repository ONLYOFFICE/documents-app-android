package app.editors.manager.onedrive.onedrive

import app.editors.manager.managers.providers.IOneDriveServiceProvider
import app.editors.manager.managers.providers.OneDriveResponse
import app.editors.manager.onedrive.mvp.models.request.ChangeFileRequest
import app.editors.manager.onedrive.mvp.models.request.CreateFolderRequest
import app.editors.manager.onedrive.mvp.models.request.RenameRequest
import app.editors.manager.onedrive.mvp.models.request.UploadRequest
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

    override fun userInfo(): Single<OneDriveResponse> {
        return oneDriveService.getUserInfo()
            .map { fetchResponse(it) }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    override fun getFiles(): Single<OneDriveResponse> {
        return oneDriveService.getFiles()
            .map { fetchResponse(it) }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    override fun getChildren(itemId: String): Single<OneDriveResponse> {
        return oneDriveService.getChildren(itemId)
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
        ext: String,
        opts: Map<String, String>
    ): Single<OneDriveResponse> {
        return oneDriveService.createFile(itemId, ext, opts)
            .map{fetchResponse(it)}
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    override fun updateFile(itemId: String, body: ChangeFileRequest): Single<Response<ResponseBody>> {
        return oneDriveService.updateFile(itemId, body)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    override fun uploadFile(folderId: String, fileName: String, request: UploadRequest): Single<OneDriveResponse> {
        return oneDriveService.uploadFile(folderId, fileName, request)
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