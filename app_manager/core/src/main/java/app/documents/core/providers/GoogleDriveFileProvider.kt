package app.documents.core.providers

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.os.Environment
import app.documents.core.network.common.contracts.ApiContract
import app.documents.core.network.common.utils.GoogleDriveUtils
import app.documents.core.network.manager.models.explorer.*
import app.documents.core.network.manager.models.request.RequestCreate
import app.documents.core.network.manager.models.request.RequestExternal
import app.documents.core.network.manager.models.response.ResponseExternal
import app.documents.core.network.manager.models.response.ResponseOperation
import app.documents.core.network.storages.IStorageHelper
import app.documents.core.network.storages.googledrive.api.GoogleDriveProvider
import app.documents.core.network.storages.googledrive.api.GoogleDriveResponse
import app.documents.core.network.storages.googledrive.models.GoogleDriveCloudFile
import app.documents.core.network.storages.googledrive.models.GoogleDriveFile
import app.documents.core.network.storages.googledrive.models.request.CreateItemRequest
import app.documents.core.network.storages.googledrive.models.request.RenameRequest
import app.documents.core.network.storages.googledrive.models.request.ShareRequest
import app.documents.core.network.storages.googledrive.models.resonse.GoogleDriveExplorerResponse
import io.reactivex.Emitter
import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import lib.toolkit.base.managers.tools.LocalContentTools
import lib.toolkit.base.managers.utils.FileUtils
import lib.toolkit.base.managers.utils.StringUtils
import okhttp3.ResponseBody
import retrofit2.HttpException
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.*

