package app.documents.core.providers

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.os.Environment
import app.documents.core.network.common.contracts.ApiContract
import app.documents.core.network.login.models.request.ProgressRequestBody
import app.documents.core.network.manager.models.explorer.*
import app.documents.core.network.webdav.WebDavService
import app.documents.core.network.webdav.models.WebDavModel
import app.documents.core.network.manager.models.request.RequestCreate
import app.documents.core.network.manager.models.request.RequestExternal
import app.documents.core.network.manager.models.response.ResponseExternal
import app.documents.core.network.manager.models.response.ResponseOperation
import io.reactivex.Emitter
import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import io.reactivex.ObservableSource
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import lib.toolkit.base.BuildConfig
import lib.toolkit.base.managers.utils.ContentResolverUtils.getName
import lib.toolkit.base.managers.utils.ContentResolverUtils.getSize
import lib.toolkit.base.managers.utils.FileUtils.createCacheFile
import lib.toolkit.base.managers.utils.FileUtils.createFile
import lib.toolkit.base.managers.utils.FileUtils.createTempAssetsFile
import lib.toolkit.base.managers.utils.FileUtils.getPercentOfLoading
import lib.toolkit.base.managers.utils.FileUtils.getTemplates
import lib.toolkit.base.managers.utils.NetworkUtils.decodeUrl
import lib.toolkit.base.managers.utils.StringUtils
import lib.toolkit.base.managers.utils.StringUtils.getEncodedString
import lib.toolkit.base.managers.utils.StringUtils.getExtension
import lib.toolkit.base.managers.utils.StringUtils.getExtensionFromPath
import lib.toolkit.base.managers.utils.StringUtils.getNameWithoutExtension
import okhttp3.ResponseBody
import retrofit2.HttpException
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.UnsupportedEncodingException
import java.util.*
import javax.inject.Inject

