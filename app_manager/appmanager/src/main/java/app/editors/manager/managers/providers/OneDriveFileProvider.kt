package app.editors.manager.managers.providers

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Environment
import androidx.work.Data
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import app.documents.core.network.common.contracts.ApiContract
import app.documents.core.network.manager.models.explorer.*
import app.documents.core.network.manager.models.request.RequestCreate
import app.documents.core.network.manager.models.request.RequestExternal
import app.documents.core.network.manager.models.response.ResponseExternal
import app.documents.core.network.manager.models.response.ResponseOperation
import app.documents.core.network.storages.onedrive.models.request.RenameRequest
import app.documents.core.network.storages.onedrive.api.OneDriveResponse
import app.documents.core.network.storages.onedrive.models.explorer.DriveItemCloudTree
import app.documents.core.network.storages.onedrive.models.explorer.DriveItemParentReference
import app.documents.core.network.storages.onedrive.models.explorer.DriveItemValue
import app.documents.core.network.storages.onedrive.models.request.CopyItemRequest
import app.documents.core.providers.BaseFileProvider
import app.documents.core.network.common.utils.OneDriveUtils
import app.documents.core.network.storages.onedrive.api.OneDriveProvider
import app.documents.core.network.storages.onedrive.models.explorer.DriveItemFolder
import app.documents.core.network.storages.onedrive.models.request.CreateFolderRequest
import app.documents.core.network.storages.onedrive.models.request.ExternalLinkRequest
import app.documents.core.network.storages.onedrive.models.response.ExternalLinkResponse
import app.editors.manager.app.App
import app.editors.manager.app.oneDriveProvider
import app.editors.manager.managers.works.BaseStorageUploadWork
import app.editors.manager.managers.works.onedrive.UploadWork
import app.editors.manager.ui.fragments.base.BaseStorageDocsFragment
import io.reactivex.*
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import lib.toolkit.base.managers.utils.FileUtils
import lib.toolkit.base.managers.utils.FileUtils.createCacheFile
import lib.toolkit.base.managers.utils.FileUtils.createFile
import lib.toolkit.base.managers.utils.StringUtils
import lib.toolkit.base.managers.utils.StringUtils.getExtension
import lib.toolkit.base.managers.utils.StringUtils.getExtensionFromPath
import okhttp3.ResponseBody
import retrofit2.HttpException
import java.io.*
import java.util.*

class OneDriveFileProvider : BaseFileProvider {


    companion object {
        private const val PATH_TEMPLATES = "templates/"
    }

    private var api: OneDriveProvider = App.getApp().oneDriveProvider

    private val workManager = WorkManager.getInstance(App.getApp().applicationContext)


    fun refreshInstance() {
        App.getApp().refreshOneDriveInstance()
    }

