package app.documents.core.providers

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.os.Environment
import app.documents.core.network.common.contracts.ApiContract
import app.documents.core.network.common.utils.OneDriveUtils
import app.documents.core.network.manager.models.explorer.*
import app.documents.core.network.manager.models.request.RequestCreate
import app.documents.core.network.manager.models.response.ResponseOperation
import app.documents.core.network.storages.IStorageHelper
import app.documents.core.network.storages.onedrive.api.OneDriveProvider
import app.documents.core.network.storages.onedrive.api.OneDriveResponse
import app.documents.core.network.storages.onedrive.models.explorer.DriveItemCloudTree
import app.documents.core.network.storages.onedrive.models.explorer.DriveItemFolder
import app.documents.core.network.storages.onedrive.models.explorer.DriveItemParentReference
import app.documents.core.network.storages.onedrive.models.explorer.DriveItemValue
import app.documents.core.network.storages.onedrive.models.request.CopyItemRequest
import app.documents.core.network.storages.onedrive.models.request.CreateFolderRequest
import app.documents.core.network.storages.onedrive.models.request.ExternalLinkRequest
import app.documents.core.network.storages.onedrive.models.request.RenameRequest
import app.documents.core.network.storages.onedrive.models.response.ExternalLinkResponse
import io.reactivex.*
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import lib.toolkit.base.managers.utils.FileUtils
import lib.toolkit.base.managers.utils.FileUtils.createCacheFile
import lib.toolkit.base.managers.utils.FileUtils.createFile
import lib.toolkit.base.managers.utils.StringUtils
import lib.toolkit.base.managers.utils.StringUtils.getExtension
import lib.toolkit.base.managers.utils.StringUtils.getExtensionFromPath
import okhttp3.ResponseBody
import retrofit2.HttpException
import retrofit2.Response
import java.io.*
import java.util.*

