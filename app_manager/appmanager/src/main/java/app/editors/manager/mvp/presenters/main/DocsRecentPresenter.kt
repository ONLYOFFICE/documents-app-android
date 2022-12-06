package app.editors.manager.mvp.presenters.main

import android.accounts.Account
import android.annotation.SuppressLint
import android.content.ClipData
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import app.documents.core.network.common.contracts.ApiContract
import app.documents.core.network.manager.models.explorer.CloudFile
import app.documents.core.network.manager.models.explorer.Current
import app.documents.core.network.manager.models.explorer.Explorer
import app.documents.core.network.manager.models.explorer.Item
import app.documents.core.providers.DropboxFileProvider
import app.documents.core.providers.GoogleDriveFileProvider
import app.documents.core.providers.OneDriveFileProvider
import app.documents.core.storage.account.CloudAccount
import app.documents.core.storage.recent.Recent
import app.editors.manager.R
import app.editors.manager.app.*
import app.editors.manager.managers.providers.DropboxStorageHelper
import app.editors.manager.managers.providers.GoogleDriveStorageHelper
import app.editors.manager.managers.providers.OneDriveStorageHelper
import app.editors.manager.mvp.views.main.DocsRecentView
import app.editors.manager.ui.dialogs.ContextBottomDialog
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import lib.toolkit.base.managers.utils.AccountUtils
import lib.toolkit.base.managers.utils.ContentResolverUtils.getName
import lib.toolkit.base.managers.utils.FileUtils.asyncDeletePath
import lib.toolkit.base.managers.utils.PermissionUtils.checkReadWritePermission
import lib.toolkit.base.managers.utils.StringUtils
import lib.toolkit.base.managers.utils.TimeUtils
import moxy.InjectViewState
import moxy.presenterScope
import retrofit2.HttpException
import java.io.File
import java.util.*

sealed class RecentState {
    class RenderList(val recents: List<Recent>) : RecentState()
}