    override fun getFiles(id: String?, filter: Map<String, String>?): Observable<Explorer> {
        return Observable.fromCallable {
            if (filter?.get(ApiContract.Parameters.ARG_FILTER_VALUE) == null || filter[ApiContract.Parameters.ARG_FILTER_VALUE]?.isEmpty() == true) {
                if (id?.isEmpty() == true) {
                    api.getFiles(OneDriveUtils.getSortBy(filter)).blockingGet()
                } else {
                    id?.let {
                        api.getChildren(it, OneDriveUtils.getSortBy(filter))
                            .blockingGet()
                    }
                }
            } else {
                api.filter(filter[ApiContract.Parameters.ARG_FILTER_VALUE]!!, OneDriveUtils.getSortBy(filter))
                    .blockingGet()
            }
        }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .map { response ->
                when (response) {
                    is OneDriveResponse.Success -> {
                        return@map getExplorer(response.response as DriveItemCloudTree)
                    }
                    is OneDriveResponse.Error -> {
                        throw response.error
                    }
                    else -> return@map null
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
            val current = Current()

            val context = response.context.split("/")

            current.id = context[6].split("'")[1].replace("%21", "!")
            current.filesCount = 0.toString()
            current.foldersCount = 0.toString()
            explorer.current = current
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
                        val path = FileUtils.getTemplates(
                            context = App.getApp(),
                            locale = App.getLocale(),
                            extension = getExtensionFromPath(PATH_TEMPLATES + item.name.lowercase())
                        )
                        val temp = FileUtils.createTempAssetsFile(
                                App.getApp(),
                                path.orEmpty(),
                                StringUtils.getNameWithoutExtension(item.name),
                                getExtensionFromPath(item.name)
                            )
                        upload(folderId, mutableListOf(Uri.fromFile(temp)))?.subscribe()
                        return@map CloudFile().apply {
                            webUrl = Uri.fromFile(temp).toString()
                            pureContentLength = temp?.length() ?: 0
                            updated = Date()
                            id = item.id
                            title = item.name
                            fileExst = item.name.split(".")[1]
                        }
                    }
                    is OneDriveResponse.Error -> {
                        throw response.error
                    }
                    else -> {
                        return@map null
                    }
                }
            }
    }

    override fun search(query: String?): Observable<String>? = null

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
                    is OneDriveResponse.Success -> {
                        CloudFolder().apply {
                            val item = response.response as DriveItemValue
                            id = item.id
                            title = item.name
                            updated = Date()
                        }
                    }
                    is OneDriveResponse.Error -> {
                        throw response.error
                    }
                    else -> {
                        return@map null
                    }
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
        to: CloudFolder?,
        conflict: Int,
        isMove: Boolean,
        isOverwrite: Boolean
    ): Observable<List<Operation>>? {
        return if (isMove) {
            to?.id?.let { moveItem(items, it, isOverwrite) }
        } else {
            to?.id?.let { copyItem(items, it, isOverwrite) }
        }
    }

    private fun copyItem(items: List<Item>?, to: String, isOverwrite: Boolean): Observable<List<Operation>> {
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

    private fun moveItem(items: List<Item>?, to: String, isOverwrite: Boolean): Observable<List<Operation>> {
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

            outputFile?.let {
                if (it.exists()) {
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

    override fun download(items: List<Item>): Observable<Int>? = null

    override fun upload(folderId: String, uris: List<Uri?>): Observable<Int>? {
        return Observable.fromIterable(uris)
            .flatMap {
                val data = Data.Builder()
                    .putString(BaseStorageUploadWork.TAG_FOLDER_ID, folderId)
                    .putString(BaseStorageUploadWork.TAG_UPLOAD_FILES, it.toString())
                    .putString(BaseStorageUploadWork.KEY_TAG, BaseStorageDocsFragment.KEY_UPDATE)
                    .build()

                val request = OneTimeWorkRequest.Builder(UploadWork::class.java)
                    .setInputData(data)
                    .build()

                workManager.enqueue(request)
                return@flatMap Observable.just(1)
            }
    }

    @Throws(IOException::class)
    private fun download(emitter: Emitter<CloudFile?>, item: Item, outputFile: File) {
        val result = api.download((item as CloudFile).id).blockingGet()
        if (result is OneDriveResponse.Success) {
            try {
                (result.response as ResponseBody).byteStream().use { inputStream ->
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
        } else if (result is OneDriveResponse.Error) {
            throw result.error
        }
    }

    override fun share(
        id: String,
        requestExternal: RequestExternal
    ): Observable<ResponseExternal>? = null

    fun share(
        id: String,
        request: ExternalLinkRequest
    ): Observable<ExternalLinkResponse>? {
        return Observable.fromCallable { api.getExternalLink(id, request).blockingGet() }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .map { response ->
                when (response) {
                    is OneDriveResponse.Success -> {
                        return@map (response.response as ExternalLinkResponse)
                    }
                    is OneDriveResponse.Error -> {
                        throw response.error
                    }
                    else -> {
                        return@map null
                    }
                }
            }
    }

    override fun terminate(): Observable<List<Operation>>? = null

    @SuppressLint("MissingPermission")
    private fun checkDirectory(item: Item?): File? {
        val file = item as CloudFile
        when (getExtension(file.fileExst)) {
            StringUtils.Extension.UNKNOWN, StringUtils.Extension.EBOOK, StringUtils.Extension.ARCH, StringUtils.Extension.VIDEO, StringUtils.Extension.HTML -> {
                val parent =
                    File(Environment.getExternalStorageDirectory().absolutePath + "/OnlyOffice")
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
            createCacheFile(App.getApp(), item.title)
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