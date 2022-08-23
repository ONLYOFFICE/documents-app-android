package app.editors.manager.managers.providers

import android.net.Uri
import app.editors.manager.app.Api
import app.editors.manager.app.App
import app.editors.manager.mvp.models.base.Base
import app.editors.manager.mvp.models.explorer.*
import app.editors.manager.mvp.models.request.*
import app.editors.manager.mvp.models.response.*
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import okhttp3.ResponseBody
import retrofit2.HttpException
import retrofit2.Response
import java.util.*

class CloudFileProvider : BaseFileProvider {

    var api: Api = App.getApp().getApi()

    override fun getFiles(id: String?, filter: Map<String, String>?): Observable<Explorer> {
        return id?.let {
            api.getItemById(it, mapOf())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map { responseExplorerResponse: Response<ResponseExplorer> ->
                    if (responseExplorerResponse.isSuccessful && responseExplorerResponse.body() != null) {
                        return@map responseExplorerResponse.body()!!.response
                    } else {
                        throw HttpException(responseExplorerResponse)
                    }
                }
        }!!
    }

    override fun search(query: String?): Observable<String>? {
        return query?.let {
            api.search(it)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map { responseSearchResponse ->
                    if (responseSearchResponse.isSuccessful && responseSearchResponse.body() != null) {
                        return@map responseSearchResponse.body()!!.string()
                    } else {
                        throw HttpException(responseSearchResponse)
                    }
                }
        }
    }

    override fun createFile(folderId: String, body: RequestCreate): Observable<CloudFile> {
        return api.createDocs(folderId, body)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .map { responseCreateFile: Response<ResponseCreateFile> ->
                if (responseCreateFile.isSuccessful && responseCreateFile.body() != null) {
                    return@map responseCreateFile.body()!!.response
                } else {
                    throw HttpException(responseCreateFile)
                }
            }
    }

    override fun createFolder(folderId: String, body: RequestCreate): Observable<CloudFolder> {
        return api.createFolder(folderId, body)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .map { responseCreateFolder: Response<ResponseCreateFolder> ->
                if (responseCreateFolder.isSuccessful && responseCreateFolder.body() != null) {
                    return@map responseCreateFolder.body()!!.response
                } else {
                    throw HttpException(responseCreateFolder)
                }
            }
    }

    override fun rename(item: Item, newName: String, version: Int?): Observable<Item> {
        return if (version == null) {
            folderRename(item.id, newName)
        } else {
            fileRename(item.id, newName, version)
        }
    }

    private fun fileRename(id: String, newName: String, version: Int): Observable<Item> {
        val requestRenameFile = RequestRenameFile()
        requestRenameFile.lastVersion = version
        requestRenameFile.title = newName
        return api.renameFile(id, requestRenameFile)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .map { responseFile: Response<ResponseFile> ->
                if (responseFile.isSuccessful && responseFile.body() != null) {
                    return@map responseFile.body()!!.response
                } else if (responseFile.code() == 400) {
                    throw ProviderError.throwForbiddenError()
                } else {
                    throw HttpException(responseFile)
                }
            }
    }

    private fun folderRename(id: String, newName: String): Observable<Item> {
        val requestTitle = RequestTitle()
        requestTitle.title = newName
        return api.renameFolder(id, requestTitle)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .map { responseFolder: Response<ResponseFolder> ->
                if (responseFolder.isSuccessful && responseFolder.body() != null) {
                    return@map responseFolder.body()!!.response
                } else if (responseFolder.code() == 400) {
                    throw ProviderError.throwForbiddenError()
                } else {
                    throw HttpException(responseFolder)
                }
            }
    }

    override fun delete(items: List<Item>, from: CloudFolder?): Observable<List<Operation>> {
        val filesId: MutableList<String> = ArrayList()
        val foldersId: MutableList<String> = ArrayList()
        for (item in items) {
            if (item is CloudFile) {
                filesId.add(item.getId())
            } else if (item is CloudFolder) {
                if (item.getProviderItem()) {
                    removeStorage(item.getId())
                } else {
                    foldersId.add(item.getId())
                }
            }
        }
        val request = RequestBatchBase()
        request.isDeleteAfter = false
        request.fileIds = filesId
        request.folderIds = foldersId
        return Observable.fromCallable {
            api.deleteBatch(
                request
            ).execute()
        }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .map { responseOperationResponse: Response<ResponseOperation?> ->
                if (responseOperationResponse.isSuccessful && responseOperationResponse.body() != null) {
                    return@map responseOperationResponse.body()!!.response
                } else {
                    throw HttpException(responseOperationResponse)
                }
            }
    }

