package app.editors.manager.managers.providers

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import app.documents.core.network.ApiContract
import app.editors.manager.app.App.Companion.getLocale
import app.editors.manager.managers.providers.ProviderError.Companion.throwErrorCreate
import app.editors.manager.managers.providers.ProviderError.Companion.throwExistException
import app.editors.manager.managers.providers.ProviderError.Companion.throwUnsupportedException
import app.editors.manager.mvp.models.base.Base
import app.editors.manager.mvp.models.explorer.*
import app.editors.manager.mvp.models.request.RequestCreate
import app.editors.manager.mvp.models.request.RequestExternal
import app.editors.manager.mvp.models.request.RequestFavorites
import app.editors.manager.mvp.models.response.ResponseExternal
import app.editors.manager.mvp.models.response.ResponseOperation
import io.reactivex.Observable
import lib.toolkit.base.managers.tools.LocalContentTools
import lib.toolkit.base.managers.utils.FileUtils
import lib.toolkit.base.managers.utils.StringUtils.getExtensionFromPath
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Paths
import java.util.*
import kotlin.io.path.name

class LocalFileProvider(private val localContentTools: LocalContentTools) : BaseFileProvider {

    override fun getFiles(id: String?, filter: Map<String, String>?): Observable<Explorer> {
        return Observable.just(localContentTools.createRootDir())
            .map<List<File?>> { file: File ->
                if (file.exists()) {
                    return@map localContentTools.getFiles(File(checkNotNull(id)))
                } else {
                    throw throwErrorCreate()
                }
            }
            .flatMap { files: List<File?> -> Observable.just(getExplorer(files, File(checkNotNull(id)))) }
            .flatMap { explorer: Explorer ->
                getFilterExplorer(explorer, filter?.get(ApiContract.Parameters.ARG_FILTER_VALUE))
            }
            .map { explorer: Explorer -> sortExplorer(explorer, filter) }
    }

    override fun createFile(folderId: String, body: RequestCreate): Observable<CloudFile> {
        val parentFile = File(folderId)
        val name = body.title
        return try {
            val localFile = localContentTools.createFile(name, parentFile, getLocale())
            Observable.just(localFile)
                .map { createFile: File ->
                    if (createFile.exists()) {
                        val file = CloudFile()
                        file.id = folderId + "/" + createFile.name
                        file.webUrl = file.id
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
                .map { throw throwErrorCreate() }
        }
    }

    override fun search(query: String?): Observable<String>? = null

    override fun createFolder(folderId: String, body: RequestCreate): Observable<CloudFolder> {
        val parentFile = File(folderId)
        val name = body.title
        return Observable.just(localContentTools.createFolder(name, parentFile))
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
        return Observable.just(localContentTools.renameFile(oldFile, newName))
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
                localContentTools.deleteFile(File(checkNotNull(item?.id)))
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

    override fun upload(folderId: String, uris: List<Uri?>): Observable<Int>? = null

    fun import(context: Context, folderId: String, uri: Uri?): Observable<Int> {
        val folder = File(folderId)
        return Observable.just(uri).map { file ->
            val docFile = DocumentFile.fromSingleUri(context, file)
            return@map if (FileUtils.copyFile(context, file, "${folder.path}/${docFile?.name}")) 1 else 0
        }
    }

    @Throws(Exception::class)
    fun transfer(path: String?, clickedItem: Item?, isCopy: Boolean): Boolean {
        return if (path != null) {
            val file = File(checkNotNull(clickedItem?.id))
            val parentFile = File(checkNotNull(Uri.parse(path).path))
            if (parentFile == file || parentFile.absolutePath.contains(file.absolutePath)) {
                throw throwExistException()
            }
            localContentTools.moveFiles(file, parentFile, isCopy)
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
                folder.parentId = checkNotNull(convertFile.parentFile).absolutePath
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
                file.folderId = checkNotNull(convertFile.parentFile).absolutePath
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

    private fun search(value: String?, id: String): Explorer {
        val files: MutableList<File?> = mutableListOf()
        val resultExplorer = Explorer()
        var tempExplorer: Explorer?
        Files.walk(Paths.get(id)).use { walkStream ->
            walkStream.filter { item -> item.name.contains(value.toString(), true) }
                .forEach { item ->
                    files.add(item.toFile())
                    tempExplorer = getExplorer(files, File(id))
                    resultExplorer.add(tempExplorer)
                }
        }

        if (resultExplorer.count == 0) {
            val current = Current()
            current.id = id
            resultExplorer.current = current
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
                ApiContract.Parameters.VAL_SORT_BY_UPDATED -> folders.sortWith { o1: CloudFolder, o2: CloudFolder ->
                    o1.updated.compareTo(
                        o2.updated
                    )
                }
                ApiContract.Parameters.VAL_SORT_BY_TITLE -> {
                    folders.sortWith { o1: CloudFolder, o2: CloudFolder ->
                        o1.title.lowercase(Locale.getDefault()).compareTo(o2.title.lowercase(Locale.getDefault()))
                    }
                    files.sortWith { o1: CloudFile, o2: CloudFile ->
                        o1.title.lowercase(Locale.getDefault()).compareTo(o2.title.lowercase(Locale.getDefault()))
                    }
                }
                ApiContract.Parameters.VAL_SORT_BY_SIZE -> files.sortWith { o1: CloudFile, o2: CloudFile ->
                    o1.pureContentLength.compareTo(o2.pureContentLength)
                }
                ApiContract.Parameters.VAL_SORT_BY_TYPE -> files.sortWith { o1: CloudFile, o2: CloudFile ->
                    o1.fileExst.compareTo(
                        o2.fileExst
                    )
                }
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