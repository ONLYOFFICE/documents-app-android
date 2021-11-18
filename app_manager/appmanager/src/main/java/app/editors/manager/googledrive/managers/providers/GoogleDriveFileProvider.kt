package app.editors.manager.googledrive.managers.providers

import android.net.Uri
import app.editors.manager.app.App
import app.editors.manager.dropbox.mvp.models.request.PathRequest
import app.editors.manager.googledrive.googledrive.login.GoogleDriveResponse
import app.editors.manager.googledrive.mvp.models.GoogleDriveFile
import app.editors.manager.googledrive.mvp.models.resonse.GoogleDriveExplorerResponse
import app.editors.manager.managers.providers.BaseFileProvider
import app.editors.manager.mvp.models.base.Base
import app.editors.manager.mvp.models.explorer.*
import app.editors.manager.mvp.models.request.RequestCreate
import app.editors.manager.mvp.models.request.RequestExternal
import app.editors.manager.mvp.models.request.RequestFavorites
import app.editors.manager.mvp.models.response.ResponseExternal
import app.editors.manager.mvp.models.response.ResponseOperation
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import lib.toolkit.base.managers.utils.StringUtils
import retrofit2.HttpException
import java.text.SimpleDateFormat
import java.util.*

class GoogleDriveFileProvider: BaseFileProvider {

    private val api = App.getApp().getGoogleDriveComponent()

    override fun getFiles(id: String?, filter: Map<String, String>?): Observable<Explorer> {
        val map = mapOf(
            "q" to "\"root\" in parents",
            "fields" to "nextPageToken, files/id, files/name, files/mimeType, files/description, files/parents, files/webViewLink, files/webContentLink, files/modifiedTime, files/createdTime, files/capabilities/canDelete, files/size"
        )
        return Observable.fromCallable { api.getFiles(map).blockingGet() }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .map { googleDriveResponse ->
                when(googleDriveResponse) {
                    is GoogleDriveResponse.Success -> {
                        return@map getExplorer((googleDriveResponse.response as GoogleDriveExplorerResponse).files)
                    }
                    is GoogleDriveResponse.Error -> {
                        throw googleDriveResponse.error
                    }
                }
            }

    }

    private fun getExplorer(items: List<GoogleDriveFile>): Explorer {
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

            explorer.current = current
            explorer.files = files
            explorer.folders = folders
        }
//        else {
//            val current = Current()
//
//            val context = response.context.split("/")
//
//            current.id = context[6].split("'")[1].replace("%21", "!")
//            current.filesCount = 0.toString()
//            current.foldersCount = 0.toString()
//
//            explorer.current = current
//            explorer.files = emptyList()
//            explorer.folders = emptyList()
//        }
        return explorer
    }

    override fun createFile(folderId: String, body: RequestCreate): Observable<CloudFile> {
        TODO("Not yet implemented")
    }

    override fun search(query: String?): Observable<String>? {
        TODO("Not yet implemented")
    }

    override fun createFolder(folderId: String, body: RequestCreate): Observable<CloudFolder> {
        TODO("Not yet implemented")
    }

    override fun rename(item: Item, newName: String, version: Int?): Observable<Item> {
        TODO("Not yet implemented")
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
        TODO("Not yet implemented")
    }

    override fun fileInfo(item: Item?): Observable<CloudFile> {
        TODO("Not yet implemented")
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
        TODO("Not yet implemented")
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