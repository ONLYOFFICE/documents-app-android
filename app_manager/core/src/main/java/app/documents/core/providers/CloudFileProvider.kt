package app.documents.core.providers

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import androidx.core.net.toUri
import app.documents.core.network.common.contracts.ApiContract
import app.documents.core.network.common.models.BaseResponse
import app.documents.core.network.manager.ManagerService
import app.documents.core.network.manager.models.explorer.*
import app.documents.core.network.manager.models.request.*
import app.documents.core.network.manager.models.response.*
import app.documents.core.network.room.RoomService
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import lib.toolkit.base.managers.utils.AccountUtils
import lib.toolkit.base.managers.utils.FileUtils
import okhttp3.ResponseBody
import retrofit2.HttpException
import retrofit2.Response
import java.io.File
import java.io.FileNotFoundException
import javax.inject.Inject

class CloudFileProvider @Inject constructor(
    private val managerService: ManagerService,
    private val roomService: RoomService
) : BaseFileProvider {

    interface RoomCallback {
        fun isRoomRoot(id: String?): Boolean
        fun isArchive(): Boolean
    }

    var roomCallback: RoomCallback? = null

    private var sendingCachedFile: File? = null

    override fun getFiles(id: String?, filter: Map<String, String>?): Observable<Explorer> {
        return when {
            roomCallback?.isRoomRoot(id) == true -> getRooms(filter, roomCallback!!::isArchive)
            else -> managerService.getItemById(id.orEmpty(), filter)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map { responseExplorerResponse: Response<ResponseExplorer> ->
                    if (responseExplorerResponse.isSuccessful && responseExplorerResponse.body() != null) {
                        return@map responseExplorerResponse.body()?.response
                    } else {
                        throw HttpException(responseExplorerResponse)
                    }
                }
        }
    }

    override fun search(query: String?): Observable<String>? {
        return query?.let {
            managerService.search(it)
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
        return managerService.createDocs(folderId, body)
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
        return managerService.createFolder(folderId, body)
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
        return managerService.renameFile(id, requestRenameFile)
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
        return managerService.renameFolder(id, requestTitle)
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
        return managerService.deleteBatch(getDeleteRequest(items))
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .map { responseOperationResponse: Response<ResponseOperation> ->
                if (responseOperationResponse.isSuccessful && responseOperationResponse.body() != null) {
                    responseOperationResponse.body()!!.response
                } else {
                    throw HttpException(responseOperationResponse)
                }
            }
    }

    private fun getDeleteRequest(items: List<Item>): RequestBatchBase {
        val isArchive = roomCallback?.isArchive() == true
        val filesId: MutableList<String> = mutableListOf()
        val foldersId: MutableList<String> = mutableListOf()

        items.forEach { item ->
            val id = item.id
            when (item) {
                is CloudFile -> filesId.add(id)
                is CloudFolder -> {
                    if (item.providerItem) {
                        removeStorage(id)
                    } else {
                        foldersId.add(id)
                    }
                }
            }
        }

        return RequestBatchBase().apply {
            isDeleteAfter = isArchive
            isImmediately = isArchive
            fileIds = filesId
            folderIds = foldersId
        }
    }

    private fun removeStorage(id: String) {
        managerService.deleteStorage(id.substring(id.indexOf('-') + 1))
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
        to: CloudFolder,
        conflict: Int,
        isMove: Boolean,
        isOverwrite: Boolean
    ): Observable<List<Operation>> {
        val filesId: MutableList<String> = ArrayList()
        val foldersId: MutableList<String> = ArrayList()
        for (item in items) {
            if (item is CloudFile) {
                filesId.add(item.id)
            } else if (item is CloudFolder) {
                foldersId.add(item.id)
            }
        }
        val batchOperation =
            RequestBatchOperation()
        batchOperation.fileIds = filesId
        batchOperation.folderIds = foldersId
        batchOperation.isDeleteAfter = false
        batchOperation.destFolderId = to.id
        batchOperation.conflictResolveType = conflict
        return if (isMove) {
            moveItems(batchOperation)
        } else {
            copyFiles(batchOperation)
        }
    }

    private fun moveItems(body: RequestBatchOperation): Observable<List<Operation>> {
        return managerService.move(body)
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
        return managerService.copy(body)
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

    fun getRooms(filters: Map<String, String>?, isArchive: (() -> Boolean)? = null): Observable<Explorer> {
        val roomFilter = filters?.toMutableMap()?.apply {
            remove(ApiContract.Parameters.ARG_FILTER_BY_TYPE)
            remove(ApiContract.Parameters.ARG_FILTER_SUBFOLDERS)
            if (isArchive?.invoke() == true) {
                put("searchArea", "Archive")
            }
        }
        return roomService.getAllRooms(roomFilter)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .map { responseExplorerResponse: Response<ResponseExplorer> ->
                if (responseExplorerResponse.isSuccessful && responseExplorerResponse.body() != null) {
                    return@map responseExplorerResponse.body()?.response
                } else {
                    throw HttpException(responseExplorerResponse)
                }
            }
    }

    override fun fileInfo(item: Item?): Observable<CloudFile> {
        return managerService.getFileInfo(item?.id)
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
        return managerService.status()
            .blockingGet()
    }

    override fun share(id: String, requestExternal: RequestExternal): Observable<ResponseExternal> {
        return managerService.getExternalLink(id, requestExternal)
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
            managerService.terminate().execute()
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

    fun addToFavorites(requestFavorites: RequestFavorites, isAdd: Boolean): Observable<BaseResponse> {
        return if (isAdd) {
            managerService.addToFavorites(requestFavorites)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map { baseResponse: Response<BaseResponse> -> baseResponse.body() }
        } else {
            managerService.deleteFromFavorites(requestFavorites)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map { baseResponse: Response<BaseResponse> -> baseResponse.body() }
        }
    }

    fun clearTrash(): Observable<List<Operation>> {
        return managerService.emptyTrash()
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

    @SuppressLint("MissingPermission")
    fun cacheSendingFile(context: Context, cloudFile: CloudFile, account: String): Single<File> {
        return managerService.downloadFile(
            url = cloudFile.viewUrl,
            cookie = ApiContract.COOKIE_HEADER + AccountUtils.getToken(context, account)
        )
            .subscribeOn(Schedulers.io())
            .map { response ->
                val responseBody = response.body()
                if (response.isSuccessful && responseBody != null) {
                    FileUtils.createCacheFile(context, cloudFile.title)?.also { file ->
                        FileUtils.writeFromResponseBody(
                            response = responseBody,
                            to = file.toUri(),
                            context = context
                        )
                    } ?: throw FileNotFoundException("Caching file error")
                } else throw HttpException(response)
            }
            .observeOn(AndroidSchedulers.mainThread())
    }

    fun removeSendingCachedFile() {
        sendingCachedFile?.delete()
    }

}