class OneDriveFileProvider(
    private val context: Context,
    private val helper: IStorageHelper<OneDriveProvider>
) : BaseFileProvider {

    companion object {
        private const val PATH_TEMPLATES = "templates/"
    }

    override val fileOpenResultFlow: Flow<FileOpenResult>
        get() = flowOf()

    private val api: OneDriveProvider get() = helper.api

    override fun getFiles(id: String?, filter: Map<String, String>?): Observable<Explorer> {
        val searchValue = filter?.get(ApiContract.Parameters.ARG_FILTER_VALUE)?.trim().orEmpty()
        val mapWithSearchValue = OneDriveUtils.getSortBy(filter).plus(
            "\$filter" to "startswith(name, '$searchValue')"
        )
        return if (id.isNullOrEmpty()) {
            api.getFiles(mapWithSearchValue)
        } else {
            api.getChildren(id, mapWithSearchValue)
        }
            .toObservable()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .map { response ->
                when (response) {
                    is OneDriveResponse.Success -> getExplorer(response.response as DriveItemCloudTree)
                    is OneDriveResponse.Error -> throw response.error
                }
            }
    }

    private fun getExplorer(response: DriveItemCloudTree): Explorer {
        val explorer = Explorer()
        val files: MutableList<CloudFile> = mutableListOf()
        val folders: MutableList<CloudFolder> = mutableListOf()

        if (response.value.isNotEmpty()) {
            val parentFolder = CloudFolder().apply {
                this.id = response.value[0].parentReference.id
                this.title = response.value[0].parentReference.path.split("/").last()
                this.etag = response.value[0].eTag
                this.updated = StringUtils.getDate("yyyy-MM-dd'T'HH:mm:ss", response.value[0].lastModifiedDateTime)
            }

            for (item in response.value) {
                if (item.folder != null) {
                    val folder = CloudFolder()
                    folder.id = item.id
                    folder.title = item.name
                    folder.parentId = item.parentReference.id
                    folder.updated = StringUtils.getDate("yyyy-MM-dd'T'HH:mm:ss", item.lastModifiedDateTime)
                    folder.etag = item.eTag
                    folders.add(folder)
                } else if (item.file != null) {
                    val file = CloudFile()
                    file.id = item.id
                    file.title = item.name
                    file.folderId = item.parentReference.id
                    file.pureContentLength = item.size.toLong()
                    file.fileExst = getExtensionFromPath(file.title.lowercase(Locale.getDefault()))
                    file.created = StringUtils.getDate("yyyy-MM-dd'T'HH:mm:ss", item.createdDateTime)
                    file.updated = StringUtils.getDate("yyyy-MM-dd'T'HH:mm:ss", item.lastModifiedDateTime)
                    files.add(file)
                }
            }

            val current = Current()
            current.id = parentFolder.id
            current.filesCount = files.size.toString()
            current.foldersCount = files.size.toString()
            current.title = parentFolder.title

            explorer.current = current
            explorer.files = files
            explorer.folders = folders
        } else {
            explorer.current = Current().apply {
                id = runCatching {
                    response.context
                        .split("/")[6]
                        .split("'")[1]
                        .replace("%21", "!")
                }.getOrElse { "" }
                filesCount = "0"
                foldersCount = "0"
            }
        }
        return explorer
    }

    @SuppressLint("MissingPermission")
    override fun createFile(folderId: String, body: RequestCreate): Observable<CloudFile> {
        return Observable.fromCallable {
            api.createFile(
                folderId,
                body.title,
                mapOf(OneDriveUtils.KEY_CONFLICT_BEHAVIOR to OneDriveUtils.VAL_CONFLICT_BEHAVIOR_RENAME)
            ).blockingGet()
        }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .map { response ->
                when (response) {
                    is OneDriveResponse.Success -> {
                        val item = response.response as DriveItemValue
                        val path = PATH_TEMPLATES + FileUtils.getTemplates(
                            context = context,
                            locale = Locale.getDefault().language,
                            extension = getExtensionFromPath(item.name.lowercase())
                        )
                        val temp = FileUtils.createTempAssetsFile(
                            context = context,
                            from = path,
                            name = StringUtils.getNameWithoutExtension(item.name),
                            ext = getExtensionFromPath(item.name)
                        )
                        upload(folderId, mutableListOf(Uri.fromFile(temp))).subscribe()
                        return@map CloudFile().apply {
                            webUrl = Uri.fromFile(temp).toString()
                            pureContentLength = temp?.length() ?: 0
                            updated = Date()
                            id = item.id
                            title = item.name
                            fileExst = item.name.split(".")[1]
                        }
                    }
                    is OneDriveResponse.Error -> throw response.error
                }
            }
    }

    override fun createFolder(folderId: String, body: RequestCreate): Observable<CloudFolder> {
        val request = CreateFolderRequest(
            name = body.title,
            folder = DriveItemFolder(),
            conflictBehavior = OneDriveUtils.VAL_CONFLICT_BEHAVIOR_RENAME
        )
        return Observable.fromCallable { api.createFolder(folderId, request).blockingGet() }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .map { response ->
                when (response) {
                    is OneDriveResponse.Success -> CloudFolder().apply {
                        val item = response.response as DriveItemValue
                        id = item.id
                        title = item.name
                        updated = Date()
                    }
                    is OneDriveResponse.Error -> throw response.error
                }
            }
    }

    override fun rename(item: Item, newName: String, version: Int?): Observable<Item> {
        val correctName = if (item is CloudFile) {
            StringUtils.getEncodedString(newName) + item.fileExst
        } else {
            newName
        }
        return Observable.fromCallable {
            api.renameItem(item.id, RenameRequest(correctName)).blockingGet()
        }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .map { response ->
                if (response.isSuccessful) {
                    item.updated = Date()
                    item.title = newName
                    return@map item
                } else {
                    throw HttpException(response)
                }
            }
    }

    override fun delete(
        items: List<Item>,
        from: CloudFolder?
    ): Observable<List<Operation>> {
        return items.size.let {
            Observable.fromIterable(items).map { item -> api.deleteItem(item.id).blockingGet() }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map { response ->
                    if (response.isSuccessful && response.code() == 204) {
                        return@map response
                    } else {
                        throw HttpException(response)
                    }
                }.buffer(it)
                .map {
                    val operations: MutableList<Operation> = ArrayList()
                    val operation = Operation()
                    operation.progress = 100
                    operations.add(operation)
                    return@map operations
                }
        }!!
    }

    override fun transfer(
        items: List<Item>,
        to: CloudFolder,
        conflict: Int,
        isMove: Boolean,
        isOverwrite: Boolean
    ): Observable<List<Operation>> {
        return if (isMove) moveItem(items, to.id) else copyItem(items, to.id)
    }

    private fun copyItem(items: List<Item>?, to: String): Observable<List<Operation>> {
        return Observable.fromIterable(items)
            .flatMap { item ->
                val request = CopyItemRequest(
                    parentReference = DriveItemParentReference(
                        driveId = "",
                        driveType = "",
                        id = to,
                        name = item.title,
                        path = ""
                    ), name = item.title
                )
                Observable.fromCallable { api.copyItem(item.id, request).blockingGet() }
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
            }.map { responseBody ->
                if (responseBody.isSuccessful) {
                    val operation = Operation()
                    operation.progress = 100
                    return@map mutableListOf(operation)
                } else {
                    val httpException = HttpException(responseBody)
                    throw httpException
                }
            }
    }

    private fun moveItem(items: List<Item>?, to: String): Observable<List<Operation>> {
        return Observable.fromIterable(items)
            .flatMap { item ->
                val request = CopyItemRequest(
                    parentReference = DriveItemParentReference(
                        driveId = "",
                        driveType = "",
                        id = to,
                        name = item.title,
                        path = ""
                    ), name = item.title
                )
                Observable.fromCallable { api.moveItem(item.id, request).blockingGet() }
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
            }.map { responseBody ->
                if (responseBody.isSuccessful) {
                    val operation = Operation()
                    operation.progress = 100
                    return@map mutableListOf(operation)
                } else {
                    val httpException = HttpException(responseBody)
                    throw httpException
                }
            }
    }

    override fun fileInfo(item: Item?): Observable<CloudFile> {
        return Observable.create { emitter: ObservableEmitter<CloudFile> ->
            val outputFile = checkDirectory(item)

            outputFile?.let { file ->
                if (file.exists()) {
                    if (item is CloudFile) {
                        if (item.pureContentLength != outputFile.length()) {
                            download(emitter, item, outputFile)
                        } else {
                            emitter.onNext(setFile(item, outputFile))
                            emitter.onComplete()
                        }
                    }
                }
            }
        }
    }

    fun fileInfo(item: Item, isDownload: Boolean): Observable<CloudFile?> {
        return Observable.create { emitter: ObservableEmitter<CloudFile?> ->
            val outputFile = checkDirectory(item)

            outputFile?.let {
                if (it.exists()) {
                    if (item is CloudFile) {
                        if (isDownload) {
                            download(emitter, item, outputFile)
                        } else {
                            emitter.onNext(setFile(item, outputFile))
                            emitter.onComplete()
                        }
                    }
                }
            }
        }
    }

    override fun getStatusOperation(): ResponseOperation {
        val responseOperation = ResponseOperation()
        responseOperation.response = ArrayList()
        return responseOperation
    }

    @Throws(IOException::class)
    private fun download(emitter: Emitter<CloudFile?>, item: Item, outputFile: File) {
        val response = api.download((item as CloudFile).id).blockingGet()
        val responseBody = response.body()
        if (response.isSuccessful && responseBody != null) {
            try {
                responseBody.byteStream().use { inputStream ->
                    FileOutputStream(outputFile).use { outputStream ->
                        val buffer = ByteArray(4096)
                        var count: Int
                        while (inputStream.read(buffer).also { count = it } != -1) {
                            outputStream.write(buffer, 0, count)
                        }
                        outputStream.flush()
                        emitter.onNext(setFile(item, outputFile))
                        emitter.onComplete()
                    }
                }
            } catch (error: IOException) {
                emitter.onError(error)
            }
        }
    }

    override fun getDownloadResponse(cloudFile: CloudFile, token: String?): Single<Response<ResponseBody>> {
        return api.download(cloudFile.id)
    }

    fun upload(folderId: String, uris: List<Uri?>): Observable<Int> {
        return helper.upload(folderId, uris)
    }

    fun share(
        id: String,
        request: ExternalLinkRequest
    ): Observable<ExternalLinkResponse>? {
        return Observable.fromCallable { api.getExternalLink(id, request).blockingGet() }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .map { response ->
                when (response) {
                    is OneDriveResponse.Success -> response.response as ExternalLinkResponse
                    is OneDriveResponse.Error -> throw response.error
                }
            }
    }

    override fun terminate(): Observable<List<Operation>>? = null

    @SuppressLint("MissingPermission")
    private fun checkDirectory(item: Item?): File? {
        val file = item as CloudFile
        when (getExtension(file.fileExst)) {
            StringUtils.Extension.UNKNOWN, StringUtils.Extension.EBOOK, StringUtils.Extension.ARCH,
            StringUtils.Extension.VIDEO, StringUtils.Extension.HTML -> {
                val parent = File(Environment.getExternalStorageDirectory().absolutePath + "/OnlyOffice")
                return createFile(parent, file.title)
            }
            else -> {
                // Stub
            }
        }
        val local = File(Uri.parse(file.webUrl).path.toString())
        return if (local.exists()) {
            local
        } else {
            createCacheFile(context, item.title)
        }
    }

    private fun setFile(item: Item, outputFile: File): CloudFile {
        val originFile = item as CloudFile
        return CloudFile().apply {
            folderId = originFile.folderId
            title = originFile.title
            pureContentLength = outputFile.length()
            fileExst = originFile.fileExst
            viewUrl = originFile.id
            id = ""
            webUrl = Uri.fromFile(outputFile).toString()
        }
    }

}