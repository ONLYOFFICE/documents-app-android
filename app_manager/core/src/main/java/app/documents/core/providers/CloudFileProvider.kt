package app.documents.core.providers

import android.annotation.SuppressLint
import android.content.Context
import app.documents.core.account.AccountRepository
import app.documents.core.manager.ManagerRepository
import app.documents.core.model.cloud.Access
import app.documents.core.network.common.NetworkClient
import app.documents.core.network.common.asResult
import app.documents.core.network.common.contracts.ApiContract
import app.documents.core.network.common.models.BaseResponse
import app.documents.core.network.manager.ManagerService
import app.documents.core.network.manager.models.explorer.CloudFile
import app.documents.core.network.manager.models.explorer.CloudFolder
import app.documents.core.network.manager.models.explorer.Explorer
import app.documents.core.network.manager.models.explorer.FormRole
import app.documents.core.network.manager.models.explorer.Item
import app.documents.core.network.manager.models.explorer.Operation
import app.documents.core.network.manager.models.request.RequestBatchBase
import app.documents.core.network.manager.models.request.RequestBatchOperation
import app.documents.core.network.manager.models.request.RequestCreate
import app.documents.core.network.manager.models.request.RequestDeleteRecent
import app.documents.core.network.manager.models.request.RequestFavorites
import app.documents.core.network.manager.models.request.RequestRenameFile
import app.documents.core.network.manager.models.request.RequestStopFilling
import app.documents.core.network.manager.models.request.RequestTitle
import app.documents.core.network.manager.models.response.ResponseCreateFile
import app.documents.core.network.manager.models.response.ResponseCreateFolder
import app.documents.core.network.manager.models.response.ResponseExplorer
import app.documents.core.network.manager.models.response.ResponseFile
import app.documents.core.network.manager.models.response.ResponseFolder
import app.documents.core.network.manager.models.response.ResponseOperation
import app.documents.core.network.room.RoomService
import app.documents.core.network.room.models.DeleteVersionRequest
import app.documents.core.network.room.models.EditCommentRequest
import app.documents.core.utils.FirebaseTool
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import lib.toolkit.base.managers.utils.EditType
import lib.toolkit.base.managers.utils.StringUtils
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.HttpException
import retrofit2.Response
import java.io.InputStream
import javax.inject.Inject
import kotlin.coroutines.resume
import app.documents.core.network.common.Result as NetworkResult


data class OpenDocumentResult(
    val info: String? = null,
    val isPdf: Boolean = false
)

