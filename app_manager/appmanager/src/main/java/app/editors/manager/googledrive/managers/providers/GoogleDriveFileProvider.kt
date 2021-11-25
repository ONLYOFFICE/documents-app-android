package app.editors.manager.googledrive.managers.providers

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Environment
import androidx.work.Data
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import app.documents.core.network.ApiContract
import app.editors.manager.app.App
import app.editors.manager.googledrive.googledrive.login.GoogleDriveResponse
import app.editors.manager.googledrive.managers.utils.GoogleDriveUtils
import app.editors.manager.googledrive.managers.works.UploadWork
import app.editors.manager.googledrive.mvp.models.GoogleDriveFile
import app.editors.manager.googledrive.mvp.models.request.CreateItemRequest
import app.editors.manager.googledrive.mvp.models.request.RenameRequest
import app.editors.manager.googledrive.mvp.models.resonse.GoogleDriveExplorerResponse
import app.editors.manager.googledrive.ui.fragments.DocsGoogleDriveFragment
import app.editors.manager.managers.providers.BaseFileProvider
import app.editors.manager.mvp.models.base.Base
import app.editors.manager.mvp.models.explorer.*
import app.editors.manager.mvp.models.request.RequestCreate
import app.editors.manager.mvp.models.request.RequestExternal
import app.editors.manager.mvp.models.request.RequestFavorites
import app.editors.manager.mvp.models.response.ResponseExternal
import app.editors.manager.mvp.models.response.ResponseOperation
import app.editors.manager.onedrive.managers.providers.OneDriveFileProvider
import app.editors.manager.onedrive.managers.utils.OneDriveUtils
import app.editors.manager.onedrive.mvp.models.explorer.DriveItemValue
import app.editors.manager.onedrive.onedrive.OneDriveResponse
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

class GoogleDriveFileProvider: BaseFileProvider {

    companion object {
        private const val PATH_TEMPLATES = "templates/"
    }

    private val api = App.getApp().getGoogleDriveComponent()

    private val workManager = WorkManager.getInstance()

