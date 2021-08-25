package app.editors.manager.onedrive.managers.providers

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Environment
import app.documents.core.network.ApiContract
import app.editors.manager.app.App
import app.editors.manager.app.App.Companion.getApp
import app.editors.manager.app.getOneDriveServiceProvider
import app.editors.manager.managers.providers.BaseFileProvider
import app.editors.manager.onedrive.onedrive.OneDriveResponse
import app.editors.manager.mvp.models.base.Base
import app.editors.manager.mvp.models.explorer.*
import app.editors.manager.mvp.models.request.RequestCreate
import app.editors.manager.mvp.models.request.RequestExternal
import app.editors.manager.mvp.models.request.RequestFavorites
import app.editors.manager.mvp.models.response.ResponseExternal
import app.editors.manager.mvp.models.response.ResponseOperation
import app.editors.manager.onedrive.*
import app.editors.manager.onedrive.managers.utils.OneDriveUtils
import app.editors.manager.onedrive.mvp.models.explorer.DriveItemCloudTree
import app.editors.manager.onedrive.mvp.models.explorer.DriveItemFolder
import app.editors.manager.onedrive.mvp.models.explorer.DriveItemParentReference
import app.editors.manager.onedrive.mvp.models.explorer.DriveItemValue
import app.editors.manager.onedrive.mvp.models.request.*
import app.editors.manager.onedrive.mvp.models.response.ExternalLinkResponse
import io.reactivex.*
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import lib.toolkit.base.managers.utils.FileUtils.createCacheFile
import lib.toolkit.base.managers.utils.FileUtils.createFile
import lib.toolkit.base.managers.utils.StringUtils
import lib.toolkit.base.managers.utils.StringUtils.getExtension
import lib.toolkit.base.managers.utils.StringUtils.getExtensionFromPath
import okhttp3.ResponseBody
import retrofit2.HttpException
import java.io.*
import java.text.SimpleDateFormat
import java.util.*

class OneDriveFileProvider : BaseFileProvider {

    val context = App.getApp().applicationContext