    private fun removeStorage(id: String) {
        api.deleteStorage(id.substring(id.indexOf('-') + 1))
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .map { responseBody: Response<ResponseBody> ->
                if (responseBody.isSuccessful) {
                    val operation = Operation()
                    operation.progress = 100
                    return@map listOf(operation)
                } else {
                    throw HttpException(responseBody)
                }
            }.subscribe()
    }

    override fun download(items: List<Item>): Observable<Int>? {
        return null
    }

    override fun upload(folderId: String, uris: List<Uri?>): Observable<Int>? {
        return null
    }

    override fun transfer(
        items: List<Item>,
        to: CloudFolder?,
        conflict: Int,
        isMove: Boolean,
        isOverwrite: Boolean
    ): Observable<List<Operation>> {
        val filesId: MutableList<String> = ArrayList()
        val foldersId: MutableList<String> = ArrayList()
        for (item in items) {
            if (item is CloudFile) {
                filesId.add(item.getId())
            } else if (item is CloudFolder) {
                foldersId.add(item.getId())
            }
        }
        val batchOperation = RequestBatchOperation()
        batchOperation.fileIds = filesId
        batchOperation.folderIds = foldersId
        batchOperation.isDeleteAfter = false
        batchOperation.destFolderId = to!!.id
        batchOperation.conflictResolveType = conflict
        return if (isMove) {
            moveItems(batchOperation)
        } else {
            copyFiles(batchOperation)
        }
    }

    private fun moveItems(body: RequestBatchOperation): Observable<List<Operation>> {
        return api.move(body)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .map { responseOperation: Response<ResponseOperation> ->
                if (responseOperation.isSuccessful && responseOperation.body() != null) {
                    return@map responseOperation.body()!!.response
                } else {
                    throw HttpException(responseOperation)
                }
            }
    }

    private fun copyFiles(body: RequestBatchOperation): Observable<List<Operation>> {
        return api.copy(body)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .map { responseOperation: Response<ResponseOperation> ->
                if (responseOperation.isSuccessful && responseOperation.body() != null) {
                    return@map responseOperation.body()!!.response
                } else {
                    throw HttpException(responseOperation)
                }
            }
    }

    override fun fileInfo(item: Item?): Observable<CloudFile> {
        return api.getFileInfo(item?.id)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .map { responseFile: Response<ResponseFile> ->
                if (responseFile.isSuccessful && responseFile.body() != null) {
                    return@map responseFile.body()!!.response
                } else {
                    throw HttpException(responseFile)
                }
            }
    }

    override fun getStatusOperation(): ResponseOperation {
        return api.status()
            .blockingGet()
    }

    override fun share(id: String, requestExternal: RequestExternal): Observable<ResponseExternal> {
        return api.getExternalLink(id, requestExternal)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .map { responseExternal: Response<ResponseExternal> ->
                if (responseExternal.isSuccessful) {
                    return@map responseExternal.body()
                } else {
                    throw HttpException(responseExternal)
                }
            }
    }

    override fun terminate(): Observable<List<Operation>> {
        return Observable.fromCallable {
            api.terminate().execute()
        }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .map { operationResponse: Response<ResponseOperation?> ->
                if (operationResponse.isSuccessful && operationResponse.body() != null) {
                    return@map operationResponse.body()!!.response
                } else {
                    throw HttpException(operationResponse)
                }
            }
    }

    override fun addToFavorites(requestFavorites: RequestFavorites): Observable<Base> {
        return api.addToFavorites(requestFavorites)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .map { baseResponse: Response<Base> -> baseResponse.body() }
    }

    override fun deleteFromFavorites(requestFavorites: RequestFavorites): Observable<Base> {
        return api.deleteFromFavorites(requestFavorites)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .map { baseResponse: Response<Base> -> baseResponse.body() }
    }

    fun clearTrash(): Observable<List<Operation>> {
        return api.emptyTrash()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .map { operationResponse: Response<ResponseOperation> ->
                if (operationResponse.isSuccessful && operationResponse.body() != null) {
                    return@map operationResponse.body()!!.response
                } else {
                    throw HttpException(operationResponse)
                }
            }
    }
}