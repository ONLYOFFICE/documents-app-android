package app.editors.manager.mvp.presenters.main

import android.accounts.Account
import android.annotation.SuppressLint
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import app.documents.core.account.AccountPreferences
import app.documents.core.model.cloud.CloudAccount
import app.documents.core.model.cloud.Recent
import app.documents.core.network.common.contracts.ApiContract
import app.documents.core.network.manager.models.explorer.CloudFile
import app.documents.core.network.manager.models.explorer.Current
import app.documents.core.network.manager.models.explorer.Explorer
import app.documents.core.network.manager.models.explorer.allowShare
import app.documents.core.providers.DropboxFileProvider
import app.documents.core.providers.GoogleDriveFileProvider
import app.documents.core.providers.OneDriveFileProvider
import app.editors.manager.R
import app.editors.manager.app.App
import app.editors.manager.app.cloudFileProvider
import app.editors.manager.app.webDavFileProvider
import app.editors.manager.managers.providers.DropboxStorageHelper
import app.editors.manager.managers.providers.GoogleDriveStorageHelper
import app.editors.manager.managers.providers.OneDriveStorageHelper
import app.editors.manager.mvp.views.main.DocsRecentView
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import lib.toolkit.base.managers.utils.AccountUtils
import lib.toolkit.base.managers.utils.ContentResolverUtils.getName
import lib.toolkit.base.managers.utils.EditorsType
import lib.toolkit.base.managers.utils.FileUtils
import lib.toolkit.base.managers.utils.FileUtils.asyncDeletePath
import lib.toolkit.base.managers.utils.PermissionUtils.checkReadWritePermission
import lib.toolkit.base.managers.utils.StringUtils
import moxy.InjectViewState
import moxy.presenterScope
import retrofit2.HttpException
import java.io.File
import java.util.Date
import java.util.Locale
import javax.inject.Inject

sealed class RecentState {
    class RenderList(val recents: List<Recent>) : RecentState()
}

sealed class OpenState(val uri: Uri?, val type: EditorsType?) {
    class Docs(uri: Uri) : OpenState(uri, EditorsType.DOCS)
    class Cells(uri: Uri) : OpenState(uri, EditorsType.CELLS)
    class Slide(uri: Uri) : OpenState(uri, EditorsType.PRESENTATION)
    class Pdf(uri: Uri, val isForm: Boolean) : OpenState(uri, EditorsType.PDF)
    class Media(val explorer: Explorer, val isWebDav: Boolean) : OpenState(null, null)
}

@InjectViewState
class DocsRecentPresenter : DocsBasePresenter<DocsRecentView>() {

    companion object {
        val TAG: String = DocsRecentPresenter::class.java.simpleName
    }

    @Inject
    lateinit var accountPreferences: AccountPreferences

    init {
        App.getApp().appComponent.inject(this)
    }

    private var contextPosition = 0
    private var item: Recent? = null
    private var temp: CloudFile? = null

    override fun onDestroy() {
        super.onDestroy()
        disposable.clear()
    }

    fun getRecentFiles(checkFiles: Boolean = true) {
        presenterScope.launch {
            val list = if (checkFiles) {
                recentDataSource.getRecentList().filter { recent -> checkFiles(recent) }
            } else {
                recentDataSource.getRecentList()
            }

            withContext(Dispatchers.Main) {
                viewState.onRender(RecentState.RenderList(list.sort()))
            }
        }
    }

    private fun checkFiles(recent: Recent): Boolean {
        return if (recent.source == null) {
            val uri = Uri.parse(recent.path)
            if (uri.scheme != null) {
                return if (DocumentFile.fromSingleUri(context, uri)?.exists() == true) {
                    true
                } else {
                    presenterScope.launch {
                        recentDataSource.deleteRecent(recent)
                    }
                    false
                }
            }
            val file = File(recent.path)
            if (file.exists()) {
                true
            } else {
                presenterScope.launch {
                    recentDataSource.deleteRecent(recent)
                }
                false
            }
        } else {
            true
        }
    }

    override fun filter(value: String) {
        filteringValue = value
        presenterScope.launch {
            val list = recentDataSource.getRecentList()
                .filter { recent -> recent.name.contains(value, true) }
                .sort()

            withContext(Dispatchers.Main) {
                updateFiles(list)
            }
        }
    }

