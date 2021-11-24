package app.editors.manager.googledrive.managers.providers

import android.net.Uri
import app.documents.core.network.ApiContract
import app.editors.manager.app.App
import app.editors.manager.googledrive.googledrive.login.GoogleDriveResponse
import app.editors.manager.googledrive.managers.utils.GoogleDriveUtils
import app.editors.manager.googledrive.mvp.models.GoogleDriveFile
import app.editors.manager.googledrive.mvp.models.request.CreateItemRequest
import app.editors.manager.googledrive.mvp.models.request.RenameRequest
import app.editors.manager.googledrive.mvp.models.resonse.GoogleDriveExplorerResponse
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
        val request = CreateItemRequest(
            name = body.title,
            mimeType = GoogleDriveUtils.getFileMimeType(body.title.split(".")[1]),
            parents = listOf(folderId)
        )
        return Observable.fromCallable { api.create(request).blockingGet() }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .map { response ->
                when (response) {
                    is GoogleDriveResponse.Success -> {
                        val file = CloudFile()
                        file.webUrl = (response.response as GoogleDriveFile).webViewLink
                        file.updated = Date()
                        file.id = response.response.id
                        file.title = response.response.name
                        file.fileExst = response.response.name.split(".")[1]
                        return@map file
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
        TODO("Not yet implemented")
    }

    override fun fileInfo(item: Item?): Observable<CloudFile> {
        val map = mapOf(
            GoogleDriveUtils.GOOGLE_DRIVE_FIELDS to GoogleDriveUtils.GOOGLE_DRIVE_FIELDS_VALUES
        )
        return Observable.fromCallable { item?.id?.let { api.getFileInfo(it, map).blockingGet() } }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .map { response ->
                when(response) {
                    is GoogleDriveResponse.Success -> {
                        return@map CloudFile().apply {
                            id = (response.response as GoogleDriveFile).id
                            title = response.response.name
                            folderId = response.response.parents[0]
                            pureContentLength = response.response.size.toLong()
                            webUrl = response.response.webViewLink
                            fileExst = StringUtils.getExtensionFromPath(title.toLowerCase())
                            created =
                                SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).parse(response.response.createdTime)
                            updated =
                                SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).parse(response.response.modifiedTime)
                        }
                    }
                    is GoogleDriveResponse.Error -> {
                        throw response.error
                    }
                }
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