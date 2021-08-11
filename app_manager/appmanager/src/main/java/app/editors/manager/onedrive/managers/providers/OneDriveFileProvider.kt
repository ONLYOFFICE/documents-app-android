package app.editors.manager.onedrive.managers.providers

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
import app.editors.manager.onedrive.*
import app.editors.manager.onedrive.di.component.OneDriveComponent
import app.editors.manager.onedrive.mvp.models.explorer.DriveItemCloudTree
import app.editors.manager.onedrive.mvp.models.explorer.DriveItemFolder
import app.editors.manager.onedrive.mvp.models.explorer.DriveItemValue
import app.editors.manager.onedrive.mvp.models.request.ChangeFileRequest
import app.editors.manager.onedrive.mvp.models.request.CreateFolderRequest
import app.editors.manager.onedrive.mvp.models.request.RenameRequest
import app.editors.manager.onedrive.mvp.models.request.UploadRequest
import app.editors.manager.onedrive.mvp.models.response.UploadResponse
import app.editors.manager.onedrive.ui.fragments.DocsOneDriveFragment
import io.reactivex.*
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import lib.toolkit.base.managers.utils.AccountUtils
import lib.toolkit.base.managers.utils.FileUtils.createCacheFile
import lib.toolkit.base.managers.utils.FileUtils.createFile
import lib.toolkit.base.managers.utils.PathUtils
import lib.toolkit.base.managers.utils.StringUtils
import lib.toolkit.base.managers.utils.StringUtils.getExtension
import lib.toolkit.base.managers.utils.StringUtils.getExtensionFromPath
import okhttp3.ResponseBody
import retrofit2.HttpException
import java.io.*
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.min

class OneDriveFileProvider : BaseFileProvider {

    var api = getOneDriveApi()

    private var tag: String = ""

