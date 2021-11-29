package app.editors.manager.managers.providers

import android.content.Context
import android.net.Uri
import android.os.Build
import app.editors.manager.managers.providers.ProviderError.Companion.throwErrorCreate
import app.editors.manager.app.App.Companion.getLocale
import lib.toolkit.base.managers.utils.StringUtils.getExtensionFromPath
import app.editors.manager.managers.providers.ProviderError.Companion.throwExistException
import app.editors.manager.managers.providers.ProviderError.Companion.throwUnsupportedException
import lib.toolkit.base.managers.tools.LocalContentTools
import app.editors.manager.mvp.models.request.RequestCreate
import app.documents.core.network.ApiContract
import app.editors.manager.mvp.models.base.Base
import app.editors.manager.mvp.models.explorer.*
import app.editors.manager.mvp.models.request.RequestExternal
import app.editors.manager.mvp.models.response.ResponseOperation
import app.editors.manager.mvp.models.request.RequestFavorites
import app.editors.manager.mvp.models.response.ResponseExternal
import io.reactivex.Observable
import lib.toolkit.base.managers.utils.PathUtils
import java.io.File
import java.io.IOException
import java.lang.Exception
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.*

class LocalFileProvider(private val mLocalContentTools: LocalContentTools) : BaseFileProvider {

    override fun getFiles(id: String, filter: Map<String, String>?): Observable<Explorer> {
        return Observable.just(mLocalContentTools.createRootDir())
            .map<List<File?>> { file: File ->
                if (file.exists()) {
                    return@map mLocalContentTools.getFiles(File(id))
                } else {
                    throw throwErrorCreate()
                }
            }
            .flatMap { files: List<File?> -> Observable.just(getExplorer(files, File(id))) }
            .map { explorer: Explorer -> sortExplorer(explorer, filter) }
            .flatMap { explorer: Explorer ->
                filter?.let {
                    if (it.containsKey("filterValue")) {
                        return@flatMap getFilterExplorer(explorer, it["filterValue"])
                    }
                }
                Observable.just(explorer)
            }
    }

    override fun createFile(folderId: String, body: RequestCreate): Observable<CloudFile> {
        val parentFile = File(folderId)
        val name = body.title
        return try {
            val localFile = mLocalContentTools.createFile(name, parentFile, getLocale())
            Observable.just(localFile)
                .map { createFile: File ->
                    if (createFile.exists()) {
                        val file = CloudFile()
                        file.id = folderId + "/" + createFile.name
                        file.title = createFile.name
                        file.folderId = folderId
                        file.pureContentLength = createFile.length()
                        file.fileExst = getExtensionFromPath(name)
                        file.created = Date()
                        file.isJustCreated = true
                        return@map file
                    } else {
                        throw throwErrorCreate()
                    }
                }
        } catch (error: Throwable) {
            Observable.just(CloudFile())
                .map { file: CloudFile? -> throw throwErrorCreate() }
        }
    }

    override fun search(query: String?): Observable<String>? = null

    override fun createFolder(folderId: String, body: RequestCreate): Observable<CloudFolder> {
        val parentFile = File(folderId)
        val name = body.title
        return Observable.just(mLocalContentTools.createFolder(name, parentFile))
            .map { isCreate: Boolean ->
                if (isCreate) {
                    val folder = CloudFolder()
                    folder.id = "$folderId/$name"
                    folder.title = name
                    folder.parentId = folderId
                    folder.created = Date()
                    folder.isJustCreated = true
                    return@map folder
                } else {
                    throw throwErrorCreate()
                }
            }
    }

    override fun rename(item: Item, newName: String, version: Int?): Observable<Item> {
        val oldFile = File(item.id)
        return Observable.just(mLocalContentTools.renameFile(oldFile, newName))
            .map { isRename: Boolean ->
                if (isRename) {
                    item.id = item.id.replace(item.title, "") + newName
                    if (item is CloudFile) {
                        item.setTitle(newName + item.fileExst)
                    } else {
                        item.title = newName
                    }
                    return@map item
                } else {
                    throw Exception("Error rename")
                }
            }
    }

    override fun delete(items: List<Item>, from: CloudFolder?): Observable<List<Operation>> {
        return Observable.fromIterable(items)
            .map { item: Item? ->
                mLocalContentTools.deleteFile(
                    File(
                        item?.id
                    )
                )
            }
            .toList()
            .toObservable()
            .map { booleans: List<Boolean> ->
                if (booleans.isNotEmpty()) {
                    val operation = Operation()
                    operation.progress = 100
                    return@map listOf(operation)
                } else {
                    throw Exception("Error delete")
                }
            }
    }

    override fun transfer(
        items: List<Item>,
        to: CloudFolder?,
        conflict: Int,
        isMove: Boolean,
        isOverwrite: Boolean
    ): Observable<List<Operation>>? = null

    override fun fileInfo(item: Item?): Observable<CloudFile> {
        (item as CloudFile?)?.fileStatus = java.lang.String.valueOf(ApiContract.FileStatus.NONE)
        return Observable.just(item)
    }

    // Stub to local
    override fun getStatusOperation(): ResponseOperation? = null

    override fun download(items: List<Item>): Observable<Int>? = null
    override fun share(
        id: String,
        requestExternal: RequestExternal
    ): Observable<ResponseExternal>? = null

    override fun terminate(): Observable<List<Operation>>? = null

    override fun addToFavorites(fileId: RequestFavorites): Observable<Base>? = null