class GoogleDriveFileProvider(
    private val context: Context,
    private val helper: IStorageHelper<GoogleDriveProvider>
) : BaseFileProvider {

    enum class GoogleMimeType(val value: String) {
        Docs("application/vnd.google-apps.document"),
        Cells("application/vnd.google-apps.spreadsheet"),
        Slides("application/vnd.google-apps.presentation");

        companion object {
            fun isGoogleMimeType(mimeType: String): Boolean {
                return values().map { it.value }
                    .contains(mimeType)
            }
        }
    }

    companion object {
        private const val PATH_TEMPLATES = "templates/"

        private const val docx = ".${LocalContentTools.DOCX_EXTENSION}"
        private const val xlsx = ".${LocalContentTools.XLSX_EXTENSION}"
        private const val pptx = ".${LocalContentTools.PPTX_EXTENSION}"

        private val googleMimeTypes = GoogleMimeType.values().map { it.value }

        private fun checkItemExtension(name: String, mimeType: String): String {
            if (!name.contains(Regex("$docx|$xlsx|$pptx"))) {
                return name + when (mimeType) {
                    GoogleMimeType.Docs.value -> docx
                    GoogleMimeType.Cells.value -> xlsx
                    GoogleMimeType.Slides.value -> pptx
                    else -> return name
                }
            } else return name
        }

        fun getCommonMimeType(googleMimeType: String): String {
            return when (googleMimeType) {
                GoogleMimeType.Docs.value -> LocalContentTools.MIME_TYPE_DOCX
                GoogleMimeType.Cells.value -> LocalContentTools.MIME_TYPE_XLSX
                GoogleMimeType.Slides.value -> LocalContentTools.MIME_TYPE_XLSX
                else -> ""
            }
        }

    }

    private val api: GoogleDriveProvider get() = helper.api

    override fun getFiles(id: String?, filter: Map<String, String>?): Observable<Explorer> {
        var queryString = "\"$id\" in parents and trashed = false"

        if (filter?.get(ApiContract.Parameters.ARG_FILTER_VALUE) != null && filter[ApiContract.Parameters.ARG_FILTER_VALUE]?.isNotEmpty() == true) {
            queryString = "name contains \'${filter[ApiContract.Parameters.ARG_FILTER_VALUE]!!}\'"
        }

        val map = mapOf(
            GoogleDriveUtils.GOOGLE_DRIVE_QUERY to queryString,
            GoogleDriveUtils.GOOGLE_DRIVE_FIELDS to GoogleDriveUtils.GOOGLE_DRIVE_FIELDS_VALUES,
            GoogleDriveUtils.GOOGLE_DRIVE_SORT to GoogleDriveUtils.getSortBy(filter),
            GoogleDriveUtils.GOOGLE_DRIVE_NEXT_PAGE_TOKEN to filter?.get(GoogleDriveUtils.GOOGLE_DRIVE_NEXT_PAGE_TOKEN)
                .orEmpty()
        )
        val intMap = mapOf(
            GoogleDriveUtils.GOOGLE_DRIVE_PAGE_SIZE to GoogleDriveUtils.DEFAULT_PAGE_SIZE
        )
        return Observable.fromCallable { api.getFiles(map, intMap).blockingGet() }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .map { googleDriveResponse ->
                when (googleDriveResponse) {
                    is GoogleDriveResponse.Success -> {
                        val response = googleDriveResponse.response as GoogleDriveExplorerResponse
                        return@map getExplorer(response.files, response.nextPageToken, id!!)
                    }
                    is GoogleDriveResponse.Error -> {
                        throw googleDriveResponse.error
                    }
                }
            }

    }

    private fun getExplorer(items: List<GoogleDriveFile>, cursor: String, id: String): Explorer {
        val explorer = Explorer()
        val files: MutableList<CloudFile> = mutableListOf()
        val folders: MutableList<CloudFolder> = mutableListOf()

        if (items.isNotEmpty()) {
            val parentFolder = CloudFolder().apply {
                this.id = items[0].parents[0]
            }

            for (item in items) {
                if (item.mimeType.contains("folder")) {
                    val folder = GoogleDriveFolder()
                    folder.id = item.id
                    folder.title = item.name
                    folder.webUrl = item.webViewLink
                    folder.parentId = item.parents[0]
                    folder.updated = StringUtils.getDate("yyyy-MM-dd'T'HH:mm:ss", item.modifiedTime)
                    folders.add(folder)
                } else {
                    val file = GoogleDriveCloudFile()
                    file.id = item.id
                    file.title = checkItemExtension(item.name, item.mimeType)
                    file.mimeType = item.mimeType
                    file.folderId = item.parents[0]
                    file.pureContentLength = if (item.size.isNotEmpty()) item.size.toLong() else 0
                    file.webUrl = item.webViewLink
                    file.fileExst = StringUtils.getExtensionFromPath(file.title.lowercase())
                    file.created = StringUtils.getDate("yyyy-MM-dd'T'HH:mm:ss", item.createdTime)
                    file.updated = StringUtils.getDate("yyyy-MM-dd'T'HH:mm:ss", item.modifiedTime)
                    files.add(file)
                }
            }

            val current = Current()
            current.id = parentFolder.id
            current.filesCount = files.size.toString()
            current.foldersCount = files.size.toString()
            current.title = parentFolder.title
            current.parentId = cursor

            explorer.current = current
            explorer.files = files
            explorer.folders = folders
        } else {
            val current = Current()
            current.id = id
            current.filesCount = 0.toString()
            current.foldersCount = 0.toString()
            explorer.current = current
        }
        return explorer
    }

    override fun createFile(folderId: String, body: RequestCreate): Observable<CloudFile> {
        return Observable.just(1)
            .map {
                val title = body.title
                val path = PATH_TEMPLATES + FileUtils.getTemplates(
                    context = context,
                    locale = Locale.getDefault().language,
                    extension = StringUtils.getExtensionFromPath(body.title.lowercase())
                )
                val temp = FileUtils.createTempAssetsFile(
                    context = context,
                    from = path,
                    name = StringUtils.getNameWithoutExtension(title),
                    ext = StringUtils.getExtensionFromPath(title)
                )
                upload(folderId, mutableListOf(Uri.fromFile(temp))).subscribe()
                val file = CloudFile()
                file.webUrl = Uri.fromFile(temp).toString()
                file.pureContentLength = temp?.length() ?: 0
                file.updated = Date()
                file.title = body.title
                file.fileExst = body.title.split(".")[1]
                return@map file
            }
    }

    override fun search(query: String?): Observable<String>? {
        TODO("Not yet implemented")
    }

    override fun createFolder(folderId: String, body: RequestCreate): Observable<CloudFolder> {
        val request = CreateItemRequest(
            name = body.title,
            mimeType = GoogleDriveUtils.FOLDER_MIMETYPE,
            parents = listOf(folderId)
        )
        return Observable.fromCallable { api.create(request).blockingGet() }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .map { response ->
                when (response) {
                    is GoogleDriveResponse.Success -> {
                        CloudFolder().apply {
                            val file = response.response as GoogleDriveFile
                            id = file.id
                            title = file.name
                            updated = Date()
                        }
                    }
                    is GoogleDriveResponse.Error -> {
                        throw response.error
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
        val request = RenameRequest(
            name = correctName
        )
        return Observable.fromCallable { api.rename(item.id, request).blockingGet() }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .map { response ->
                when (response) {
                    is GoogleDriveResponse.Success -> {
                        item.updated = Date()
                        item.title = newName
                        return@map item
                    }
                    is GoogleDriveResponse.Error -> {
                        throw response.error
                    }
                }
            }
    }

    override fun delete(items: List<Item>, from: CloudFolder?): Observable<List<Operation>> {
        return items.size.let {
            Observable.fromIterable(items).map { item -> api.delete(item.id).blockingGet() }
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
        }
    }

    override fun transfer(
        items: List<Item>,
        to: CloudFolder,
        conflict: Int,
        isMove: Boolean,
        isOverwrite: Boolean
    ): Observable<List<Operation>>? {
        return Observable.fromIterable(items)
            .flatMap { item ->
                val map = mapOf(
                    GoogleDriveUtils.MOVE_ADD_PARENTS to to.id,
                    GoogleDriveUtils.MOVE_REMOVE_PARENTS to when (item) {
                        is CloudFile -> item.folderId
                        is CloudFolder -> item.parentId
                        else -> null
                    }
                )
                Observable.fromCallable { api.move(item.id, map).blockingGet() }
            }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .map { response ->
                when (response) {
                    is GoogleDriveResponse.Success -> {
                        return@map response.response
                    }
                    is GoogleDriveResponse.Error -> {
                        throw response.error
                    }
                }
            }.buffer(items.size)
            .map {
                val operation = Operation()
                operation.progress = 100
                return@map mutableListOf(operation)
            }
    }

    fun copy(items: List<Item>): Observable<Boolean> {
        return items.size.let {
            Observable.fromIterable(items)
                .map { item ->
                    api.copy(item.id).blockingGet()
                }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map { response ->
                    when (response) {
                        is GoogleDriveResponse.Success -> {
                            true
                        }
                        is GoogleDriveResponse.Error -> {
                            throw response.error
                        }
                    }
                }
        }
    }

    override fun fileInfo(item: Item?): Observable<CloudFile> {
        return Observable.create { emitter: ObservableEmitter<CloudFile> ->
            if (item != null && item is GoogleDriveCloudFile) {
                checkDirectory(item)?.let { outputFile ->
                    if (outputFile.exists()) {
                        if (googleMimeTypes.contains(item.mimeType)) {
                            export(emitter, item, outputFile)
                        } else if (item.pureContentLength != outputFile.length()) {
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

    private fun export(emitter: Emitter<CloudFile?>, item: GoogleDriveCloudFile, outputFile: File) {
        val result = api.export(item.id, getCommonMimeType(item.mimeType)).blockingGet()
        result.body()?.let { response ->
            try {
                emitter.onNext(writeToFile(item, response, outputFile))
                emitter.onComplete()
            } catch (error: IOException) {
                emitter.onError(error)
            }
        }
    }

    private fun download(emitter: Emitter<CloudFile?>, item: Item, outputFile: File) {
        val result = api.download(item.id).blockingGet()
        result.body()?.let { response ->
            try {
                emitter.onNext(writeToFile(item, response, outputFile))
                emitter.onComplete()
            } catch (error: IOException) {
                emitter.onError(error)
            }
        }
    }

    private fun writeToFile(item: Item, response: ResponseBody, outputFile: File): CloudFile {
        response.byteStream().use { inputStream ->
            FileOutputStream(outputFile).use { outputStream ->
                val buffer = ByteArray(4096)
                var count: Int
                while (inputStream.read(buffer).also { count = it } != -1) {
                    outputStream.write(buffer, 0, count)
                }
                outputStream.flush()
                return setFile(item, outputFile)
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun checkDirectory(item: Item): File? {
        val file = item as CloudFile
        when (StringUtils.getExtension(file.fileExst)) {
            StringUtils.Extension.UNKNOWN, StringUtils.Extension.EBOOK, StringUtils.Extension.ARCH, StringUtils.Extension.VIDEO, StringUtils.Extension.HTML -> {
                val parent =
                    File(Environment.getExternalStorageDirectory().absolutePath + "/OnlyOffice")
                return FileUtils.createFile(parent, file.title)
            }
            else -> {
                // Stub
            }
        }
        val local = File(Uri.parse(file.webUrl).path.toString())
        return if (local.exists()) {
            local
        } else {
            FileUtils.createCacheFile(context, item.title)
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

    override fun getStatusOperation(): ResponseOperation {
        val responseOperation = ResponseOperation()
        responseOperation.response = ArrayList()
        return responseOperation
    }

    override fun download(items: List<Item>): Observable<Int>? {
        TODO("Not yet implemented")
    }

    override fun upload(folderId: String, uris: List<Uri?>): Observable<Int> {
        return helper.upload(folderId, uris)
    }

    override fun share(
        id: String,
        requestExternal: RequestExternal
    ): Observable<ResponseExternal>? {
        TODO("Not yet implemented")
    }

    fun share(fileId: String, request: ShareRequest): Observable<Boolean> {
        return Observable.fromCallable { api.share(fileId, request).blockingGet() }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .map { response ->
                response.isSuccessful
            }
    }

    override fun terminate(): Observable<List<Operation>>? {
        TODO("Not yet implemented")
    }

    override fun getDownloadResponse(cloudFile: CloudFile, token: String?): Single<Response<ResponseBody>> {
        return if (cloudFile is GoogleDriveCloudFile && googleMimeTypes.contains(cloudFile.mimeType)) {
            api.export(cloudFile.id, getCommonMimeType(cloudFile.mimeType))
        } else {
            api.download(cloudFile.id)
        }
    }

}