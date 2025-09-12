package app.documents.core.providers

import android.annotation.SuppressLint
import android.content.Context
import app.documents.core.account.AccountRepository
import app.documents.core.manager.ManagerRepository
import app.documents.core.model.cloud.Access
import app.documents.core.network.common.NetworkClient
import app.documents.core.network.common.NetworkResult
import app.documents.core.network.common.asResult
import app.documents.core.network.common.contracts.ApiContract
import app.documents.core.network.common.interceptors.HeaderType
import app.documents.core.network.common.models.BaseResponse
import app.documents.core.network.manager.ManagerService
import app.documents.core.network.manager.models.base.FillResult
import app.documents.core.network.manager.models.explorer.CloudFile
import app.documents.core.network.manager.models.explorer.CloudFolder
import app.documents.core.network.manager.models.explorer.Explorer
import app.documents.core.network.manager.models.explorer.FormRole
import app.documents.core.network.manager.models.explorer.Item
import app.documents.core.network.manager.models.explorer.Operation
import app.documents.core.network.manager.models.request.RequestBatchBase
import app.documents.core.network.manager.models.request.RequestBatchOperation
import app.documents.core.network.manager.models.request.RequestCreate
import app.documents.core.network.manager.models.request.RequestCreateThumbnails
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
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
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


