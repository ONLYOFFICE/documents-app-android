package app.editors.manager.mvp.presenters.main

import android.accounts.Account
import android.annotation.SuppressLint
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import app.documents.core.network.common.contracts.ApiContract
import app.documents.core.network.manager.models.explorer.CloudFile
import app.documents.core.network.manager.models.explorer.Current
import app.documents.core.network.manager.models.explorer.Explorer
import app.documents.core.providers.CloudFileProvider
import app.documents.core.providers.DropboxFileProvider
import app.documents.core.providers.GoogleDriveFileProvider
import app.documents.core.providers.OneDriveFileProvider
import app.documents.core.storage.account.CloudAccount
import app.documents.core.storage.recent.Recent
import app.editors.manager.BuildConfig
import app.editors.manager.R
import app.editors.manager.app.*
import app.editors.manager.managers.providers.DropboxStorageHelper
import app.editors.manager.managers.providers.GoogleDriveStorageHelper
import app.editors.manager.managers.providers.OneDriveStorageHelper
import app.editors.manager.mvp.views.main.DocsRecentView
import app.editors.manager.ui.popup.MainPopup
import app.editors.manager.ui.popup.MainPopupItem
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import lib.toolkit.base.managers.utils.AccountUtils
import lib.toolkit.base.managers.utils.ContentResolverUtils.getName
import lib.toolkit.base.managers.utils.EditorsType
import lib.toolkit.base.managers.utils.FileUtils.asyncDeletePath
import lib.toolkit.base.managers.utils.PermissionUtils.checkReadWritePermission
import lib.toolkit.base.managers.utils.StringUtils
import moxy.InjectViewState
import moxy.presenterScope
import retrofit2.HttpException
import java.io.File
import java.util.*

sealed class RecentState {
    class RenderList(val recents: List<Recent>) : RecentState()
}

sealed class OpenState(val uri: Uri?, val type: EditorsType?) {
    class Docs(uri: Uri) : OpenState(uri, EditorsType.DOCS)
    class Cells(uri: Uri) : OpenState(uri, EditorsType.CELLS)
    class Slide(uri: Uri) : OpenState(uri, EditorsType.PRESENTATION)
    class Pdf(uri: Uri) : OpenState(uri, EditorsType.PDF)
    class Media(val explorer: Explorer, val isWebDav: Boolean) : OpenState(null, null)
}

@InjectViewState
class DocsRecentPresenter : DocsBasePresenter<DocsRecentView>() {

    companion object {
        val TAG: String = DocsRecentPresenter::class.java.simpleName
    }

    init {
        App.getApp().appComponent.inject(this)
    }

    private var contextPosition = 0
    private var item: Recent? = null
    private var temp: CloudFile? = null
    private val account: CloudAccount? = getAccount()

