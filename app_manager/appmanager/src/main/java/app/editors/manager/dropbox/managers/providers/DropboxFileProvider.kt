package app.editors.manager.dropbox.managers.providers

import android.net.Uri
import android.util.Log
import androidx.compose.ui.text.toLowerCase
import app.editors.manager.app.App
import app.editors.manager.dropbox.dropbox.api.IDropboxServiceProvider
import app.editors.manager.dropbox.dropbox.login.DropboxResponse
import app.editors.manager.dropbox.managers.utils.DropboxUtils
import app.editors.manager.dropbox.mvp.models.explorer.DropboxItem
import app.editors.manager.dropbox.mvp.models.request.ExplorerRequest
import app.editors.manager.dropbox.mvp.models.response.ExplorerResponse
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
import java.text.SimpleDateFormat
import java.util.*

class DropboxFileProvider : BaseFileProvider {

    private var api: IDropboxServiceProvider = App.getApp().getDropboxComponent()

    override fun getFiles(id: String?, filter: MutableMap<String, String>?): Observable<Explorer> {
        val request = id?.let { ExplorerRequest(path = it) }
        return Observable.fromCallable {
            request?.let { api.getFiles(it).blockingGet() }
        }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .map { dropboxResponse ->
                when(dropboxResponse) {
                    is DropboxResponse.Success -> {
                        return@map getExplorer((dropboxResponse.response as ExplorerResponse).entries)
                    }
                    is DropboxResponse.Error -> {
                        throw dropboxResponse.error
                    }
                }
            }
    }

    private fun getExplorer(items: List<DropboxItem>): Explorer {
        val explorer = Explorer()
        val files: MutableList<CloudFile> = mutableListOf()
        val folders: MutableList<CloudFolder> = mutableListOf()

        val parentFolder = CloudFolder().apply {
            this.id = items[0].path_display.substring(0, items[0].path_display.lastIndexOf('/'))
            this.title = items[0].path_display.split('/').run {
                this[this.size - 2]
            }
        }

        for(item in items) {
            if(item.tag == "file") {
                val file = CloudFile()
                file.id = item.id
                file.title = item.name
                file.pureContentLength = item.size.toLong()
                file.fileExst = StringUtils.getExtensionFromPath(item.name.toLowerCase())
                file.updated = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).parse(item.client_modified)
                files.add(file)
            } else {
                val folder = CloudFolder()
                folder.id = item.id
                folder.title = item.name
                //folder.updated = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).parse(item.client_modified)
                folders.add(folder)
            }
        }
        val current = Current()
        current.filesCount = files.size.toString()
        current.foldersCount = files.size.toString()
        current.title = if(parentFolder.id.isEmpty()) "root" else parentFolder.title
        current.id = if(parentFolder.id.isEmpty()) DropboxUtils.DROPBOX_ROOT else parentFolder.id

        explorer.current = current
        explorer.files = files
        explorer.folders = folders

        return explorer
    }

    override fun createFile(folderId: String?, body: RequestCreate?): Observable<CloudFile> {
        TODO("Not yet implemented")
    }

    override fun createFolder(folderId: String?, body: RequestCreate?): Observable<CloudFolder> {
        TODO("Not yet implemented")
    }

    override fun rename(item: Item?, newName: String?, version: Int?): Observable<Item> {
        TODO("Not yet implemented")
    }

    override fun delete(
        items: MutableList<Item>?,
        from: CloudFolder?
    ): Observable<MutableList<Operation>> {
        TODO("Not yet implemented")
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

    override fun fileInfo(item: Item?): Observable<CloudFile> {
        TODO("Not yet implemented")
    }

    override fun getStatusOperation(): ResponseOperation {
        TODO("Not yet implemented")
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
}