    private suspend fun openFile(recent: Recent) {
        cloudDataSource.getAccount(recent.ownerId ?: "")?.let { account ->
            AccountUtils.getToken(
                context,
                Account(account.accountName, context.getString(lib.toolkit.base.R.string.account_type))
            )?.let {
                val fileProvider = context.cloudFileProvider
                disposable.add(
                    fileProvider.fileInfo(CloudFile().apply {
                        id = recent.fileId
                    }).flatMap { cloudFile ->
                        fileProvider.opeEdit(cloudFile, cloudFile.allowShare && !account.isVisitor, null).toObservable()
                            .zipWith(Observable.fromCallable { cloudFile }) { info, file ->
                                return@zipWith arrayOf(file, info)
                            }
                    }.subscribe({ response ->
                        checkExt(response[0] as CloudFile, response[1] as String)
                    }, { throwable ->
                        if (throwable is HttpException) {
                            when (throwable.code()) {
                                ApiContract.HttpCodes.CLIENT_UNAUTHORIZED ->
                                    viewState.onError(context.getString(R.string.errors_client_unauthorized))

                                ApiContract.HttpCodes.CLIENT_FORBIDDEN ->
                                    viewState.onError(context.getString(R.string.error_recent_account))

                                else ->
                                    onErrorHandle(throwable.response()?.errorBody(), throwable.code())
                            }
                        } else {
                            viewState.onError(context.getString(R.string.error_recent_account))
                        }
                    })
                )
            } ?: run {
                viewState.onError(context.getString(R.string.error_recent_enter_account))
            }
        }
    }

    private fun checkExt(file: CloudFile, info: String) {
        if (file.rootFolderType.toInt() != ApiContract.SectionType.CLOUD_TRASH) {
            when (StringUtils.getExtension(file.fileExst)) {
                StringUtils.Extension.DOC, StringUtils.Extension.FORM, StringUtils.Extension.SHEET,
                StringUtils.Extension.PRESENTATION, StringUtils.Extension.IMAGE, StringUtils.Extension.IMAGE_GIF,
                StringUtils.Extension.VIDEO_SUPPORT, StringUtils.Extension.PDF -> {
                    checkSdkVersion { isCheck ->
                        if (isCheck) {
                            viewState.onOpenDocumentServer(/* file = */ file, /* info = */ info, /* type = */ null)
                        } else {
                            downloadTempFile(file, false, null)
                        }
                    }
                }

                else -> viewState.onError(context.getString(R.string.error_unsupported_format))
            }
        } else {
            viewState.onError(context.getString(R.string.error_recent_account))
        }
    }

    private fun addRecent(recent: Recent) {
        presenterScope.launch {
            recentDataSource.updateRecent(recent.copy(date = Date().time))
            getRecentFiles()
        }
    }

    override fun upload(uri: Uri?, uris: List<Uri>?, tag: String?) {
        item?.let { item ->
            if (item.isWebdav) {
                val provider = context.webDavFileProvider

                val file = CloudFile().apply {
                    id = item.fileId
                    title = item.path
                    webUrl = item.path
                    folderId = item.fileId.substring(0, item.fileId.lastIndexOf('/').plus(1))
                    fileExst = StringUtils.getExtensionFromPath(item.name)
                }

                disposable.add(provider.fileInfo(file, false)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .flatMap { cloudFile ->
                        addRecent(cloudFile)
                        return@flatMap provider.upload(cloudFile.folderId, arrayListOf(uri))
                    }.subscribe({}, { error -> fetchError(error) }, {
                        deleteTempFile()
                        viewState.onSnackBar(context.getString(R.string.upload_manager_complete))
                    })
                )
            }
        }
    }

    fun deleteRecent() {
        presenterScope.launch {
            item?.let { recent ->
                recentDataSource.deleteRecent(recent)
                withContext(Dispatchers.Main) {
                    viewState.onDeleteItem(contextPosition)
                }
            }
        }
    }