    override fun getFiles(id: String?, filter: Map<String, String>?): Observable<Explorer> {
        var queryString = "\"$id\" in parents and trashed = false"

        if(filter?.get(ApiContract.Parameters.ARG_FILTER_VALUE) != null && filter[ApiContract.Parameters.ARG_FILTER_VALUE]?.isNotEmpty() == true) {
            queryString = "name contains \'${filter[ApiContract.Parameters.ARG_FILTER_VALUE]!!}\'"
        }

        val map = mapOf(
            GoogleDriveUtils.GOOGLE_DRIVE_QUERY to queryString,
            GoogleDriveUtils.GOOGLE_DRIVE_FIELDS to GoogleDriveUtils.GOOGLE_DRIVE_FIELDS_VALUES,
            GoogleDriveUtils.GOOGLE_DRIVE_SORT to GoogleDriveUtils.getSortBy(filter)
        )
        return Observable.fromCallable { api.getFiles(map).blockingGet() }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .map { googleDriveResponse ->
                when(googleDriveResponse) {
                    is GoogleDriveResponse.Success -> {
                        return@map getExplorer((googleDriveResponse.response as GoogleDriveExplorerResponse).files, googleDriveResponse.response.nexPageToken, id!!)
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

        if(items.isNotEmpty()) {
            val parentFolder = CloudFolder().apply {
                this.id = items[0].parents[0]
            }

            for (item in items) {
                if (item.mimeType.contains("folder")) {
                    val folder = CloudFolder()
                    folder.id = item.id
                    folder.title = item.name
                    folder.parentId = item.parents[0]
                    folder.updated =
                        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).parse(item.modifiedTime)
                    folders.add(folder)
                } else  {
                    val file = CloudFile()
                    file.id = item.id
                    file.title = item.name
                    file.folderId = item.parents[0]
                    file.pureContentLength = item.size.toLong()
                    file.webUrl = item.webViewLink
                    file.fileExst = StringUtils.getExtensionFromPath(file.title.toLowerCase())
                    file.created =
                        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).parse(item.createdTime)
                    file.updated =
                        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).parse(item.modifiedTime)
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
            explorer.files = emptyList()
            explorer.folders = emptyList()
        }
        return explorer
    }

    override fun createFile(folderId: String, body: RequestCreate): Observable<CloudFile> {
        return Observable.just(1)
            .map { _ ->
                val title = body.title
                val path = PATH_TEMPLATES + body.title.lowercase()
                    .let { StringUtils.getExtensionFromPath(it) }.let {
                        FileUtils.getTemplates(
                            App.getApp(), App.getLocale(),
                            it
                        )
                    }
                val temp = title.let { StringUtils.getNameWithoutExtension(it) }.let {
                    FileUtils.createTempAssetsFile(
                        App.getApp(),
                        path,
                        it,
                        StringUtils.getExtensionFromPath(title)
                    )
                }
                upload(folderId, mutableListOf(Uri.fromFile(temp)))?.subscribe()
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
                        val folder = CloudFolder()
                        folder.id = (response.response as GoogleDriveFile).id
                        folder.title = response.response.name
                        folder.updated = Date()
                        return@map folder
                    }
                    is GoogleDriveResponse.Error -> {
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
        val request = RenameRequest(
            name = correctName
        )
        return Observable.fromCallable { api.rename(item.id, request).blockingGet() }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .map { response ->
                when(response) {
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
        }
    }

    override fun transfer(
        items: List<Item>,
        to: CloudFolder?,
        conflict: Int,
        isMove: Boolean,
        isOverwrite: Boolean
    ): Observable<List<Operation>>? {
        return items.size.let {
            Observable.fromIterable(items)
                .flatMap { item ->
                    val map = mapOf(
                        GoogleDriveUtils.MOVE_ADD_PARENTS to to?.id,
                        GoogleDriveUtils.MOVE_REMOVE_PARENTS to if (item is CloudFile) item.folderId else (item as CloudFolder).parentId
                    )
                    Observable.fromCallable { api.move(item.id, map).blockingGet() }
                }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map { response ->
                    when(response) {
                        is GoogleDriveResponse.Success -> {
                            return@map response.response
                        }
                        is GoogleDriveResponse.Error -> {
                            throw response.error
                        }
                    }
                }.buffer(it)
                .map {
                    val operation = Operation()
                    operation.progress = 100
                    return@map mutableListOf(operation)
                }
        }
    }

    fun copy(items: List<Item>, dest: String): Observable<Boolean> {
        return items.size.let {
            Observable.fromIterable(items)
                .map { item ->
                    api.copy(item.id).blockingGet()
                }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map { response ->
                    when(response) {
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

    @Throws(IOException::class)
    private fun download(emitter: Emitter<CloudFile?>, item: Item, outputFile: File) {
        val result = api.download(item.id).blockingGet()
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
        val local = File(Uri.parse(file.webUrl).path)
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

    override fun getStatusOperation(): ResponseOperation? {
        val responseOperation = ResponseOperation()
        responseOperation.response = ArrayList()
        return responseOperation
    }

    override fun download(items: List<Item>): Observable<Int>? {
        TODO("Not yet implemented")
    }

    override fun upload(folderId: String, uris: List<Uri?>): Observable<Int>? {
        return Observable.fromIterable(uris)
            .map {
                val data = Data.Builder()
                    .putString(UploadWork.KEY_FOLDER_ID, folderId)
                    .putString(UploadWork.KEY_FROM, it.toString())
                    .putString(UploadWork.KEY_TAG, DocsGoogleDriveFragment.KEY_CREATE)
                    .build()

                val request = OneTimeWorkRequest.Builder(UploadWork::class.java)
                    .setInputData(data)
                    .build()

                workManager.enqueue(request)
                1
            }
    }

    override fun share(
        id: String,
        requestExternal: RequestExternal
    ): Observable<ResponseExternal>? {
        TODO("Not yet implemented")
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
}