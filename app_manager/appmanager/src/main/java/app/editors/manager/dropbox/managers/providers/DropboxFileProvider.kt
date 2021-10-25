package app.editors.manager.dropbox.managers.providers

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Environment
import android.util.Log

import app.editors.manager.app.App
import app.editors.manager.dropbox.dropbox.api.IDropboxServiceProvider
import app.editors.manager.dropbox.dropbox.login.DropboxResponse
import app.editors.manager.dropbox.managers.utils.DropboxUtils
import app.editors.manager.dropbox.mvp.models.explorer.DropboxItem
import app.editors.manager.dropbox.mvp.models.request.CreateFolderRequest
import app.editors.manager.dropbox.mvp.models.request.DeleteRequest
import app.editors.manager.dropbox.mvp.models.request.ExplorerRequest
import app.editors.manager.dropbox.mvp.models.response.ExplorerResponse
import app.editors.manager.dropbox.mvp.models.response.MetadataResponse
import app.editors.manager.managers.providers.BaseFileProvider
import app.editors.manager.mvp.models.base.Base
import app.editors.manager.mvp.models.explorer.*
import app.editors.manager.mvp.models.request.RequestCreate
import app.editors.manager.mvp.models.request.RequestExternal
import app.editors.manager.mvp.models.request.RequestFavorites
import app.editors.manager.mvp.models.response.ResponseExternal
import app.editors.manager.mvp.models.response.ResponseOperation
import app.editors.manager.onedrive.mvp.models.explorer.DriveItemValue
import app.editors.manager.onedrive.onedrive.OneDriveResponse
import io.reactivex.Emitter
import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import lib.toolkit.base.managers.utils.FileUtils
import lib.toolkit.base.managers.utils.StringUtils
import okhttp3.ResponseBody
import retrofit2.HttpException
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class DropboxFileProvider : BaseFileProvider {

    private var api: IDropboxServiceProvider = App.getApp().getDropboxComponent()

    override fun getFiles(id: String?, filter: MutableMap<String, String>?): Observable<Explorer> {
        val request = if(id?.isEmpty() == true) ExplorerRequest(path = "/ ") else id?.let { ExplorerRequest(path = it) }
        return Observable.fromCallable {
            request?.let { api.getFiles(it).blockingGet() }
        }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .map { dropboxResponse ->
                when(dropboxResponse) {
                    is DropboxResponse.Success -> {
                        return@map getExplorer((dropboxResponse.response as ExplorerResponse).entries, id)
                    }
                    is DropboxResponse.Error -> {
                        throw dropboxResponse.error
                    }
                }
            }
    }

    private fun getExplorer(items: List<DropboxItem>, id: String?): Explorer {
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
                if (item.tag == "file") {
                    val file = CloudFile()
                    file.id = item.path_display
                    file.title = item.name
                    file.pureContentLength = item.size.toLong()
                    file.fileExst = StringUtils.getExtensionFromPath(item.name.toLowerCase())
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

        return explorer
    }

    override fun createFile(folderId: String?, body: RequestCreate?): Observable<CloudFile> {
        TODO("Not yet implemented")
    }

    override fun createFolder(folderId: String?, body: RequestCreate?): Observable<CloudFolder> {
        val createFolderRequest = folderId?.let {
            CreateFolderRequest(
                path = "$it${body?.title}",
                autorename = false
            )
        }
        return Observable.fromCallable { createFolderRequest?.let { api.createFolder(it).blockingGet() } }
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

    override fun rename(item: Item?, newName: String?, version: Int?): Observable<Item> {
        TODO("Not yet implemented")
    }

    override fun delete(
        items: MutableList<Item>?,
        from: CloudFolder?
    ): Observable<MutableList<Operation>> {
        return items?.size?.let {
            Observable.fromIterable(items).map { item -> api.delete(DeleteRequest(item.id)).blockingGet() }
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
        }!!
    }

    override fun transfer(
        items: MutableList<Item>?,
        to: CloudFolder?,
        conflict: Int,
        isMove: Boolean,
        isOverwrite: Boolean
    ): Observable<MutableList<Operation>> {
        TODO("Not yet implemented")
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

    override fun share(
        id: String?,
        requestExternal: RequestExternal?
    ): Observable<ResponseExternal> {
        TODO("Not yet implemented")
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

    @Throws(IOException::class)
    private fun download(emitter: Emitter<CloudFile?>, item: Item, outputFile: File) {
        val map = mapOf("path" to (item as CloudFile).id)
        val result = api.download(Json.encodeToString(map)).blockingGet()
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

    fun refreshInstance() {
        api = App.getApp().getDropboxComponent()
    }

}