    override fun deleteFromFavorites(requestFavorites: RequestFavorites): Observable<Base>? = null

    override fun upload(folderId: String, uris: List<Uri?>): Observable<Int>? = null

    fun import(context: Context, folderId: String, uris: List<Uri?>):Observable<Int> {
        val folder = File(folderId)
        return Observable.fromIterable(uris).map { uri ->
            val path = PathUtils.getPath(context, uri)
            val file = File(Uri.parse(path).path)
            mLocalContentTools.moveFiles(file, folder, true)
            1
        }
    }

    @Throws(Exception::class)
    fun transfer(path: String?, clickedItem: Item?, isCopy: Boolean): Boolean {
        return if (path != null) {
            val file = File(clickedItem?.id)
            val parentFile = File(Uri.parse(path).path)
            if (parentFile == file || parentFile.absolutePath.contains(file.absolutePath)) {
                throw throwExistException()
            }
            mLocalContentTools.moveFiles(file, parentFile, isCopy)
        } else {
            throw throwUnsupportedException()
        }
    }

    private fun getExplorer(localFiles: List<File?>, parent: File): Explorer {
        val explorer = Explorer()
        val files: MutableList<CloudFile> = ArrayList()
        val folders: MutableList<CloudFolder> = ArrayList()
        for (convertFile in localFiles) {
            if (convertFile?.isDirectory == true) {
                val folder = CloudFolder()
                folder.parentId = convertFile.parentFile.absolutePath
                folder.id = convertFile.absolutePath
                folder.title = convertFile.name
                folder.updated = Date(convertFile.lastModified())
                folders.add(folder)
            } else {
                val file = CloudFile()
                file.id = convertFile?.absolutePath
                file.title = convertFile?.name
                file.fileExst = convertFile?.name?.let { name ->
                    getExtensionFromPath(name)
                }
                file.pureContentLength = convertFile?.length()!!
                file.folderId = convertFile.parentFile.absolutePath
                file.webUrl = convertFile.absolutePath
                file.updated = Date(convertFile.lastModified())
                files.add(file)
            }
        }
        val current = Current()
        current.id = parent.absolutePath
        current.title = parent.name
        current.filesCount = files.size.toString()
        current.foldersCount = folders.size.toString()
        explorer.files = files
        explorer.folders = folders
        explorer.current = current
        return explorer
    }

    @Throws(IOException::class)
    private fun getFilterExplorer(explorer: Explorer, value: String?): Observable<Explorer> {
        return if (value?.isNotEmpty() == true) {
            Observable.just(search(value, explorer.current.id))
        } else {
            Observable.just(explorer)
        }
    }

    @Throws(IOException::class)
    private fun search(value: String?, id: String): Explorer {
        val files: MutableList<File?> = mutableListOf()
        var resultExplorer = Explorer()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Files.walk(Paths.get(id))
                .filter { path: Path? -> Files.isReadable(path) }
                .forEach { foundFile: Path ->
                    if (foundFile.toFile().name.toLowerCase().contains(
                            value?.toLowerCase().toString()
                        )
                    ) {
                        files.add(foundFile.toFile())
                    }
                }
            resultExplorer = getExplorer(files, File(id))
        } else {
            var tempExplorer = Explorer()
            val root = File(id)
            val listFiles = root.listFiles()
            for (item in listFiles) {
                if (item.name.toLowerCase().contains(value?.toLowerCase().toString())) {
                    files.add(item)
                    tempExplorer = getExplorer(files, File(id))
                }
                if (item.isDirectory) {
                    tempExplorer = search(value, item.absolutePath)
                }
                resultExplorer.add(tempExplorer)
            }
        }
        return resultExplorer
    }

    private fun sortExplorer(explorer: Explorer, filter: Map<String, String>?): Explorer {
        val folders = explorer.folders
        val files = explorer.files
        filter?.let {
            val sort = it[ApiContract.Parameters.ARG_SORT_BY]
            val order = it[ApiContract.Parameters.ARG_SORT_ORDER]
            when (sort) {
                ApiContract.Parameters.VAL_SORT_BY_UPDATED -> folders.sortWith(Comparator { o1: CloudFolder, o2: CloudFolder ->
                    o1.updated.compareTo(
                        o2.updated
                    )
                })
                ApiContract.Parameters.VAL_SORT_BY_TITLE -> {
                    folders.sortWith(Comparator { o1: CloudFolder, o2: CloudFolder ->
                        o1.title.toLowerCase().compareTo(o2.title.toLowerCase())
                    })
                    files.sortWith(Comparator { o1: CloudFile, o2: CloudFile ->
                        o1.title.toLowerCase().compareTo(o2.title.toLowerCase())
                    })
                }
                ApiContract.Parameters.VAL_SORT_BY_SIZE -> files.sortWith(Comparator { o1: CloudFile, o2: CloudFile ->
                    o1.pureContentLength.compareTo(o2.pureContentLength)
                })
                ApiContract.Parameters.VAL_SORT_BY_TYPE -> files.sortWith(Comparator { o1: CloudFile, o2: CloudFile ->
                    o1.fileExst.compareTo(
                        o2.fileExst
                    )
                })
            }
            if (order == ApiContract.Parameters.VAL_SORT_ORDER_DESC) {
                folders.reverse()
                files.reverse()
            }
        }
        explorer.folders = folders
        explorer.files = files
        return explorer
    }
}