    private fun getImages(uri: Uri): Explorer {
        val explorer = Explorer()
        val current = Current()
        val explorerFile = CloudFile()
        if (uri.scheme == "content") {
            val clickedFile = DocumentFile.fromSingleUri(context, uri)
            val extension = StringUtils.getExtensionFromPath(clickedFile?.name ?: "")
            explorerFile.pureContentLength = clickedFile?.length() ?: 0
            explorerFile.webUrl = uri.toString()
            explorerFile.fileExst = extension
            explorerFile.title = clickedFile?.name ?: ""
            explorerFile.isClicked = true
            current.title = clickedFile?.name ?: ""
            current.filesCount = "1"
        } else {
            val clickedFile = File(checkNotNull(uri.path))
            val extension = StringUtils.getExtensionFromPath(clickedFile.name)
            explorerFile.pureContentLength = clickedFile.length()
            explorerFile.webUrl = clickedFile.absolutePath
            explorerFile.fileExst = extension
            explorerFile.title = clickedFile.name
            explorerFile.isClicked = true
            current.title = clickedFile.name
            current.filesCount = "1"
        }
        explorer.current = current
        explorer.files = mutableListOf(explorerFile)
        return explorer
    }

    private fun getWebDavImage(recent: Recent): Explorer {
        val explorer = Explorer()
        val extension = StringUtils.getExtensionFromPath(recent.name)
        val explorerFile = CloudFile()
        explorerFile.pureContentLength = recent.size
        //        explorerFile.setId(recent.id)
        explorerFile.fileExst = extension
        explorerFile.title = recent.name
        explorerFile.isClicked = true
        val current = Current()
        current.title = recent.name
        current.filesCount = "1"
        explorer.current = current
        explorer.files = mutableListOf(explorerFile)
        return explorer
    }

    fun onContextClick(recent: Recent, position: Int) {
        item = recent
        contextPosition = position
    }

    fun fileClick(recent: Recent? = item) {
        recent?.let { item = recent }
        item?.let { recentItem ->
            if (recentItem.source == null) {
                recentItem.path.let { path ->
                    Uri.parse(path)?.let { uri ->
                        if (uri.scheme != null) {
                            openLocalFile(uri)
                        } else {
                            openLocalFile(Uri.fromFile(File(path)))
                        }
                        addRecent(recentItem)
                    }
                }
            } else {
                presenterScope.launch {
                    if (checkCloudFile(recentItem)) {
                        addRecent(recentItem)
                    }
                }
            }
        }
    }

    private suspend fun checkCloudFile(recent: Recent): Boolean {
        recent.ownerId?.let { id ->
            cloudDataSource.getAccount(id)?.let { recentAccount ->
                if (recentAccount.id != accountPreferences.onlineAccountId) {
                    withContext(Dispatchers.Main) {
                        viewState.onError(context.getString(R.string.error_recent_enter_account))
                    }
                    return false
                } else if (recentAccount.isWebDav) {
                    openWebDavFile(recent)
                } else if (recentAccount.isDropbox || recentAccount.isGoogleDrive || recentAccount.isOneDrive) {
                    openStorageFile(recent = recent, recentAccount)
                } else {
                    openFile(recent)
                }
                return true
            }
        }
        return false
    }