    private fun getAccount(): CloudAccount? = runBlocking(Dispatchers.Default) {
        accountDao.getAccountOnline()?.let { account ->
            if (account.isWebDav) {
                AccountUtils.getPassword(
                    context,
                    Account(account.getAccountName(), context.getString(lib.toolkit.base.R.string.account_type))
                )?.let {
                    return@runBlocking account
                }
            } else {
                AccountUtils.getToken(
                    context,
                    Account(account.getAccountName(), context.getString(lib.toolkit.base.R.string.account_type))
                )?.let {
                    return@runBlocking account
                }
            }
        } ?: run {
            return@runBlocking null
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        disposable.clear()
    }


    fun getRecentFiles(checkFiles: Boolean = true) {
        presenterScope.launch {
            val list = if (checkFiles) {
                recentDao.getRecents().filter { recent -> checkFiles(recent) }
            } else {
                recentDao.getRecents()
            }
            withContext(Dispatchers.Main) {
                viewState.onRender(RecentState.RenderList(list.sort()))
            }
        }
    }

    private fun checkFiles(recent: Recent): Boolean {
        return if (recent.isLocal) {
            val uri = Uri.parse(recent.path)
            if (uri.scheme != null) {
                return if (DocumentFile.fromSingleUri(context, uri)?.exists() == true) {
                    true
                } else {
                    presenterScope.launch {
                        recentDao.deleteRecent(recent)
                    }
                    false
                }
            }
            val file = File(recent.path ?: "")
            if (file.exists()) {
                true
            } else {
                presenterScope.launch {
                    recentDao.deleteRecent(recent)
                }
                false
            }
        } else {
            true
        }
    }

    fun searchRecent(newText: String?) {
        presenterScope.launch {
            val list = recentDao.getRecents()
                .filter { it.name.contains(newText ?: "", true) }
                .sort()

            withContext(Dispatchers.Main) {
                updateFiles(list)
            }
        }
    }

    private suspend fun openFile(recent: Recent) {
        accountDao.getAccount(recent.ownerId ?: "")?.let { account ->
            AccountUtils.getToken(
                context,
                Account(account.getAccountName(), context.getString(lib.toolkit.base.R.string.account_type))
            )?.let {
                val fileProvider = context.cloudFileProvider
                disposable.add(
                    fileProvider.fileInfo(CloudFile().apply {
                        id = recent.idFile ?: ""
                    }).flatMap { cloudFile ->
                        fileProvider.opeEdit(cloudFile).toObservable()
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

    @Suppress("KotlinConstantConditions")
    private fun checkExt(file: CloudFile, info: String) {
        if (file.rootFolderType.toInt() != ApiContract.SectionType.CLOUD_TRASH) {
            when (val ext = StringUtils.getExtension(file.fileExst)) {
                StringUtils.Extension.DOC, StringUtils.Extension.FORM, StringUtils.Extension.SHEET, StringUtils.Extension.PRESENTATION, StringUtils.Extension.IMAGE, StringUtils.Extension.IMAGE_GIF, StringUtils.Extension.VIDEO_SUPPORT -> {
                    if (BuildConfig.APPLICATION_ID != "com.onlyoffice.documents" && ext == StringUtils.Extension.FORM) {
                        viewState.onError(context.getString(R.string.error_unsupported_format))
                    } else {
                        checkSdkVersion { isCheck ->
                            if (isCheck) {
                                viewState.onOpenDocumentServer(file, info, false)
                            } else {
                                viewState.openFile(file)
                            }
                        }
                    }
                }

                StringUtils.Extension.PDF -> {
                    viewState.openFile(file)
                }

                else -> viewState.onError(context.getString(R.string.error_unsupported_format))
            }
        } else {
            viewState.onError(context.getString(R.string.error_recent_account))
        }
    }

    private fun addRecent(recent: Recent) {
        presenterScope.launch {
            recentDao.updateRecent(recent.copy(date = Date().time))
            getRecentFiles()
        }
    }

    override fun upload(uri: Uri?, uris: List<Uri>?, tag: String?) {
        item?.let { item ->
            if (item.isWebDav) {
                val provider = context.webDavFileProvider

                val file = CloudFile().apply {
                    id = item.idFile.orEmpty()
                    title = item.path.orEmpty()
                    webUrl = item.path.orEmpty()
                    folderId = item.idFile?.substring(0, item.idFile?.lastIndexOf('/')?.plus(1) ?: -1).orEmpty()
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
                recentDao.deleteRecent(recent)
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
            if (recentItem.isLocal) {
                recentItem.path?.let { path ->
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
            accountDao.getAccount(id)?.let { recentAccount ->
                if (!recentAccount.isOnline) {
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
                id = recent.idFile.orEmpty()
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
                        viewState.onOpenLocalFile(file)
                    }
                ) { throwable: Throwable -> fetchError(throwable) }
        } ?: run {
            viewState.onError(context.getString(R.string.error_recent_enter_account))
        }
    }

    @Suppress("KotlinConstantConditions")
    private fun openLocalFile(uri: Uri) {
        val name = getName(context, uri)
        when (val ext = StringUtils.getExtension(StringUtils.getExtensionFromPath(name.lowercase(Locale.ROOT)))) {
            StringUtils.Extension.DOC, StringUtils.Extension.FORM -> {
                if (BuildConfig.APPLICATION_ID != "com.onlyoffice.documents" && ext == StringUtils.Extension.FORM) {
                    viewState.onError(context.getString(R.string.error_unsupported_format))
                } else {
                    viewState.onOpenFile(OpenState.Docs(uri))
                }
            }
            StringUtils.Extension.SHEET -> viewState.onOpenFile(OpenState.Cells(uri))
            StringUtils.Extension.PRESENTATION -> viewState.onOpenFile(OpenState.Slide(uri))
            StringUtils.Extension.PDF -> viewState.onOpenFile(OpenState.Pdf(uri))
            StringUtils.Extension.IMAGE, StringUtils.Extension.IMAGE_GIF, StringUtils.Extension.VIDEO_SUPPORT -> {
                viewState.onOpenFile(OpenState.Media(getImages(uri), false))
            }
            else -> viewState.onError(context.getString(R.string.error_unsupported_format))
        }
    }

    private suspend fun openWebDavFile(recent: Recent) {
        accountDao.getAccount(recent.ownerId ?: "")?.let {
            val provider = context.webDavFileProvider
            val cloudFile = CloudFile().apply {
                title = recent.name
                id = recent.idFile.orEmpty()
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
            item?.let {
                recentDao.updateRecent(it.copy(date = Date().time))
            }
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

    override fun sortBy(type: MainPopupItem.SortBy): Boolean {
        val isRepeatedTap = MainPopup.getSortPopupItem(preferenceTool.sortBy) == type
        preferenceTool.sortBy = type.value
        if (isRepeatedTap) {
            reverseSortOrder()
        }
        presenterScope.launch(Dispatchers.IO) {
            val list = recentDao.getRecents().sort(type.value)
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
            recentDao.getRecents().forEach { recent ->
                recentDao.deleteRecent(recent)
            }
            withContext(Dispatchers.Main) {
                viewState.onRender(RecentState.RenderList(emptyList()))
            }
        }
    }

}