data class OpenDocumentResult(
    val info: String? = null,
    val isPdf: Boolean = false,
    val isForm: Boolean = false
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
        fun isTemplatesRoot(id: String?): Boolean
    }

    var roomCallback: RoomCallback? = null

    override fun getFiles(id: String?, filter: Map<String, String>?): Observable<Explorer> {
        return when {
            roomCallback?.isTemplatesRoot(id) == true -> getRoomTemplates(filter.orEmpty())
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

    fun getThumbnails(
        explorer: Explorer?,
        id: String,
        filter: Map<String, String>,
        initDelay: Boolean = true,
        delayMs: Long = 2000,
        maxAttempts: Int = 5
    ): Flow<CloudFile> = flow {

        val missingStatuses = listOf(
            ApiContract.ThumbnailStatus.WAITING,
            ApiContract.ThumbnailStatus.CREATING
        )
        val missingThumbnailsIds = explorer?.files
            ?.filter { it.thumbnailStatus in missingStatuses }
            ?.map { it.id }.orEmpty().toMutableList()

        if (missingThumbnailsIds.isEmpty()) return@flow

        val response = managerService.createThumbnails(
            RequestCreateThumbnails(fileIds = missingThumbnailsIds)
        )
        if (!response.isSuccessful) return@flow

        repeat(maxAttempts) { index ->
            if (initDelay && index == 0 || index > 0) delay(delayMs)
            val fileResponse = managerService.getItemByIdFlow(id, filter)
            val body = fileResponse.body()?.response ?: return@flow
            if (!fileResponse.isSuccessful) return@flow

            val readyFiles = body.files.filter { file ->
                file.id in missingThumbnailsIds && file.thumbnailStatus !in missingStatuses
            }
            readyFiles.forEach { file ->
                missingThumbnailsIds.remove(file.id)
                emit(file.apply { thumbnailStatus = ApiContract.ThumbnailStatus.CREATED })
            }

            if (index == maxAttempts - 1) {
                body.files.filter { it.id in missingThumbnailsIds }.forEach { file ->
                    emit(file.apply { thumbnailStatus = ApiContract.ThumbnailStatus.NOT_REQUIRED })
                }
            }

            if (missingThumbnailsIds.isEmpty()) return@flow
        }
    }.flowOn(Dispatchers.IO)

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
        return if (item is CloudFolder) {
            folderRename(item.id, newName)
        } else {
            fileRename(item.id, newName)
        }
    }

    private fun fileRename(id: String, newName: String): Observable<Item> {
        val requestRenameFile = RequestRenameFile()
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

    fun openDeeplink(
        portal: String,
        token: String,
        login: String,
        id: String,
        title: String,
        extension: String,
    ): Flow<NetworkResult<FileOpenResult>> {
        return flow {
            emit(FileOpenResult.Loading())

            val accountOnline = accountRepository.getOnlineAccount()
            val api = managerService.takeIf {
                accountOnline != null &&
                accountOnline.portal.urlWithScheme == portal &&
                accountOnline.login == login
            } ?: NetworkClient.getRetrofit<ManagerService>(
                url = portal,
                token = token,
                context = context,
                headerType = HeaderType.REQUEST_TOKEN
            )

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

            val result = fileJson
                .put("url", docService)
                .put("fileId", id)
                .put("canShareable", false)

            // opening not form pdf locally
            if (fileJson.getString("documentType") == "pdf" && (!fileJson
                    .getJSONObject("document")
                    .getBoolean("isForm") || !fileJson
                    .getJSONObject("document")
                    .getJSONObject("permissions")
                    .getBoolean("fillForms"))
            ) {
                val cloudFile = CloudFile().apply { this.id = id; this.title = title }
                emit(
                    FileOpenResult.OpenLocally(
                        file = suspendGetCachedFile(
                            context = context,
                            cloudFile = cloudFile,
                            token = accountRepository.getOnlineToken() ?: token
                        ),
                        fileId = cloudFile.id,
                        editType = EditType.View(),
                        access = Access.None
                    )
                )
                return@flow
            }

            emit(
                FileOpenResult.OpenDocumentServer(
                    cloudFile = CloudFile().apply {
                        this.id = id
                        this.title = title
                        this.fileExst = extension
                    },
                    info = result.toString(),
                    editType = if (fileJson.getJSONObject("editorConfig")
                            .getString("mode") == "view"
                    ) {
                        EditType.View()
                    } else if (fileJson.getString("documentType") == "pdf") {
                        if (fileJson.getJSONObject("document").getBoolean("isForm")) {
                            EditType.Fill()
                        } else {
                            EditType.View()
                        }
                    } else {
                        EditType.Edit()
                    }
                )
            )
        }
            .flowOn(Dispatchers.IO)
            .asResult()
    }

    @OptIn(InternalCoroutinesApi::class)
    override fun openFile(
        cloudFile: CloudFile,
        editType: EditType,
        canBeShared: Boolean
    ): Flow<NetworkResult<FileOpenResult>> {
        return flow {
            emit(NetworkResult.Success(FileOpenResult.Loading()))
            val cloudFile = suspendCancellableCoroutine<CloudFile> { cont ->
                fileInfo(cloudFile)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnNext { cloudFile -> cont.resume(cloudFile) }
                    .doOnError { error -> cont.tryResumeWithException(error) }
                    .subscribe()
            }
            openFile(
                cloudFile = cloudFile,
                editType = if (cloudFile.isForm && editType != EditType.StartFilling()) EditType.Fill() else editType,
                canBeShared = canBeShared,
                access = cloudFile.access
            ).collect { emit(it) }
        }.catch { NetworkResult.Error(it) }
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
                        val document = checkPdfForm(
                            cloudFile = cloudFile,
                            token = token,
                            canShareable = canBeShared,
                            editType = editType
                        )

                        if (editType is EditType.StartFilling && document.info != null) {
                            emit(
                                FileOpenResult.OpenDocumentServer(
                                    cloudFile = cloudFile,
                                    info = document.info,
                                    editType = editType
                                )
                            )
                            return@flow
                        }

                        val isNotFillableForm = document.isForm &&
                                if (cloudFile.security != null) {
                                    cloudFile.security?.fillForms != true
                                } else {
                                    false
                                }

                        if (document.isPdf || document.info == null || isNotFillableForm) {
                            emit(
                                FileOpenResult.OpenLocally(
                                    file = suspendGetCachedFile(context, cloudFile, token),
                                    fileId = cloudFile.id,
                                    editType = if (isNotFillableForm) EditType.View() else editType,
                                    access = access
                                )
                            )
                        } else {
                            emit(
                                FileOpenResult.OpenDocumentServer(
                                    cloudFile = cloudFile,
                                    info = document.info,
                                    editType = editType
                                )
                            )
                        }
                    } else {
                        emit(
                            FileOpenResult.OpenLocally(
                                file = suspendGetCachedFile(context, cloudFile, token),
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
    private suspend fun checkPdfForm(
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
                OpenDocumentResult(info = info, isForm = true)
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
            edit = editType is EditType.Edit || editType is EditType.StartFilling,
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

    fun getRoomTemplates(filter: Map<String, String> = mapOf()): Observable<Explorer> {
        val options = filter.plus("searchArea" to "Templates")
        return roomService.getAllRooms(options)
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

    fun getVersionHistory(fileId: String): Flow<NetworkResult<List<CloudFile>>> = apiFlow {
        val response = managerService.getVersionHistory(fileId)
        val files = response.body()?.response
        if (response.isSuccessful && files != null) files
        else throw HttpException(response)
    }

    fun restoreVersion(fileId: String, version: Int): Flow<NetworkResult<Unit>> = apiFlow {
        val response = managerService.restoreVersion(fileId, version)
        if (!response.isSuccessful) throw HttpException(response)
    }

    fun editVersionComment(
        fileId: String,
        version: Int,
        comment: String
    ): Flow<NetworkResult<Unit>> = apiFlow {
        val body = EditCommentRequest(version, comment)
        val response = managerService.updateVersionComment(fileId, body)
        if (!response.isSuccessful) throw HttpException(response)
    }

    fun deleteVersion(
        fileId: String,
        version: Int
    ): Flow<NetworkResult<Unit>> = apiFlow {
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

    suspend fun getFillResult(sessionId: String, portal: String?, token: String?): FillResult {
        val api = managerService.takeIf {
            token.isNullOrEmpty()
        } ?: NetworkClient.getRetrofit<ManagerService>(
            url = portal.orEmpty(),
            token = token.orEmpty(),
            context = context,
            headerType = HeaderType.REQUEST_TOKEN
        )

        return api.getFillResult(sessionId).response
    }

    private fun <T> apiFlow(apiCall: suspend () -> T): Flow<NetworkResult<T>> = flow {
        val result = apiCall()
        emit(result)
    }
        .flowOn(Dispatchers.IO)
        .asResult()
}