    private fun openStorageFile(recent: Recent, recentAccount: CloudAccount) {
        when {
            recentAccount.isOneDrive -> OneDriveFileProvider(context, OneDriveStorageHelper())
            recentAccount.isGoogleDrive -> GoogleDriveFileProvider(context, GoogleDriveStorageHelper())
            recentAccount.isDropbox -> DropboxFileProvider(context, DropboxStorageHelper())
            else -> null
        }?.let { provider ->
            showDialogWaiting(TAG_DIALOG_CANCEL_DOWNLOAD)
            val cloudFile = CloudFile().apply {
                title = recent.name
                id = recent.fileId
                fileExst = StringUtils.getExtensionFromPath(recent.name)
                pureContentLength = recent.size
            }
            downloadDisposable = provider.fileInfo(cloudFile)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    { file: CloudFile? ->
                        viewState.onDialogClose()
                        file?.let { addRecent(it) }
                        viewState.onOpenLocalFile(file, null)
                    }
                ) { throwable: Throwable -> fetchError(throwable) }
        } ?: run {
            viewState.onError(context.getString(R.string.error_recent_enter_account))
        }
    }

    private fun openLocalFile(uri: Uri) {
        val name = getName(context, uri)
        when (StringUtils.getExtension(StringUtils.getExtensionFromPath(name.lowercase(Locale.ROOT)))) {
            StringUtils.Extension.DOC, StringUtils.Extension.FORM -> {
                viewState.onOpenFile(OpenState.Docs(uri))
            }

            StringUtils.Extension.SHEET -> viewState.onOpenFile(OpenState.Cells(uri))
            StringUtils.Extension.PRESENTATION -> viewState.onOpenFile(OpenState.Slide(uri))
            StringUtils.Extension.PDF -> viewState.onOpenFile(OpenState.Pdf(uri, FileUtils.isOformPdf(context.contentResolver.openInputStream(uri))))
            StringUtils.Extension.IMAGE, StringUtils.Extension.IMAGE_GIF, StringUtils.Extension.VIDEO_SUPPORT -> {
                viewState.onOpenFile(OpenState.Media(getImages(uri), false))
            }

            else -> viewState.onError(context.getString(R.string.error_unsupported_format))
        }
    }

    private suspend fun openWebDavFile(recent: Recent) {
        cloudDataSource.getAccount(recent.ownerId ?: "")?.let {
            val provider = context.webDavFileProvider
            val cloudFile = CloudFile().apply {
                title = recent.name
                id = recent.fileId
                fileExst = StringUtils.getExtensionFromPath(recent.name)
                pureContentLength = recent.size
            }
            withContext(Dispatchers.Main) {
                if (StringUtils.isImage(cloudFile.fileExst)) {
                    viewState.onOpenFile(OpenState.Media(getWebDavImage(recent), true))
                } else {
                    disposable.add(provider.fileInfo(cloudFile)
                        .doOnSubscribe {
                            viewState.onDialogWaiting(
                                context.getString(R.string.dialogs_wait_title),
                                null
                            )
                        }
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({ file ->
                            temp = file
                            viewState.onDialogClose()
                            openLocalFile(Uri.parse(file.webUrl))
                            getRecentFiles(checkFiles = false)
                        }, ::fetchError)
                    )
                }
            }
        }
    }

    override fun addRecent(file: CloudFile) {
        presenterScope.launch {
            item?.let { recentDataSource.updateRecent(it.copy(date = Date().time)) }
        }
    }

    override fun getNextList() {
        // stub
    }

    override fun createDocs(title: String) {
        // stub
    }

    override fun getFileInfo() {
        // stub
    }

    override fun updateViewsState() {
        // stub
    }

    override fun onActionClick() {
        // stub
    }

    override fun sortBy(sortValue: String): Boolean {
        val isRepeatedTap = preferenceTool.sortBy == sortValue
        preferenceTool.sortBy = sortValue
        if (isRepeatedTap) reverseSortOrder()
        presenterScope.launch(Dispatchers.IO) {
            val list = recentDataSource.getRecentList().sort(sortValue)
            withContext(Dispatchers.Main) {
                updateFiles(list)
            }
        }
        return false
    }

    @SuppressLint("MissingPermission")
    fun deleteTempFile() {
        if (temp != null && checkReadWritePermission(context)) {
            val uri = Uri.parse(temp!!.webUrl)
            if (uri.path != null) {
                asyncDeletePath(uri.path!!)
            }
        }
        temp = null
    }

    fun updateFiles(list: List<Recent>) {
        val sortByUpdated = preferenceTool.sortBy == ApiContract.Parameters.VAL_SORT_BY_UPDATED
        viewState.updateFiles(list, sortByUpdated)
    }

    private fun List<Recent>.sort(sortBy: String? = preferenceTool.sortBy): List<Recent> {
        var sortedList = when (sortBy) {
            ApiContract.Parameters.VAL_SORT_BY_TITLE -> sortedBy { it.name }
            ApiContract.Parameters.VAL_SORT_BY_UPDATED -> sortedBy { it.date }
            ApiContract.Parameters.VAL_SORT_BY_SIZE -> sortedBy { it.size }
            ApiContract.Parameters.VAL_SORT_BY_TYPE -> sortedBy {
                StringUtils.getExtensionFromPath(it.name)
            }

            else -> this
        }
        if (preferenceTool.sortOrder == ApiContract.Parameters.VAL_SORT_ORDER_DESC) {
            sortedList = sortedList.reversed()
        }
        return sortedList
    }

    fun clearRecents() {
        presenterScope.launch {
            recentDataSource.getRecentList().forEach { recent ->
                recentDataSource.deleteRecent(recent)
            }
            withContext(Dispatchers.Main) {
                viewState.onRender(RecentState.RenderList(emptyList()))
            }
        }
    }

}