sealed class OpenState {
    class Docs(val uri: Uri) : OpenState()
    class Cells(val uri: Uri) : OpenState()
    class Slide(val uri: Uri) : OpenState()
    class Pdf(val uri: Uri) : OpenState()
    class Media(val explorer: Explorer, val isWebDav: Boolean) : OpenState()
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
    private var contextItem: Recent? = null
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
                disposable.add(
                    context.api
                        .getFileInfo(recent.idFile)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .map { response ->
                            {
                                if (response.isSuccessful && response.body() != null) {
                                    response.body()?.response
                                } else {
                                    throw HttpException(response)
                                }
                            }
                        }
                        .subscribe({ response ->
                            checkExt(checkNotNull(response.invoke()))
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

    private fun checkExt(file: CloudFile) {
        if (file.rootFolderType.toInt() != ApiContract.SectionType.CLOUD_TRASH) {
            when (StringUtils.getExtension(file.fileExst)) {
                StringUtils.Extension.DOC, StringUtils.Extension.FORM, StringUtils.Extension.SHEET, StringUtils.Extension.PRESENTATION, StringUtils.Extension.PDF, StringUtils.Extension.IMAGE, StringUtils.Extension.IMAGE_GIF, StringUtils.Extension.VIDEO_SUPPORT -> {
                    viewState.openFile(file)
                }
                else -> viewState.onError(context.getString(R.string.error_unsupported_format))
            }
        } else {
            viewState.onError(context.getString(R.string.error_recent_account))
        }
    }

    fun loadMore(itemCount: Int?) {
//        mAccountsSqlData.getRecent()
    }

    fun setOrder(isAsc: Boolean) {
        if (isAsc) {
            preferenceTool.sortOrder = ApiContract.Parameters.VAL_SORT_ORDER_ASC
        } else {
            preferenceTool.sortOrder = ApiContract.Parameters.VAL_SORT_ORDER_DESC
        }
        update()
    }

    fun reverseOrder() {
        if (preferenceTool.sortOrder == ApiContract.Parameters.VAL_SORT_ORDER_ASC) {
            preferenceTool.sortOrder = ApiContract.Parameters.VAL_SORT_ORDER_DESC
        } else {
            preferenceTool.sortOrder = ApiContract.Parameters.VAL_SORT_ORDER_ASC
        }
        update()
    }

    private fun addRecent(recent: Recent) {
        presenterScope.launch {
            recentDao.updateRecent(recent.copy(date = Date().time))
            getRecentFiles()
        }
    }

    fun contextClick(recent: Recent, position: Int) {
        presenterScope.launch {
            contextItem = recent
            contextPosition = position
            val state = ContextBottomDialog.State()
            state.title = recent.name
            if (!recent.isLocal) {
                accountDao.getAccount(recent.ownerId ?: "")?.let {
                    state.info =
                        it.portal + context.getString(R.string.placeholder_point) + TimeUtils.formatDate(Date(recent.date))
                }

            } else {
                state.info = TimeUtils.formatDate(Date(recent.date))
            }
            state.iconResId = getIconContext(StringUtils.getExtensionFromPath(recent.name))
            state.isRecent = true
            if (recent.isLocal) {
                state.isLocal = true
            }
            withContext(Dispatchers.Main) {
                viewState.onContextShow(state)
            }
        }
    }

    override fun upload(uri: Uri?, uris: ClipData?) {
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
            contextItem?.let {
                recentDao.deleteRecent(it)
                withContext(Dispatchers.Main) {
                    viewState.onDeleteItem(contextPosition)
                }
            }
        }
    }

    private fun getImages(clickedFile: File): Explorer {
        val explorer = Explorer()
        val extension = StringUtils.getExtensionFromPath(clickedFile.name)
        val explorerFile = CloudFile()
        explorerFile.pureContentLength = clickedFile.length()
        explorerFile.webUrl = clickedFile.absolutePath
        explorerFile.fileExst = extension
        explorerFile.title = clickedFile.name
        explorerFile.isClicked = true
        val current = Current()
        current.title = clickedFile.name
        current.filesCount = "1"
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

    fun fileClick(recent: Recent, position: Int) {
        item = recent
        if (recent.isLocal) {
            recent.path?.let { path ->
                Uri.parse(path)?.let {
                    if (it.scheme != null) {
                        openLocalFile(it)
                    } else {
                        openLocalFile(Uri.fromFile(File(path)))
                    }
                    addRecent(recent)
                }
            }
        } else {
            presenterScope.launch {
                if (checkCloudFile(recent, position)) {
                    addRecent(recent)
                }
            }
        }
    }

    private suspend fun checkCloudFile(recent: Recent, position: Int): Boolean {
        recent.ownerId?.let { id ->
            accountDao.getAccount(id)?.let { recentAccount ->
                if (!recentAccount.isOnline) {
                    withContext(Dispatchers.Main) {
                        viewState.onError(context.getString(R.string.error_recent_enter_account))
                    }
                    return false
                } else if (recentAccount.isWebDav) {
                    openWebDavFile(recent, position)
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

    private fun openLocalFile(uri: Uri) {
        val name = getName(context, uri)
        when (StringUtils.getExtension(StringUtils.getExtensionFromPath(name.lowercase(Locale.ROOT)))) {
            StringUtils.Extension.DOC, StringUtils.Extension.FORM -> viewState.onOpenFile(OpenState.Docs(uri))
            StringUtils.Extension.SHEET -> viewState.onOpenFile(OpenState.Cells(uri))
            StringUtils.Extension.PRESENTATION -> viewState.onOpenFile(OpenState.Slide(uri))
            StringUtils.Extension.PDF -> viewState.onOpenFile(OpenState.Pdf(uri))
            StringUtils.Extension.IMAGE, StringUtils.Extension.IMAGE_GIF, StringUtils.Extension.VIDEO_SUPPORT -> {
                viewState.onOpenFile(OpenState.Media(getImages(File(checkNotNull(uri.path))), false))
            }
            else -> viewState.onError(context.getString(R.string.error_unsupported_format))
        }
    }

    private suspend fun openWebDavFile(recent: Recent, position: Int) {
        accountDao.getAccount(recent.ownerId ?: "")?.let { account ->
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

    override fun onContextClick(item: Item, position: Int, isTrash: Boolean) {
        // stub
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

    fun update(sortBy: String = preferenceTool.sortBy.orEmpty()) {
        preferenceTool.sortBy = sortBy
        presenterScope.launch {
            val list = recentDao.getRecents().sort()
            withContext(Dispatchers.Main) {
                updateFiles(list)
            }
        }
    }

    fun updateFiles(list: List<Recent>) {
        val sortBy = preferenceTool.sortBy.orEmpty()
        val sortOrder = preferenceTool.sortOrder.orEmpty()

        viewState.updateFiles(list, sortBy, sortOrder)
    }

    private fun List<Recent>.sort(sortBy: String = preferenceTool.sortBy.orEmpty()): List<Recent> {
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

}