class WebDavFileProvider @Inject constructor(
    private val webDavService: WebDavService,
    private val context: Context
) : BaseFileProvider {

    companion object {
        private const val TOTAL_PROGRESS = 100
        private const val PATH_TEMPLATES = "templates/"
        private val PATH_DOWNLOAD = Environment.getExternalStorageDirectory().absolutePath + "/${BuildConfig.ROOT_FOLDER}"
    }

    private var batchItems: List<Item>? = null
    val uploadsFile: MutableList<CloudFile> = Collections.synchronizedList(ArrayList())

    override fun getFiles(id: String?, filter: Map<String, String>?): Observable<Explorer> {
        return Observable.fromCallable { id?.let { webDavService.propfind(it).execute() } }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .map { modelResponse: Response<WebDavModel?> ->
                if (modelResponse.isSuccessful && modelResponse.body() != null) {
                    return@map modelResponse.body()
                } else {
                    throw HttpException(modelResponse)
                }
            }.map { webDavModel: WebDavModel -> getExplorer(webDavModel.list ?: emptyList(), filter) }
    }

    override fun createFile(folderId: String, body: RequestCreate): Observable<CloudFile> {
        val title = body.title
        val path = PATH_TEMPLATES + getTemplates(
            context, Locale.getDefault().language,
            getExtensionFromPath(body.title.lowercase())
        )
        val temp = createTempAssetsFile(context, path, getNameWithoutExtension(title), getExtensionFromPath(title))
        return Observable.fromCallable {
            val file = CloudFile()
            file.webUrl = Uri.fromFile(temp).toString()
            file.pureContentLength = temp?.length() ?: 0
            file.id = folderId + body.title
            file.updated = Date()
            file.title = body.title
            file.fileExst = getExtensionFromPath(body.title)
            file
        }
    }

    override fun search(query: String?): Observable<String> {
        TODO("Not yet implemented")
    }

    override fun createFolder(folderId: String, body: RequestCreate): Observable<CloudFolder> {
        return Observable.fromCallable { webDavService.createFolder(folderId + body.title).execute() }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .map { responseBody: Response<ResponseBody> ->
                if (responseBody.isSuccessful) {
                    val folder = CloudFolder()
                    folder.title = body.title
                    folder.id = folderId + body.title
                    folder.updated = Date()
                    return@map folder
                } else {
                    throw HttpException(responseBody)
                }
            }
    }

    override fun rename(item: Item, newName: String, version: Int?): Observable<Item> {
        var name = newName
        val correctPath: String
        return if (item is CloudFile && version != null) {
            name = getEncodedString(name) + item.fileExst
            correctPath = filePath(item.id, name)
            renameFile(correctPath, name, item)
        } else if (item is CloudFolder) {
            correctPath = folderPath(item.id, name)
            renameFolder(correctPath, name, item)
        } else {
            Observable.just(Item())
        }
    }

    private fun renameFolder(correctPath: String, newName: String, folder: CloudFolder): Observable<Item> {
        return Observable.fromCallable {
            webDavService.move(
                getEncodedString(correctPath)!!, folder.id, "F"
            ).execute()
        }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .map { responseBody: Response<ResponseBody> ->
                if (responseBody.isSuccessful) {
                    folder.id = correctPath
                    folder.title = decodeUrl(newName)
                    folder.updated = Date()
                    return@map folder
                } else {
                    throw HttpException(responseBody)
                }
            }
    }

    private fun renameFile(correctPath: String, newName: String, file: CloudFile): Observable<Item> {
        return Observable.fromCallable { webDavService.move(correctPath, file.id, "F").execute() }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .map { responseBody: Response<ResponseBody> ->
                if (responseBody.isSuccessful) {
                    file.id = correctPath
                    file.title = decodeUrl(newName)
                    file.updated = Date()
                    return@map file
                } else {
                    throw HttpException(responseBody)
                }
            }
    }

    override fun delete(items: List<Item>, from: CloudFolder?): Observable<List<Operation>> {
        batchItems = items
        return Observable.fromIterable(items).map { item: Item -> webDavService.delete(item.id).execute() }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .map { responseBody: Response<ResponseBody> ->
                if (responseBody.isSuccessful && (responseBody.code() == 204 || responseBody.code() == 202)) {
                    return@map responseBody
                } else {
                    throw HttpException(responseBody)
                }
            }.buffer(items.size)
            .map {
                val operations: MutableList<Operation> = ArrayList()
                val operation = Operation()
                operation.progress = TOTAL_PROGRESS
                operations.add(operation)
                operations
            }
    }

    override fun transfer(
        items: List<Item>,
        to: CloudFolder,
        conflict: Int,
        isMove: Boolean,
        isOverwrite: Boolean
    ): Observable<List<Operation>> {
        return if (isMove) {
            moveItems(items, to, isOverwrite)
        } else {
            copyItems(items, to, isOverwrite)
        }
    }

    private fun copyItems(items: List<Item>, to: CloudFolder, overwrite: Boolean): Observable<List<Operation>> {
        val headerOverwrite = if (overwrite) "T" else "F"
        return Observable.fromIterable(items)
            .flatMap { item: Item ->
                Observable.fromCallable {
                    webDavService.copy(
                        getEncodedString(to.id) +
                                getEncodedString(item.title), item.id, headerOverwrite
                    )
                        .execute()
                }
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
            }
            .map { responseBody: Response<ResponseBody> ->
                if (responseBody.isSuccessful) {
                    val operation = Operation()
                    operation.progress = TOTAL_PROGRESS
                    return@map listOf(operation)
                } else {
                    val httpException = HttpException(responseBody)
                    httpException.addSuppressed(Exception(getTitle(responseBody.raw().request().header("Destination"))))
                    throw httpException
                }
            }
    }

    private fun moveItems(items: List<Item>, to: CloudFolder, overwrite: Boolean): Observable<List<Operation>> {
        val headerOverwrite = if (overwrite) "T" else "F"
        return Observable.fromIterable(items)
            .flatMap { item: Item ->
                Observable.fromCallable {
                    webDavService
                        .move(
                            getEncodedString(to.id) +
                                    getEncodedString(item.title), item.id, headerOverwrite
                        )
                        .execute()
                }
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
            }
            .map { responseBody: Response<ResponseBody> ->
                if (responseBody.isSuccessful) {
                    return@map responseBody
                } else {
                    val httpException = HttpException(responseBody)
                    httpException.addSuppressed(Exception(getTitle(responseBody.raw().request().header("Destination"))))
                    throw httpException
                }
            }
            .buffer(items.size)
            .map {
                val operation = Operation()
                operation.progress = TOTAL_PROGRESS
                listOf(operation)
            }
    }

    override fun fileInfo(item: Item?): Observable<CloudFile> {
        return Observable.create { emitter: ObservableEmitter<CloudFile> ->
            val outputFile = checkDirectory(item)

            //TODO Возможно надо пределать
//            Response<WebDavModel> response = mApi.propfind(item.getId()).execute();
//            if (response.isSuccessful() && response.body() != null) {
//                WebDavModel.ResponseBean bean = response.body().getList().get(0);
//                if (bean != null) {
//                    if (outputFile != null && outputFile.exists()) {
//                        if (item instanceof File) {
//                            File file = (File) item;
//                            if (file.getPureContentLength() != outputFile.length() || outputFile.length() != Long.parseLong(bean.getContentLength())) {
//                                download(emitter, item, outputFile);
//                            } else  {
//                                emitter.onNext(setFile(item, outputFile));
//                                emitter.onComplete();
//                            }
//                        }
//                    }
//                }
//            }
            if (outputFile != null && outputFile.exists()) {
                if (item is CloudFile) {
                    if (item.pureContentLength != outputFile.length()) {
                        download(emitter, item, outputFile)
                    } else {
                        emitter.onNext(setFile(item, outputFile))
                        emitter.onComplete()
                    }
                }
            }
        }
    }

    fun fileInfo(item: Item, isDownload: Boolean): Observable<CloudFile> {
        return Observable.create { emitter: ObservableEmitter<CloudFile> ->
            val outputFile = checkDirectory(item)
            if (outputFile != null && outputFile.exists()) {
                if (item is CloudFile) {
                    if (isDownload) {
                        download(emitter, item, outputFile)
                    } else {
                        emitter.onNext(setFile(item, outputFile))
                        emitter.onComplete()
                    }
                }
            }
        }
    }

    @Throws(IOException::class)
    private fun download(emitter: Emitter<CloudFile>, item: Item, outputFile: File) {
        val response = webDavService.download(item.id).blockingGet()
        if (response.body() != null) {
            try {
                response.body()!!.byteStream().use { inputStream ->
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
        } else {
            emitter.onError(HttpException(response))
        }
    }

    override fun getDownloadResponse(cloudFile: CloudFile, token: String?): Single<Response<ResponseBody>> {
        return webDavService.download(cloudFile.id)
    }

    @SuppressLint("MissingPermission")
    private fun checkDirectory(item: Item?): File? {
        val file = item as CloudFile
        when (getExtension(file.fileExst)) {
            StringUtils.Extension.UNKNOWN, StringUtils.Extension.EBOOK, StringUtils.Extension.ARCH, StringUtils.Extension.VIDEO, StringUtils.Extension.HTML -> {
                val parent = File(Environment.getExternalStorageDirectory().absolutePath + "/OnlyOffice")
                return createFile(parent, file.title)
            }
            else -> {
                // Stub
            }
        }
        val local = File(Uri.parse(file.webUrl).path ?: "")
        return if (local.exists()) {
            local
        } else {
            createCacheFile(context, item.title)
        }
    }

    private fun setFile(item: Item, outputFile: File): CloudFile {
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

    override fun download(items: List<Item>): Observable<Int> {
        return Observable.fromIterable(items)
            .filter { item: Item? -> item is CloudFile }
            .flatMap { item: Item -> startDownload(item) }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    @SuppressLint("MissingPermission")
    private fun startDownload(item: Item): Observable<Int> {
        return Observable.create { emitter: ObservableEmitter<Int> ->
            val response = webDavService.download(item.id).blockingGet()
            val outputFile = File(PATH_DOWNLOAD, item.title)
            if (response.body() != null) {
                try {
                    response.body()!!.byteStream().use { inputStream ->
                        FileOutputStream(outputFile).use { outputStream ->
                            val buffer = ByteArray(4096)
                            var count: Int
                            var progress = 0
                            val fileSize = response.body()!!.contentLength()
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
                emitter.onError(HttpException(response))
            }
        }
    }

    override fun upload(folderId: String, uris: List<Uri?>): Observable<Int> {
        return Observable.fromIterable(uris)
            .map { uri: Uri -> addUploadsFile(folderId, uri) }
            .flatMap { uri: Uri -> startUpload(folderId, uri) }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    private fun addUploadsFile(id: String, uri: Uri): Uri {
        val fileName = getName(context, uri)
        val uploadFile = CloudFile()
        uploadFile.id = id + fileName
        uploadFile.folderId = id
        uploadFile.fileExst = getExtensionFromPath(fileName)
        uploadFile.title = fileName
        uploadFile.updated = Date()
        uploadFile.webUrl = uri.toString()
        uploadFile.pureContentLength = getSize(context, uri)
        uploadsFile.add(uploadFile)
        return uri
    }

    private fun startUpload(id: String, uri: Uri): ObservableSource<Int> {
        return Observable.create { emitter: ObservableEmitter<Int> ->
            val fileName = getName(context, uri)
            val requestBody = ProgressRequestBody(context, uri)
            requestBody.setOnUploadCallbacks { total: Long, progress: Long ->
                emitter.onNext(
                    getPercentOfLoading(
                        total,
                        progress
                    )
                )
            }
            val response = webDavService.upload(requestBody, id + fileName).execute()
            if (response.isSuccessful) {
                emitter.onNext(100)
                emitter.onComplete()
            } else {
                emitter.onError(HttpException(response))
            }
        }
    }

    override fun getStatusOperation(): ResponseOperation {
        val responseOperation =
            ResponseOperation()
        responseOperation.response = ArrayList()
        return responseOperation
    }

    override fun share(id: String, requestExternal: RequestExternal): Observable<ResponseExternal>? = null

    override fun terminate(): Observable<List<Operation>>?  = null

    @Throws(UnsupportedEncodingException::class)
    private fun getExplorer(responseBeans: List<WebDavModel.ResponseBean>, filter: Map<String, String>?): Explorer {
        val explorer = Explorer()
        val filteringValue = filter?.get(ApiContract.Parameters.ARG_FILTER_VALUE)
        val files: MutableList<CloudFile> = ArrayList()
        val folders: MutableList<CloudFolder> = ArrayList()
        val parentFolder = getFolder(responseBeans[0])
        for (i in 1 until responseBeans.size) {
            val bean = responseBeans[i]
            if (bean.isDir && getExtensionFromPath(getTitle(bean.href)).isEmpty()) {
                val folder = CloudFolder()
                folder.id = bean.href
                folder.title = getTitle(bean.href)
                folder.parentId = parentFolder.id
                folder.updated = bean.lastModifiedDate
                folder.etag = bean.etag
                if (filteringValue != null) {
                    if (folder.title.contains(filteringValue, true)) {
                        folders.add(folder)
                    }
                } else {
                    folders.add(folder)
                }
            } else {
                val file = CloudFile()
                file.id = bean.href
                file.title = getTitle(bean.href)
                file.folderId = parentFolder.id
                file.pureContentLength = bean.contentLength?.toLong() ?: 0L
                file.fileExst = getExtensionFromPath(file.title.lowercase())
                file.created = bean.lastModifiedDate
                file.updated = bean.lastModifiedDate
                if (filteringValue != null) {
                    if (file.title.contains(filteringValue, true)) {
                        files.add(file)
                    }
                } else {
                    files.add(file)
                }
            }
        }
        val current = Current()
        current.id = parentFolder.id
        current.filesCount = files.size.toString()
        current.foldersCount = folders.size.toString()
        current.title = parentFolder.title
        explorer.current = current
        explorer.files = files
        explorer.folders = folders
        return sortExplorer(explorer, filter)
    }

    @Throws(UnsupportedEncodingException::class)
    private fun getFolder(responseBean: WebDavModel.ResponseBean): CloudFolder {
        val folder = CloudFolder()
        folder.id = decodeUrl(responseBean.href)
        folder.title = decodeUrl(getFolderTitle(responseBean.href))
        folder.updated = responseBean.lastModifiedDate
        folder.etag = responseBean.etag
        return folder
    }

    private fun getFolderTitle(href: String?): String {
        if (href.isNullOrEmpty()) return ""
        return href.substring(href.lastIndexOf('/', href.length - 2) + 1, href.lastIndexOf('/'))
    }

    @Throws(UnsupportedEncodingException::class)
    private fun getTitle(href: String?): String {
        var title = href!!.substring(href.lastIndexOf('/')).replace("/".toRegex(), "")
        if (title == "") {
            title = getFolderTitle(href)
        }
        title = decodeUrl(title)
        return title
    }

    private fun folderPath(id: String, newName: String): String {
        return StringBuilder().append(filePath(id.substring(0, id.lastIndexOf('/')), newName)).append("/").toString()
    }

    private fun filePath(id: String, newName: String): String {
        return id.substring(0, id.lastIndexOf('/') + 1) +
                newName
    }

    private fun sortExplorer(explorer: Explorer, filter: Map<String, String>?): Explorer {
        val files = explorer.files
        val folders = explorer.folders
        filter?.let {
            val sort = filter[ApiContract.Parameters.ARG_SORT_BY]
            val order = filter[ApiContract.Parameters.ARG_SORT_ORDER]
            when (sort) {
                ApiContract.Parameters.VAL_SORT_BY_SIZE -> {
                    files.sortBy { it.pureContentLength }
                }
                ApiContract.Parameters.VAL_SORT_BY_TITLE -> {
                    folders.sortBy { it.title }
                    files.sortBy { it.title }
                }
                ApiContract.Parameters.VAL_SORT_BY_TYPE -> {
                    files.sortBy { it.fileExst }
                }
                ApiContract.Parameters.VAL_SORT_BY_CREATED -> {
                    files.sortBy { it.created }
                    folders.sortBy { it.created }
                }
            }
            if (order == ApiContract.Parameters.VAL_SORT_ORDER_ASC) {
                files.reverse()
                folders.reverse()
            }
            return explorer.apply {
                this.files = files
                this.folders = folders
            }
        } ?: run {
            return explorer
        }
    }

}