    override fun getFiles(id: String?, filter: MutableMap<String, String>?): Observable<Explorer>? {
        return Observable.fromCallable {
            if (filter?.get(ApiContract.Parameters.ARG_FILTER_VALUE) == null || filter[ApiContract.Parameters.ARG_FILTER_VALUE]?.isEmpty() == true) {
                id?.let {
                    context.getOneDriveServiceProvider().getChildren(id, OneDriveUtils.getSortBy(filter))
                        .blockingGet()
                } ?: context.getOneDriveServiceProvider().getFiles(OneDriveUtils.getSortBy(filter)).blockingGet()
            } else {
                context.getOneDriveServiceProvider().filter(
                    filter[ApiContract.Parameters.ARG_FILTER_VALUE]!!,
                    OneDriveUtils.getSortBy(filter)
                ).blockingGet()
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

        if(response.value.isNotEmpty()) {


            val parentFolder = CloudFolder().apply {
                this.id = response.value[0].parentReference.id
                this.title = response.value[0].parentReference.path.split("/").last()
                this.etag = response.value[0].eTag
                this.updated =
                    SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse(response.value[0].lastModifiedDateTime)
            }

            for (item in response.value) {
                if (item.folder != null) {
                    val folder = CloudFolder()
                    folder.id = item.id
                    folder.title = item.name
                    folder.parentId = item.parentReference.id
                    folder.updated =
                        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse(item.lastModifiedDateTime)
                    folder.etag = item.eTag
                    folders.add(folder)
                } else if (item.file != null) {
                    val file = CloudFile()
                    file.id = item.id
                    file.title = item.name
                    file.folderId = item.parentReference.id
                    file.pureContentLength = item.size.toLong()
                    file.fileExst = getExtensionFromPath(file.title.toLowerCase())
                    file.created =
                        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse(item.createdDateTime)
                    file.updated =
                        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse(item.lastModifiedDateTime)
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
            explorer.files = emptyList()
            explorer.folders = emptyList()
        }
        return explorer
    }

    override fun createFile(folderId: String?, body: RequestCreate?): Observable<CloudFile> {
        return Observable.fromCallable { body?.title?.let {
            folderId?.let { it1 ->
                context.getOneDriveServiceProvider().createFile(
                    it1,
                    it, mapOf(OneDriveUtils.KEY_CONFLICT_BEHAVIOR to OneDriveUtils.VAL_CONFLICT_BEHAVIOR_RENAME)).blockingGet()
            }
        } }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .map {response ->
                when (response) {
                    is OneDriveResponse.Success -> {
                        val file = CloudFile()
                        file.id = (response.response as DriveItemValue).id
                        file.title = response.response.name
                        file.updated = Date()
                        file.fileExst = response.response.name.split(".")[1]
                        return@map file
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

    override fun createFolder(folderId: String?, body: RequestCreate?): Observable<CloudFolder>? {
        val request = CreateFolderRequest(
            name = body?.title!!,
            folder = DriveItemFolder(),
            conflictBehavior = OneDriveUtils.VAL_CONFLICT_BEHAVIOR_RENAME
        )
        return Observable.fromCallable { context.getOneDriveServiceProvider().createFolder(folderId!!, request).blockingGet() }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .map { response ->
                when (response) {
                    is OneDriveResponse.Success -> {
                        val folder = CloudFolder()
                        folder.id = (response.response as DriveItemValue).id
                        folder.title = response.response.name
                        folder.updated = Date()
                        return@map folder
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

    override fun rename(item: Item?, newName: String?, version: Int?): Observable<Item> {
        val correctName = if(item is CloudFile) {StringUtils.getEncodedString(newName) + item.fileExst} else { newName }
        val request = correctName?.let { RenameRequest(it) }
        return Observable.fromCallable{ item?.id?.let { request?.let { it1 ->
            context.getOneDriveServiceProvider().renameItem(it,
                it1
            ).blockingGet()
        } } }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .map { response ->
                if(response.isSuccessful) {
                    item?.updated = Date()
                    item?.title = newName
                    return@map item
                } else {
                    throw HttpException(response)
                }
            }
    }

    override fun delete(
        items: MutableList<Item>?,
        from: CloudFolder?
    ): Observable<List<Operation>> {
        return items?.size?.let {
            Observable.fromIterable(items).map { item -> context.getOneDriveServiceProvider().deleteItem(item.id).blockingGet() }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map { response ->
                    if(response.isSuccessful && response.code() == 204) {
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
        items: MutableList<Item>?,
        to: CloudFolder?,
        conflict: Int,
        isMove: Boolean,
        isOverwrite: Boolean
    ): Observable<MutableList<Operation>> {
        return if(isMove) {
            to?.id?.let { moveItem(items, it, isOverwrite) }!!
        } else {
            to?.id?.let { copyItem(items, it, isOverwrite) }!!
        }
    }

    private fun copyItem(items: MutableList<Item>?, to: String, isOverwrite: Boolean): Observable<MutableList<Operation>> {
        return Observable.fromIterable(items)
            .flatMap { item ->
                val request = CopyItemRequest(parentReference = DriveItemParentReference(driveId = "", driveType = "", id = to, name = item.title, path = ""), name = item.title)
                Observable.fromCallable { context.getOneDriveServiceProvider().copyItem(item.id, request).blockingGet() }
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
            }.map { responseBody ->
                if(responseBody.isSuccessful) {
                    val operation = Operation()
                    operation.progress = 100
                    return@map mutableListOf(operation)
                } else {
                    val httpException = HttpException(responseBody)
                    throw httpException
                }
            }
    }

    private fun moveItem(items: MutableList<Item>?, to: String, isOverwrite: Boolean): Observable<MutableList<Operation>> {
        return Observable.fromIterable(items)
            .flatMap { item ->
                val request = CopyItemRequest(parentReference = DriveItemParentReference(driveId = "", driveType = "", id = to, name = item.title, path = ""), name = item.title)
                Observable.fromCallable { context.getOneDriveServiceProvider().moveItem(item.id, request).blockingGet() }
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
            }.map { responseBody ->
                if(responseBody.isSuccessful) {
                    val operation = Operation()
                    operation.progress = 100
                    return@map mutableListOf(operation)
                } else {
                    val httpException = HttpException(responseBody)
                    throw httpException
                }
            }
    }

    override fun fileInfo(item: Item?): Observable<CloudFile?> {
        return Observable.create { emitter: ObservableEmitter<CloudFile?> ->
            val outputFile = item?.let { checkDirectory(it) }
            if (outputFile != null && outputFile.exists()) {
                if (item is CloudFile) {
                    if (item.pureContentLength != outputFile.length()) {
                        download(emitter, item, outputFile)
                    } else {
                        setFile(item, outputFile)?.let { emitter.onNext(it) }
                        emitter.onComplete()
                    }
                }
            }
        }
    }

    fun fileInfo(item: Item, isDownload: Boolean): Observable <CloudFile?> {
        return Observable.create(ObservableOnSubscribe { emitter: ObservableEmitter<CloudFile?> ->
            val outputFile = checkDirectory(item)
            if (outputFile != null && outputFile.exists()) {
                if (item is CloudFile) {
                    if (isDownload) {
                        download(emitter, item, outputFile)
                    } else {
                        emitter.onNext(setFile(item, outputFile)!!)
                        emitter.onComplete()
                    }
                }
            }
        })
    }

    override fun getStatusOperation(): ResponseOperation {
        val responseOperation = ResponseOperation()
        responseOperation.response = ArrayList()
        return responseOperation
    }

    override fun download(items: MutableList<Item>?): Observable<Int> {
        TODO("Not yet implemented")
    }

    override fun upload(folderId: String?, uris: MutableList<Uri>?): Observable<Int> {
        TODO("Not yet implemented")
    }

    @Throws(IOException::class)
    private fun download(emitter: Emitter<CloudFile?>, item: Item, outputFile: File) {
        val result = context.getOneDriveServiceProvider().download((item as CloudFile).id).blockingGet()
        if(result is OneDriveResponse.Success) {
            try {
                (result.response as ResponseBody).byteStream().use { inputStream ->
                    FileOutputStream(outputFile).use { outputStream ->
                        val buffer = ByteArray(4096)
                        var count: Int
                        while (inputStream.read(buffer).also { count = it } != -1) {
                            outputStream.write(buffer, 0, count)
                        }
                        outputStream.flush()
                        emitter.onNext(setFile(item, outputFile)!!)
                        emitter.onComplete()
                    }
                }
            } catch (error: IOException) {
                emitter.onError(error)
            }
        } else if(result is OneDriveResponse.Error) {
            throw result.error
        }
    }

    override fun share(
        id: String?,
        requestExternal: RequestExternal?
    ): Observable<ResponseExternal> {
        TODO("Not yet implemented")
    }

    fun share(id: String, request: ExternalLinkRequest): Observable<ExternalLinkResponse>? {
        return Observable.fromCallable { context.getOneDriveServiceProvider().getExternalLink(id, request).blockingGet() }
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

    override fun terminate(): Observable<MutableList<Operation>> {
        TODO("Not yet implemented")
    }

    override fun addToFavorites(requestFavorites: RequestFavorites?): Observable<Base> {
        TODO("Not yet implemented")
    }

    override fun deleteFromFavorites(requestFavorites: RequestFavorites?): Observable<Base> {
        TODO("Not yet implemented")
    }

    @SuppressLint("MissingPermission")
    private fun checkDirectory(item: Item): File? {
        val file = item as CloudFile
        when (getExtension(file.fileExst)) {
            StringUtils.Extension.UNKNOWN, StringUtils.Extension.EBOOK, StringUtils.Extension.ARCH, StringUtils.Extension.VIDEO, StringUtils.Extension.HTML -> {
                val parent =
                    File(Environment.getExternalStorageDirectory().absolutePath + "/OnlyOffice")
                return createFile(parent, file.title)
            }
        }
        val local = File(Uri.parse(file.webUrl).path)
        return if (local.exists()) {
            local
        } else {
            createCacheFile(getApp(), item.getTitle())
        }
    }

    private fun setFile(item: Item, outputFile: File): CloudFile? {
        val originFile = item as CloudFile
        val file = CloudFile()
        file.folderId = originFile.folderId
        file.title = originFile.title
        file.pureContentLength = outputFile.length()
        file.fileExst = originFile.fileExst
        file.viewUrl = originFile.id
        file.id = ""
        file.webUrl = Uri.fromFile(outputFile).toString()
        return file
    }

}