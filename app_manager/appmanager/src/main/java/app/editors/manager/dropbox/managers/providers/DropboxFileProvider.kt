package app.editors.manager.dropbox.managers.providers

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Environment
import androidx.work.Data
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import app.documents.core.network.ApiContract
import app.editors.manager.app.App
import app.editors.manager.dropbox.dropbox.api.IDropboxServiceProvider
import app.editors.manager.dropbox.dropbox.login.DropboxResponse
import app.editors.manager.dropbox.managers.utils.DropboxUtils
import app.editors.manager.dropbox.managers.works.UploadWork
import app.editors.manager.dropbox.mvp.models.explorer.DropboxItem
import app.editors.manager.dropbox.mvp.models.operations.MoveCopyBatchCheck
import app.editors.manager.dropbox.mvp.models.operations.MoveCopyPaths
import app.editors.manager.dropbox.mvp.models.request.*
import app.editors.manager.dropbox.mvp.models.response.*
import app.editors.manager.dropbox.ui.fragments.DocsDropboxFragment
import app.editors.manager.managers.providers.BaseFileProvider
import app.editors.manager.mvp.models.base.Base
import app.editors.manager.mvp.models.explorer.*
import app.editors.manager.mvp.models.request.RequestCreate
import app.editors.manager.mvp.models.request.RequestExternal
import app.editors.manager.mvp.models.request.RequestFavorites
import app.editors.manager.mvp.models.response.ResponseExternal
import app.editors.manager.mvp.models.response.ResponseOperation
import io.reactivex.Emitter
import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import lib.toolkit.base.managers.utils.FileUtils
import lib.toolkit.base.managers.utils.StringUtils
import retrofit2.HttpException
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class DropboxFileProvider : BaseFileProvider {

    companion object {
        private const val PATH_TEMPLATES = "templates/"
        private const val TAG_FILE = "file"
        private const val TAG_FOLDER = "folder"
        private const val TAG_COMPLETE_OPERATION = "complete"
    }

    private var api: IDropboxServiceProvider = App.getApp().getDropboxComponent()
    private val workManager = WorkManager.getInstance(App.getApp())

    @Suppress("IMPLICIT_CAST_TO_ANY")
    override fun getFiles(id: String?, filter: Map<String, String>?): Observable<Explorer> {

        val req =
            if (
                (filter?.get(ApiContract.Parameters.ARG_FILTER_VALUE) != null && filter[ApiContract.Parameters.ARG_FILTER_VALUE]?.isNotEmpty() == true) &&
                (filter[DropboxUtils.DROPBOX_SEARCH_CURSOR] == null || filter[DropboxUtils.DROPBOX_SEARCH_CURSOR]?.isEmpty() == true)
            ) {
                SearchRequest(query = filter[ApiContract.Parameters.ARG_FILTER_VALUE] ?: "")
            } else if (
                (filter?.get(DropboxUtils.DROPBOX_CONTINUE_CURSOR) != null && filter[DropboxUtils.DROPBOX_CONTINUE_CURSOR]?.isNotEmpty() == true) ||
                (filter?.get(DropboxUtils.DROPBOX_SEARCH_CURSOR) != null && filter[DropboxUtils.DROPBOX_SEARCH_CURSOR]?.isNotEmpty() == true)
            ) {
                ExplorerContinueRequest(cursor = filter[DropboxUtils.DROPBOX_CONTINUE_CURSOR] ?: "")
            } else {
                if (id?.isEmpty() == true) ExplorerRequest(path = DropboxUtils.DROPBOX_ROOT) else ExplorerRequest(
                    path = id ?: ""
                )
            }

        var isSearch = false

        return Observable.fromCallable {
            if (
                (filter?.get(ApiContract.Parameters.ARG_FILTER_VALUE) != null && filter[ApiContract.Parameters.ARG_FILTER_VALUE]?.isNotEmpty() == true) &&
                (filter[DropboxUtils.DROPBOX_SEARCH_CURSOR] == null || filter[DropboxUtils.DROPBOX_SEARCH_CURSOR]?.isEmpty() == true)
            ) {
                isSearch = true
                api.search(req as SearchRequest).blockingGet()
            } else if ((filter?.get(DropboxUtils.DROPBOX_CONTINUE_CURSOR) != null && filter[DropboxUtils.DROPBOX_CONTINUE_CURSOR]?.isNotEmpty() == true) || (filter?.get(
                    DropboxUtils.DROPBOX_SEARCH_CURSOR
                ) != null && filter[DropboxUtils.DROPBOX_SEARCH_CURSOR]?.isNotEmpty() == true)
            ) {
                api.getNextFileList(req as ExplorerContinueRequest).blockingGet()
            } else if (filter?.get(DropboxUtils.DROPBOX_SEARCH_CURSOR) != null && filter[DropboxUtils.DROPBOX_SEARCH_CURSOR]?.isNotEmpty() == true) {
                isSearch = true
                api.searchNextList(req as ExplorerContinueRequest).blockingGet()
            } else {
                req.let { api.getFiles(it as ExplorerRequest).blockingGet() }
            }
        }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .flatMap { dropboxResponse ->
                when(dropboxResponse) {
                    is DropboxResponse.Success -> {
                        if(!isSearch) {
                            getExplorer((dropboxResponse.response as ExplorerResponse).entries, id, dropboxResponse.response.cursor, dropboxResponse.response.has_more)
                        } else {
                            val items = mutableListOf<DropboxItem>()
                            (dropboxResponse.response as SearchResponse).matches.forEach { searchMetadata ->
                                searchMetadata.metadata?.metadata?.let { items.add(it) }
                            }
                            getExplorer(items, id, dropboxResponse.response.cursor, dropboxResponse.response.has_more)
                        }
                    }
                    is DropboxResponse.Error -> {
                        throw dropboxResponse.error
                    }
                }
            }
            .map { explorer ->
                return@map sortExplorer(explorer, filter)
            }
    }

    private fun getExplorer(items: List<DropboxItem>, id: String?, cursor: String, hasMore: Boolean): Observable<Explorer> {
        val explorer = Explorer()
        val files: MutableList<CloudFile> = mutableListOf()
        val folders: MutableList<CloudFolder> = mutableListOf()

        if(items.isNotEmpty()) {
            val parentFolder = CloudFolder().apply {
                this.id = items[0].path_display.substring(0, items[0].path_display.lastIndexOf('/'))
                this.title = items[0].path_display.split('/').run {
                    this[this.size - 2]
                }
            }

            for (item in items) {
                if (item.tag == TAG_FILE) {
                    val file = CloudFile()
                    file.id = item.path_display
                    file.title = item.name
                    file.versionGroup = item.rev
                    file.pureContentLength = item.size.toLong()
                    file.fileExst = StringUtils.getExtensionFromPath(item.name.lowercase(Locale.getDefault()))
                    file.updated = SimpleDateFormat(
                        "yyyy-MM-dd'T'HH:mm:ss",
                        Locale.getDefault()
                    ).parse(item.client_modified)
                    files.add(file)
                } else {
                    val folder = CloudFolder()
                    folder.id = item.path_display
                    folder.title = item.name
                    //folder.updated = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).parse(item.client_modified)
                    folders.add(folder)
                }
            }
            val current = Current()
            current.filesCount = files.size.toString()
            current.foldersCount = files.size.toString()
            current.title =
                if (parentFolder.id.isEmpty()) DropboxUtils.DROPBOX_ROOT_TITLE else parentFolder.title
            current.id =
                if (parentFolder.id.isEmpty()) DropboxUtils.DROPBOX_ROOT else "${parentFolder.id}/"
            current.parentId = cursor
            current.providerItem = hasMore

            explorer.current = current
            explorer.files = files
            explorer.folders = folders
        } else {
            val current = Current()

            current.id = if(id.equals(DropboxUtils.DROPBOX_ROOT)) DropboxUtils.DROPBOX_ROOT else "$id/"
            current.filesCount = 0.toString()
            current.foldersCount = 0.toString()

            explorer.current = current
            explorer.files = emptyList()
            explorer.folders = emptyList()
        }

        return Observable.just(explorer)
    }

    override fun createFile(folderId: String, body: RequestCreate): Observable<CloudFile> {
        val title = body.title
        val path = PATH_TEMPLATES + title?.lowercase()
            .let { StringUtils.getExtensionFromPath(it!!) }.let {
                FileUtils.getTemplates(
                    App.getApp(), App.getLocale(),
                    it
                )
            }
        val temp = title.let { StringUtils.getNameWithoutExtension(it!!) }.let {
            FileUtils.createTempAssetsFile(
                App.getApp(),
                path,
                it,
                StringUtils.getExtensionFromPath(title!!)
            )
        }
        upload(folderId, mutableListOf(Uri.fromFile(temp))).subscribe()
        val file = CloudFile()
        file.webUrl = Uri.fromFile(temp).toString()
        file.pureContentLength = temp?.length() ?: 0
        file.updated = Date()
        file.id = folderId.trim() + title
        file.title = title
        file.fileExst = title?.split(".")?.get(1)
        return Observable.just(file)
    }

    override fun search(query: String?): Observable<String>? {
        TODO("Not yet implemented")
    }

    override fun createFolder(folderId: String, body: RequestCreate): Observable<CloudFolder> {
        val createFolderRequest = folderId.let {
            CreateFolderRequest(
                path = "$it${body.title}",
                autorename = false
            )
        }
        return Observable.fromCallable { createFolderRequest.let { api.createFolder(it).blockingGet() } }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .map { response ->
                when (response) {
                    is DropboxResponse.Success -> {
                        val folder = CloudFolder()
                        folder.id = (response.response as MetadataResponse).metadata?.path_display
                        folder.title = response.response.metadata?.name
                        folder.updated = Date()
                        return@map folder
                    }
                    is DropboxResponse.Error -> {
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
        val toPath = item.id?.removeRange(item.id.lastIndexOf("/") + 1, item.id.length) + correctName
        val request = MoveRequest(
            from_path = item.id,
            to_path = toPath,
            allow_shared_folder = false,
            allow_ownership_transfer = false,
            autorename = true
        )
        return Observable.fromCallable { api.move(request).blockingGet() }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .map { response ->
                when(response) {
                    is DropboxResponse.Success -> {
                        item.updated = Date()
                        item.title = newName
                        return@map item
                    }
                    is DropboxResponse.Error -> {
                        throw response.error
                    }
                }
            }
    }

    override fun delete(
        items: List<Item>,
        from: CloudFolder?
    ): Observable<List<Operation>> {
        return items.size.let {
            Observable.fromIterable(items).map { item -> api.delete(PathRequest(item.id)).blockingGet() }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map { response ->
                    if(response.isSuccessful && response.code() == 200) {
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
        to: CloudFolder?,
        conflict: Int,
        isMove: Boolean,
        isOverwrite: Boolean
    ): Observable<List<Operation>>? {
        val listItem: MutableList<MoveCopyPaths> = mutableListOf()
        items.forEach {
            listItem.add(MoveCopyPaths(
                from_path = it.id,
                to_path = to?.id?.trim() + it.title)
            )
        }
        val request = MoveCopyBatchRequest(
            entries = listItem,
            autorename = true
         )
        return Observable.fromCallable {
            if(isMove)
                api.moveBatch(request).blockingGet() else
                api.copyBatch(request).blockingGet()
        }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .flatMap { response ->
                when(response) {
                    is DropboxResponse.Success -> {
                        getStatusOperation((response.response as MoveCopyBatchResponse).async_job_id, isMove)
                    }
                    is DropboxResponse.Error -> {
                        throw response.error
                    }
                }
            }
            .map {
                val operation = Operation()
                operation.progress = 100
                return@map mutableListOf(operation)

            }
    }

    private fun getStatusOperation(id: String, isMove: Boolean): Observable<String> {
        val request = MoveCopyBatchCheck(async_job_id = id)
        return Observable.create<String>{ emitter ->
            while(true) {
                val response = if(isMove)
                    api.moveBatchCheck(request).blockingGet() else
                    api.copyBatchCheck(request).blockingGet()
                if(response.body()?.string()?.contains(TAG_COMPLETE_OPERATION) == true) {
                    emitter.onComplete()
                    break
                }
            }
        }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    override fun fileInfo(item: Item?): Observable<CloudFile> {
        return Observable.create { emitter: ObservableEmitter<CloudFile> ->
            val outputFile = item?.let { checkDirectory(it) }
            if (outputFile != null && outputFile.exists()) {
                if (item is CloudFile) {
                    if (item.pureContentLength != outputFile.length()) {
                        download(emitter, item, outputFile)
                    } else {
                        setFile(item, outputFile).let { emitter.onNext(it) }
                        emitter.onComplete()
                    }
                }
            }
        }
    }

    private fun sortExplorer(explorer: Explorer, filter: Map<String, String>?): Explorer {
        val folders = explorer.folders
        val files = explorer.files
        if (filter != null) {
            val sort = filter[ApiContract.Parameters.ARG_SORT_BY]
            val order = filter[ApiContract.Parameters.ARG_SORT_ORDER]
            if (sort != null && order != null) {
                when (sort) {
                    ApiContract.Parameters.VAL_SORT_BY_UPDATED -> folders.sortWith { o1: CloudFolder, o2: CloudFolder ->
                        o1.updated.compareTo(o2.updated)
                    }
                    ApiContract.Parameters.VAL_SORT_BY_TITLE -> {
                        folders.sortWith { o1: CloudFolder, o2: CloudFolder ->
                            o1.title.lowercase(Locale.getDefault()).compareTo(o2.title.lowercase(Locale.getDefault()))
                        }
                        files.sortWith { o1: CloudFile, o2: CloudFile ->
                            o1.title.lowercase(Locale.getDefault()).compareTo(o2.title.lowercase(Locale.getDefault()))
                        }
                    }
                    ApiContract.Parameters.VAL_SORT_BY_SIZE -> files.sortWith { o1: CloudFile, o2: CloudFile ->
                        o1.pureContentLength.compareTo(o2.pureContentLength)
                    }
                    ApiContract.Parameters.VAL_SORT_BY_TYPE -> files.sortWith { o1: CloudFile, o2: CloudFile ->
                        o1.fileExst.compareTo(o2.fileExst)
                    }
                }
                if (order == ApiContract.Parameters.VAL_SORT_ORDER_DESC) {
                    folders.reverse()
                    files.reverse()
                }
            }
        }
        explorer.folders = folders
        explorer.files = files
        return explorer
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
        return Observable.fromIterable(uris)
            .flatMap {
                val data = Data.Builder()
                    .putString(UploadWork.TAG_FOLDER_ID, folderId)
                    .putString(UploadWork.TAG_UPLOAD_FILES, it.toString())
                    .putString(UploadWork.KEY_TAG, DocsDropboxFragment.KEY_CREATE)
                    .build()

                val request = OneTimeWorkRequest.Builder(UploadWork::class.java)
                    .setInputData(data)
                    .build()

                workManager.enqueue(request)
                return@flatMap Observable.just(1)
            }
    }

    override fun share(
        id: String,
        requestExternal: RequestExternal
    ): Observable<ResponseExternal>? {
        TODO("Not yet implemented")
    }

    fun share(id: String): Observable<ExternalLinkResponse>? {
        val request = PathRequest(path = id)
        return Observable.fromCallable { api.getExternalLink(request).blockingGet() }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .map { externalLinkResponse ->
                when(externalLinkResponse) {
                    is DropboxResponse.Success -> {
                        return@map (externalLinkResponse.response as ExternalLinkResponse)
                    }
                    is DropboxResponse.Error -> {
                        throw externalLinkResponse.error
                    }
                }
            }
    }

    override fun terminate(): Observable<List<Operation>>? {
        TODO("Not yet implemented")
    }

    override fun addToFavorites(requestFavorites: RequestFavorites): Observable<Base>? {
        TODO("Not yet implemented")
    }

    override fun deleteFromFavorites(requestFavorites: RequestFavorites): Observable<Base>? {
        TODO("Not yet implemented")
    }

    @Throws(IOException::class)
    private fun download(emitter: Emitter<CloudFile?>, item: Item, outputFile: File) {
        val request = "{\"path\":\"${DropboxUtils.encodeUnicodeSymbolsDropbox((item as CloudFile).id)}\"}"
        val result = api.download(request).blockingGet()
        result.body()?.let { file ->
            try {
                file.byteStream().use { inputStream ->
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

    @SuppressLint("MissingPermission")
    private fun checkDirectory(item: Item): File? {
        val file = item as CloudFile
        when (StringUtils.getExtension(file.fileExst)) {
            StringUtils.Extension.UNKNOWN, StringUtils.Extension.EBOOK, StringUtils.Extension.ARCH, StringUtils.Extension.VIDEO, StringUtils.Extension.HTML -> {
                val parent =
                    File(Environment.getExternalStorageDirectory().absolutePath + "/OnlyOffice")
                return FileUtils.createFile(parent, file.title)
            }
        }
        val local = File(Uri.parse(file.webUrl).path ?: "")
        return if (local.exists()) {
            local
        } else {
            FileUtils.createCacheFile(App.getApp(), item.getTitle())
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

    fun refreshInstance() {
        api = App.getApp().getDropboxComponent()
    }

}