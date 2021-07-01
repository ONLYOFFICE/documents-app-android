package app.editors.manager.onedrive

import android.net.Uri
import android.util.Log
import app.editors.manager.app.App
import app.editors.manager.managers.providers.BaseFileProvider
import app.editors.manager.managers.providers.OneDriveResponse
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
import kotlinx.coroutines.runBlocking
import lib.toolkit.base.managers.utils.AccountUtils
import lib.toolkit.base.managers.utils.StringUtils.getExtensionFromPath
import java.text.SimpleDateFormat
import java.util.*

class OneDriveFileProvider : BaseFileProvider {

    var api = getOneDriveApi()

    @JvmName("getApiAsync")
    private fun getOneDriveApi(): OneDriveComponent = runBlocking {
        App.getApp().appComponent.accountsDao.getAccountOnline()?.let { cloudAccount ->
            AccountUtils.getToken(
                context = App.getApp().applicationContext,
                accountName = cloudAccount.getAccountName()
            )?.let { token ->
                return@runBlocking App.getApp().getOneDriveComponent(token)
            }
        } ?: run {
            throw Exception("No account")
        }
    }

    override fun getFiles(id: String?, filter: MutableMap<String, String>?): Observable<Explorer>? {
        return Observable.fromCallable {
            api.oneDriveService.getFiles().blockingGet() }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .map { response ->
                when(response) {
                    is OneDriveResponse.Success -> {
                        return@map getExplorer(response.response as DriveItemCloudTree)
                    }
                    is OneDriveResponse.Error -> {
                        Log.d("ONEDRIVE","${response.error.message}")
                        return@map null
                    }
                    else -> return@map null
                }
            }
    }

    private fun getExplorer(response: DriveItemCloudTree): Explorer {
        val explorer = Explorer()
        val files: MutableList<CloudFile> = mutableListOf()
        val folders: MutableList<CloudFolder> = mutableListOf()


        val nameParentFolder = response.value.get(0).parentReference.path.split("/")
        val name = nameParentFolder.get(2)
        val correctName = name.removeRange(name.length - 1, name.length)

        val parentFolder = CloudFolder().apply {
            this.id = response.value.get(0).id
            this.title = correctName
            this.etag = response.value.get(0).eTag
            this.updated = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse(response.value.get(0).lastModifiedDateTime)
        }

        for(item in response.value) {
            if(item.folder != null) {
                val folder = CloudFolder()
                folder.id = item.id
                folder.title = item.name
                folder.parentId = item.parentReference.id
                folder.updated = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse(item.lastModifiedDateTime)
                folder.etag = item.eTag
                folders.add(folder)
            } else if(item.file != null) {
                val file = CloudFile()
                file.id = item.id
                file.title = item.name
                file.folderId = item.parentReference.id
                file.pureContentLength = item.size.toLong()
                file.fileExst = getExtensionFromPath(file.title.toLowerCase())
                file.created = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse(item.createdDateTime)
                file.updated = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse(item.lastModifiedDateTime)
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