    @JvmName("getApiAsync")
    private fun getOneDriveApi(): OneDriveComponent = runBlocking {
        getApp().appComponent.accountsDao.getAccountOnline()?.let { cloudAccount ->
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
                        throw response.error
                        return@map null
                    }
                    else -> return@map null
                }
            }
    }

    override fun upload(folderId: String?, uris: MutableList<Uri>?): Observable<Int> {
        val request = UploadRequest()
        val fileName =
            uris?.get(0)?.path?.let { lib.toolkit.base.managers.utils.FileUtils.getFileName(it, true) }
        return Observable.fromCallable {
            folderId?.let {
                fileName?.let { it1 ->
                    api.oneDriveService.uploadFile(
                        it, it1, when (tag) {
                            DocsOneDriveFragment.KEY_UPLOAD -> request.copy(
                                item = app.editors.manager.onedrive.mvp.models.other.Item(
                                    "rename"
                                )
                            )
                            DocsOneDriveFragment.KEY_UPDATE -> request.copy(
                                item = app.editors.manager.onedrive.mvp.models.other.Item(
                                    "replace"
                                )
                            )
                            else -> request.copy(
                                item = app.editors.manager.onedrive.mvp.models.other.Item(
                                    "fail"
                                )
                            )
                        }
                    ).blockingGet()
                }
            }
        }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .map { response ->
                if (response is OneDriveResponse.Success) {
                    uris?.get(0)?.let {
                        uploadSession(
                            (response.response as UploadResponse).uploadUrl,
                            it
                        )
                    }
                    return@map 10
                } else {
                    return@map 12
                }
            }
    }

    fun upload(folderId: String?, uris: MutableList<Uri>?, tag: String): Observable<Int> {
        this.tag = tag
        return upload(folderId, uris)
    }

    private fun uploadSession(url: String, uri: Uri) {
        CoroutineScope(Dispatchers.Default).launch {
            val connection = URL(url).openConnection() as HttpURLConnection
            val fileInputStream = getApp().contentResolver.openInputStream(uri)
            val maxBufferSize = 62914560
            val boundary = "*****"
            var outputStream: OutputStream? = null
            var bytesAvailable = fileInputStream?.available()
            connection.doInput = true
            connection.doOutput = true
            connection.useCaches = false
            connection.requestMethod = "PUT"
            connection.setRequestProperty("Connection", "Keep-Alive")
            connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=$boundary")
            try {
                outputStream = DataOutputStream(connection.outputStream)
                var bufferSize = min(bytesAvailable!!, maxBufferSize)
                val buffer = ByteArray(bufferSize)
                var bytesRead = fileInputStream?.read(buffer, 0, bufferSize)
                while (bytesRead!! > 0) {
                    outputStream.write(buffer, 0, bufferSize)
                    bytesAvailable = fileInputStream?.available()
                    bufferSize = min(bytesAvailable!!, maxBufferSize)
                    bytesRead = fileInputStream?.read(buffer, 0, bufferSize)
                }
                Log.d("ONEDRIVE", "${connection.responseCode}, ${connection.responseMessage}")
            } catch (e: Exception) {
                Log.d("ONEDRIVE", "Exception, ${e.localizedMessage}, ${e.cause?.message}")
            }
        }
    }

    private fun getExplorer(response: DriveItemCloudTree): Explorer {
        val explorer = Explorer()
        val files: MutableList<CloudFile> = mutableListOf()
        val folders: MutableList<CloudFolder> = mutableListOf()

        if(response.value.isNotEmpty()) {

            val nameParentFolder = response.value.get(0).parentReference.path.split("/")
            val name = nameParentFolder.get(2)
            val correctName = name.removeRange(name.length - 1, name.length)

            val parentFolder = CloudFolder().apply {
                this.id = response.value.get(0).parentReference.id
                this.title = correctName
                this.etag = response.value.get(0).eTag
                this.updated =
                    SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse(response.value.get(0).lastModifiedDateTime)
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
            api.oneDriveService.createFile(folderId!!,
                it, mapOf("@microsoft.graph.conflictBehavior" to "rename")).blockingGet()
        } }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .map {response ->
                if(response is OneDriveResponse.Success) {
                    val file = CloudFile()
                    file.id = (response.response as DriveItemValue).id
                    file.title = response.response.name
                    file.updated = Date()
                    return@map file
                } else {
                    Log.d("ONEDRIVE", "${(response as OneDriveResponse.Error).error.message}")
                    return@map null
                }
            }
    }

    override fun createFolder(folderId: String?, body: RequestCreate?): Observable<CloudFolder>? {
        val request = CreateFolderRequest(
            name = body?.title!!,
            folder = DriveItemFolder(),
            conflictBehavior = "rename"
        )
        return Observable.fromCallable { api.oneDriveService.createFolder(folderId!!, request).blockingGet() }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .map { response ->
                if(response is OneDriveResponse.Success) {
                    val folder = CloudFolder()
                    folder.id = (response.response as DriveItemValue).id
                    folder.title = response.response.name
                    folder.updated = Date()
                    return@map folder
                } else {
                    Log.d("ONEDRIVE", "${(response as OneDriveResponse.Error).error.message}")
                    return@map null
                }
            }
    }

    override fun rename(item: Item?, newName: String?, version: Int?): Observable<Item> {
        val correctName = StringUtils.getEncodedString(newName) + (item as CloudFile).fileExst
        val request = RenameRequest(correctName)
        return Observable.fromCallable{ api.oneDriveService.renameItem(item.id, request).blockingGet() }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .map { response ->
                if(response.isSuccessful) {
                    item.updated = Date()
                    item.title = newName
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
            Observable.fromIterable(items).map { item -> api.oneDriveService.deleteItem(item.id).blockingGet() }
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

    @SuppressLint("MissingPermission")
    fun download(id: String, uri: Uri): Observable<Int?> {
        return Observable.create { emitter: ObservableEmitter<Int?> ->
            val response = api.oneDriveService.download(id).blockingGet()
            val outputFile = File(PathUtils.getPath(getApp().applicationContext, uri))
            if (response is OneDriveResponse.Success) {
                try {
                    (response.response as ResponseBody)
                        .byteStream().use { inputStream ->
                            FileOutputStream(outputFile).use { outputStream ->
                                val buffer = ByteArray(4096)
                                var count: Int
                                var progress = 0
                                val fileSize = response.response.contentLength()
                                while (inputStream.read(buffer).also { count = it } != -1) {
                                    outputStream.write(buffer, 0, count)
                                    progress += count
                                    emitter.onNext((progress.toDouble() / fileSize.toDouble() * 100).toInt())
                                }
                                outputStream.flush()
                                emitter.onNext(100)
                                emitter.onComplete()
                            }
                        }
                } catch (error: IOException) {
                    emitter.onNext(0)
                    emitter.onError(error)
                }
            } else {
                Log.d("ONEDRIVE", "${(response as OneDriveResponse.Error).error.message}")
            }
        }
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

    fun updateFile(folderId: String?, uri: Uri): Observable<Int> {
        val iStream = uri.let { App.getApp().contentResolver.openInputStream(it) }
        val data = iStream?.let { getBytes(it) }?.let { ChangeFileRequest(it) }
        return Observable.fromCallable { folderId?.let { data?.let { it1 ->
            api.oneDriveService.updateFile(it,
                it1
            ).blockingGet()
        } } }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .map { response ->
                if(response.isSuccessful) {
                    Log.d("ONEDRIVE", "${response.body()?.string()}")
                    return@map 10
                } else {
                    Log.d("ONEDRIVE", "${response.errorBody()?.string()}")
                    return@map 12
                }
            }
    }

    private fun getBytes(inputStream: InputStream): ByteArray {
        val stream = ByteArrayOutputStream()
        var bytesAvailable = inputStream.available()
        var bufferSize = min(bytesAvailable, 1024)
        val buffer = ByteArray(bufferSize)
        var len = 0
        len = inputStream.read(buffer, 0, bufferSize)
        while (len > 0) {
            stream.write(buffer, 0, bufferSize)
            bytesAvailable = inputStream.available()
            bufferSize = min(bytesAvailable, 1024)
            len = inputStream.read(buffer, 0, bufferSize)
        }
        return stream.toByteArray()
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