class CloudFileProvider @Inject constructor(
    private val context: Context,
    private val managerService: ManagerService,
    private val roomService: RoomService,
    private val managerRepository: ManagerRepository,
    private val accountRepository: AccountRepository,
    private val firebaseTool: FirebaseTool
) : BaseFileProvider, BaseCloudFileProvider, CacheFileHelper {

    companion object {
        private const val KEY_RESPONSE = "response"
        const val STATIC_DOC_URL = "/web-apps/apps/api/documents/api.js"
    }

    interface RoomCallback {
        fun isRoomRoot(id: String?): Boolean
        fun isArchive(): Boolean
        fun isRecent(): Boolean
    }

    var roomCallback: RoomCallback? = null

    override fun getFiles(id: String?, filter: Map<String, String>?): Observable<Explorer> {
        return when {
            roomCallback?.isRoomRoot(id) == true -> getRooms(filter, roomCallback!!::isArchive)
            roomCallback?.isRecent() == true -> getRecentViaLink(filter.orEmpty()).toObservable()
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

    //TODO Rework the creation for collaboration
    override fun createFile(folderId: String, title: String): Observable<CloudFile> {
        return managerService.createDocs(folderId, RequestCreate(title))
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

    override fun transfer(
        items: List<Item>,
        to: CloudFolder,
        conflict: Int,
        isMove: Boolean,
        isOverwrite: Boolean
    ): Observable<List<Operation>> {
        val batchOperation =
            RequestBatchOperation()
        batchOperation.fileIds = items.filterIsInstance<CloudFile>().map { it.id }
        batchOperation.folderIds = items.filterIsInstance<CloudFolder>().map { it.id }
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

    fun copyFiles(body: RequestBatchOperation): Observable<List<Operation>> {
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

    fun getRooms(
        filters: Map<String, String>?,
        isArchive: (() -> Boolean)? = null
    ): Observable<Explorer> {
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
        val version = (item as? CloudFile)?.version
        return managerService.getFileInfo(item?.id, version)
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

    fun getFileInfo(
        fileId: String,
        version: Int? = null
    ): Flow<NetworkResult<CloudFile>> {
        return flow {
            val response = managerService.suspendGetFileInfo(fileId, version)
            if (!response.isSuccessful) throw HttpException(response)
            emit(checkNotNull(response.body()?.response))
        }
            .flowOn(Dispatchers.IO)
            .asResult()
    }

    fun addToFavorites(
        requestFavorites: RequestFavorites,
        isAdd: Boolean
    ): Observable<BaseResponse> {
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
    override fun getDownloadResponse(
        cloudFile: CloudFile,
        token: String?
    ): Single<Response<ResponseBody>> {
        return managerService.downloadFile(
            url = cloudFile.viewUrl,
            cookie = ApiContract.COOKIE_HEADER + token
        )
    }

    override suspend fun suspendGetDownloadResponse(
        cloudFile: CloudFile,
        token: String?
    ): Response<ResponseBody> {
        return managerService.suspendDownloadFile(
            url = managerService.getDownloadFileLink(cloudFile.id).response,
            cookie = ApiContract.COOKIE_HEADER + token
        )
    }

    suspend fun convertToOOXML(id: String): Flow<Int> = withContext(Dispatchers.IO) {
        managerService.getConversionStatus(id, true)
        return@withContext flow {
            var progress = 0
            while (progress != 100) {
                val response = managerService.getConversionStatus(id, false).response
                if (response.isNotEmpty()) {
                    val status = response[0]
                    progress = status.progress
                    emit(status.progress)
                    delay(100L)
                }
            }
        }
    }

    override fun updateDocument(id: String, body: MultipartBody.Part): Single<Boolean> {
        return managerService.updateDocument(id, body)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread()).map { response ->
                return@map response.isSuccessful
            }
    }

    fun getRecentViaLink(filter: Map<String, String> = mapOf()): Single<Explorer> {
        val params = filter.plus("searchArea" to "3")
        return managerService.getRecentViaLink(params)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .map { it.response }
    }

    fun deleteRecent(fileIds: List<String>): Single<Response<ResponseBody>> {
        return managerService.deleteRecent(RequestDeleteRecent(fileIds))
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    fun openFile(
        portal: String,
        token: String,
        id: String,
        title: String,
        extension: String,
    ): Flow<NetworkResult<FileOpenResult>> {
        return flow {
            val api = NetworkClient.getRetrofit<ManagerService>(portal, token, context)
            val fileJson = JSONObject(api.suspendOpenFile(id).body()?.string().toString())
                .getJSONObject(KEY_RESPONSE)

            val docServiceJson = JSONObject(
                api.suspendGetDocService().body()?.string().toString()
            )

            val docService = if (docServiceJson.optJSONObject(KEY_RESPONSE) != null) {
                docServiceJson.getJSONObject(KEY_RESPONSE)
                    .getString("docServiceUrlApi")
                    .replace(STATIC_DOC_URL, "")
            } else {
                docServiceJson.getString(KEY_RESPONSE)
                    .replace(STATIC_DOC_URL, "")
            }

            val result = withContext(Dispatchers.IO) {
                fileJson
                    .put("url", docService)
                    .put("fileId", id)
                    .put("canShareable", false)
            }

            emit(
                FileOpenResult.OpenDocumentServer(
                    cloudFile = CloudFile().apply {
                        this.id = id
                        this.title = title
                        this.fileExst = extension
                    },
                    info = result.toString(),
                    editType = EditType.Edit()
                )
            )
        }
            .flowOn(Dispatchers.IO)
            .asResult()
    }

    override fun openFile(
        cloudFile: CloudFile,
        editType: EditType,
        canBeShared: Boolean
    ): Flow<NetworkResult<FileOpenResult>> {
        return flowOf()
    }

    fun openFile(
        cloudFile: CloudFile,
        editType: EditType,
        canBeShared: Boolean,
        access: Access
    ): Flow<NetworkResult<FileOpenResult>> {
        return flow {
            emit(FileOpenResult.Loading())
            val token = checkNotNull(accountRepository.getOnlineToken())
            when {
                StringUtils.isDocument(cloudFile.fileExst) -> {
                    if (firebaseTool.isCoauthoring()) {
                        val document = openDocument(
                            cloudFile = cloudFile,
                            token = token,
                            canShareable = canBeShared,
                            editType = editType
                        )

                        if (document.isPdf) {
                            val cachedFile = suspendGetCachedFile(context, cloudFile, token)
                            emit(
                                FileOpenResult.OpenLocally(
                                    file = cachedFile,
                                    fileId = cloudFile.id,
                                    editType = editType,
                                    access = access
                                )
                            )
                        } else {
                            emit(
                                FileOpenResult.OpenDocumentServer(
                                    cloudFile = cloudFile,
                                    info = checkNotNull(document.info),
                                    editType = editType
                                )
                            )
                        }
                    } else {
                        val cachedFile = suspendGetCachedFile(context, cloudFile, token)
                        emit(
                            FileOpenResult.OpenLocally(
                                file = cachedFile,
                                fileId = cloudFile.id,
                                editType = editType,
                                access = access
                            )
                        )
                    }
                }

                StringUtils.isMedia(cloudFile.fileExst) -> emit(
                    FileOpenResult.OpenCloudMedia(cloudFile)
                )

                else -> emit(FileOpenResult.DownloadNotSupportedFile())
            }
        }
            .flowOn(Dispatchers.IO)
            .asResult()
    }

    @OptIn(InternalCoroutinesApi::class)
    suspend fun openFile(
        id: String,
        editType: EditType,
        canBeShared: Boolean,
        version: Int? = null
    ): Flow<NetworkResult<FileOpenResult>> {
        val cloudFile = suspendCancellableCoroutine<CloudFile> { cont ->
            fileInfo(CloudFile().apply { this.id = id })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext { cloudFile -> cont.resume(cloudFile) }
                .doOnError { error -> cont.tryResumeWithException(error) }
                .subscribe()
        }
        return if (version != null) {
            openDocumentVersion(
                cloudFile = cloudFile,
                version = version
            )
        } else {
            openFile(
                cloudFile = cloudFile,
                editType = editType,
                canBeShared = canBeShared,
                access = cloudFile.access
            )
        }
    }

    private fun openDocumentVersion(
        cloudFile: CloudFile,
        version: Int
    ): Flow<NetworkResult<FileOpenResult>> {
        return flow {
            emit(FileOpenResult.Loading())

            if (firebaseTool.isCoauthoring()) {
                val info = openEdit(
                    cloudFile = cloudFile,
                    canShareable = false,
                    editType = EditType.Edit(),
                    version = version
                )
                emit(
                    FileOpenResult.OpenDocumentServer(
                        cloudFile = cloudFile,
                        info = info,
                        editType = EditType.Edit()
                    )
                )
                return@flow
            }

            val token = accountRepository.getOnlineToken()
            val response = managerService.suspendDownloadFile(
                url = "${cloudFile.viewUrl}&version=$version",
                cookie = ApiContract.COOKIE_HEADER + token
            )
            emit(
                FileOpenResult.OpenLocally(
                    file = mapDownloadResponse(context, cloudFile, response),
                    fileId = cloudFile.id,
                    editType = EditType.Edit(),
                    access = if (version == cloudFile.version) {
                        cloudFile.access
                    } else {
                        Access.Read
                    }
                )
            )
        }
            .flowOn(Dispatchers.IO)
            .asResult()
    }

    @SuppressLint("CheckResult")
    private suspend fun openDocument(
        cloudFile: CloudFile,
        token: String?,
        canShareable: Boolean,
        editType: EditType
    ): OpenDocumentResult {
        return if (StringUtils.getExtension(cloudFile.fileExst) == StringUtils.Extension.PDF) {
            val fileResponse = managerService.suspendDownloadFile(
                url = cloudFile.viewUrl,
                cookie = ApiContract.COOKIE_HEADER + token
            )
            if (!fileResponse.isSuccessful) throw HttpException(fileResponse)

            if (checkOFORMPdf(fileResponse.body()?.byteStream())) {
                val info = openEdit(
                    cloudFile = cloudFile,
                    canShareable = canShareable,
                    editType = editType
                )
                OpenDocumentResult(info = info)
            } else {
                OpenDocumentResult(isPdf = false)
            }
        } else {
            val info = openEdit(
                cloudFile = cloudFile,
                canShareable = canShareable,
                editType = editType
            )
            OpenDocumentResult(info = info)
        }
    }

    private fun checkOFORMPdf(inputStream: InputStream?): Boolean {
        return ByteArray(110)
            .apply { inputStream?.use { stream -> stream.read(this, 0, size) } }
            .decodeToString()
            .contains("/ONLYOFFICEFORM")
    }

    private suspend fun openEdit(
        cloudFile: CloudFile,
        canShareable: Boolean? = null,
        editType: EditType,
        version: Int? = null
    ): String {
        managerRepository.updateDocumentServerVersion()
        val file = checkNotNull(managerService.suspendGetFileInfo(cloudFile.id).body()?.response)
        val response = managerService.suspendOpenFile(
            id = file.id,
            version = version ?: file.version,
            edit = editType is EditType.Edit,
            fill = editType is EditType.Fill,
            view = editType is EditType.View,
        )
        if (!response.isSuccessful) throw HttpException(response)

        val docServiceResponse = managerService.suspendGetDocService()
        if (!docServiceResponse.isSuccessful) throw HttpException(response)

        val docServiceJson = JSONObject(docServiceResponse.body()?.string().orEmpty())
        val docService = if (docServiceJson.optJSONObject(KEY_RESPONSE) != null) {
            docServiceJson.getJSONObject(KEY_RESPONSE)
                .getString("docServiceUrlApi")
                .replace(STATIC_DOC_URL, "")
        } else {
            docServiceJson.getString(KEY_RESPONSE)
                .replace(STATIC_DOC_URL, "")
        }

        return JSONObject(response.body()?.string().orEmpty())
            .getJSONObject(KEY_RESPONSE)
            .put("url", docService)
            .put("size", cloudFile.pureContentLength)
            .put("updated", cloudFile.updated.time)
            .put("fileId", cloudFile.id)
            .put("canShareable", canShareable)
            .toString()
    }

    fun getVersionHistory(fileId: String): Flow<Result<List<CloudFile>>> = apiFlow {
        val response = managerService.getVersionHistory(fileId)
        val files = response.body()?.response
        if (response.isSuccessful && files != null) files
        else throw HttpException(response)
    }

    fun restoreVersion(fileId: String, version: Int): Flow<Result<Unit>> = apiFlow {
        val response = managerService.restoreVersion(fileId, version)
        if (!response.isSuccessful) throw HttpException(response)
    }

    fun editVersionComment(
        fileId: String,
        version: Int,
        comment: String
    ): Flow<Result<Unit>> = apiFlow {
        val body = EditCommentRequest(version, comment)
        val response = managerService.updateVersionComment(fileId, body)
        if (!response.isSuccessful) throw HttpException(response)
    }

    fun deleteVersion(
        fileId: String,
        version: Int
    ): Flow<Result<Unit>> = apiFlow {
        val body = DeleteVersionRequest(fileId, arrayOf(version))
        val response = managerService.deleteVersion(body)
        if (!response.isSuccessful) throw HttpException(response)
    }

    fun getFillingStatus(fileId: String): Flow<NetworkResult<List<FormRole>>> {
        return flow { emit(managerService.getFillingStatus(fileId).response) }
            .flowOn(Dispatchers.IO)
            .asResult()
    }

    fun stopFilling(fileId: String): Flow<NetworkResult<Unit>> {
        return flow {
            val response = managerService.stopFilling(fileId, RequestStopFilling(fileId.toInt()))
            if (!response.isSuccessful) throw HttpException(response)
            emit(Unit)
        }
            .flowOn(Dispatchers.IO)
            .asResult()
    }

    private fun <T> apiFlow(apiCall: suspend () -> T): Flow<Result<T>> = flow {
        val result = kotlin.runCatching { apiCall() }
        emit(result)
    }.flowOn(Dispatchers.IO)
}