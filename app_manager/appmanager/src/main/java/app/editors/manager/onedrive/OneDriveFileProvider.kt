package app.editors.manager.onedrive

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Environment
import android.util.Log
import app.editors.manager.app.App
import app.editors.manager.app.App.Companion.getApp
import app.editors.manager.managers.providers.BaseFileProvider
import app.editors.manager.managers.providers.OneDriveResponse
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
import kotlinx.coroutines.runBlocking
import lib.toolkit.base.managers.utils.AccountUtils
import lib.toolkit.base.managers.utils.FileUtils.createCacheFile
import lib.toolkit.base.managers.utils.FileUtils.createFile
import lib.toolkit.base.managers.utils.StringUtils
import lib.toolkit.base.managers.utils.StringUtils.getExtension
import lib.toolkit.base.managers.utils.StringUtils.getExtensionFromPath
import okhttp3.ResponseBody
import retrofit2.HttpException
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.lang.Error
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
            id?.let { api.oneDriveService.getChildren(id).blockingGet() } ?: api.oneDriveService.getFiles().blockingGet() }
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

    override fun fileInfo(item: Item?): Observable<CloudFile?> {
        return Observable.create { emitter: ObservableEmitter<CloudFile?> ->
            val outputFile = item?.let { checkDirectory(it) }
            if (outputFile != null && outputFile.exists()) {
                if (item is CloudFile) {
                    val file = item
                    if (file.pureContentLength != outputFile.length()) {
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
        TODO("Not yet implemented")
    }

    override fun download(items: MutableList<Item>?): Observable<Int> {
        TODO("Not yet implemented")
    }

    @Throws(IOException::class)
    private fun download(emitter: Emitter<CloudFile?>, item: Item, outputFile: File) {
        val result = api.oneDriveService.download((item as CloudFile).id).blockingGet()
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
        } else {
            Log.d("ONEDRIVE", "${(result as OneDriveResponse.Error).error.message}")
        }
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

    @SuppressLint("MissingPermission")
    private fun checkDirectory(item: Item): File? {
        val file = item as CloudFile
        val extension = getExtension(file.fileExst)
        when (extension) {
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