package app.editors.manager.mvp.presenters.main

import android.accounts.Account
import android.annotation.SuppressLint
import android.content.ClipData
import android.net.Uri
import app.documents.core.account.CloudAccount
import app.documents.core.account.Recent
import app.documents.core.network.ApiContract
import app.documents.core.webdav.WebDavApi
import app.editors.manager.R
import app.editors.manager.app.App
import app.editors.manager.app.api
import app.editors.manager.app.webDavApi
import app.editors.manager.managers.providers.WebDavFileProvider
import app.editors.manager.mvp.models.explorer.CloudFile
import app.editors.manager.mvp.models.explorer.Current
import app.editors.manager.mvp.models.explorer.Explorer
import app.editors.manager.mvp.models.explorer.Item
import app.editors.manager.mvp.models.models.ModelExplorerStack
import app.editors.manager.mvp.views.main.DocsRecentView
import app.editors.manager.ui.dialogs.ContextBottomDialog
import app.editors.manager.ui.views.custom.PlaceholderViews
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.*
import lib.toolkit.base.managers.utils.AccountUtils
import lib.toolkit.base.managers.utils.ContentResolverUtils.getName
import lib.toolkit.base.managers.utils.FileUtils.asyncDeletePath
import lib.toolkit.base.managers.utils.PermissionUtils.checkReadWritePermission
import lib.toolkit.base.managers.utils.StringUtils
import lib.toolkit.base.managers.utils.TimeUtils
import moxy.InjectViewState
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
        mModelExplorerStack = ModelExplorerStack()
        mFilteringValue = ""
        mPlaceholderType = PlaceholderViews.Type.NONE
        mIsContextClick = false
        mIsFilteringMode = false
        mIsSelectionMode = false
        mIsFoldersMode = false
    }

    private var contextPosition = 0
    private var item: Recent? = null
    private var contextItem: Recent? = null
    private var temp: CloudFile? = null
    private val disposable = CompositeDisposable()
    private val account: CloudAccount? = getAccount()

    private fun getAccount(): CloudAccount? = runBlocking(Dispatchers.Default) {
        accountDao?.getAccountOnline()?.let { account ->
            if (account.isWebDav) {
                AccountUtils.getPassword(
                    mContext,
                    Account(account.getAccountName(), mContext.getString(lib.toolkit.base.R.string.account_type))
                )?.let {
                    return@runBlocking account
                }
            } else {
                AccountUtils.getToken(
                    mContext,
                    Account(account.getAccountName(), mContext.getString(lib.toolkit.base.R.string.account_type))
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

    fun getRecentFiles() {
        CoroutineScope(Dispatchers.Default).launch {
            val list = recentDao.getRecents().filter { recent -> checkFiles(recent) }
                .sortedByDescending { it.date }
            withContext(Dispatchers.Main) {
                viewState.onRender(RecentState.RenderList(list))
            }
        }
    }

    private suspend fun checkFiles(recent: Recent): Boolean {
        return if (recent.isLocal) {
            val file = File(recent.path ?: "")
            if (file.exists()) {
                true
            } else {
                recentDao.deleteRecent(recent)
                false
            }
        } else {
            true
        }
    }

    fun searchRecent(newText: String?) {
        CoroutineScope(Dispatchers.Default).launch {
            val list = recentDao.getRecents().filter { it.name.contains(newText ?: "", true) }
            withContext(Dispatchers.Main) {
                viewState.updateFiles(list)
            }
        }
    }

    private suspend fun openFile(recent: Recent) {
        accountDao.getAccount(recent.ownerId!!)?.let { account ->
            AccountUtils.getToken(
                mContext,
                Account(account.getAccountName(), mContext.getString(lib.toolkit.base.R.string.account_type))
            )?.let { it ->
                disposable.add(
                    mContext.api()
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
                                        viewState.onError(mContext.getString(R.string.errors_client_unauthorized))
                                    ApiContract.HttpCodes.CLIENT_FORBIDDEN ->
                                        viewState.onError(mContext.getString(R.string.error_recent_account))
                                    else ->
                                        onErrorHandle(throwable.response()?.errorBody(),throwable.code())
                                }
                            } else {
                                viewState.onError(mContext.getString(R.string.error_recent_account))
                            }
                        })
                )
            } ?: run {
                viewState.onError(mContext.getString(R.string.error_recent_enter_account))
            }
        }
    }

    private fun checkExt(file: CloudFile) {
        if (file.rootFolderType.toInt() != ApiContract.SectionType.CLOUD_TRASH) {
            when (StringUtils.getExtension(file.fileExst)) {
                StringUtils.Extension.DOC, StringUtils.Extension.SHEET, StringUtils.Extension.PRESENTATION, StringUtils.Extension.PDF, StringUtils.Extension.IMAGE, StringUtils.Extension.IMAGE_GIF, StringUtils.Extension.VIDEO_SUPPORT -> {
                    viewState.openFile(file)
                }
                else -> viewState.onError(mContext.getString(R.string.error_unsupported_format))
            }
        } else {
            viewState.onError(mContext.getString(R.string.error_recent_account))
        }
    }

    fun loadMore(itemCount: Int?) {
//        mAccountsSqlData.getRecent()
    }

    override fun sortBy(parameters: String, isAscending: Boolean): Boolean {
        mPreferenceTool.sortBy = parameters
        setOrder(isAscending)
        when (parameters) {
            ApiContract.Parameters.VAL_SORT_BY_CREATED -> sortByCreated()
            ApiContract.Parameters.VAL_SORT_BY_TITLE -> sortByName(isAscending)
            ApiContract.Parameters.VAL_SORT_BY_OWNER -> sortByOwner(isAscending)
            ApiContract.Parameters.VAL_SORT_BY_UPDATED -> sortByUpdated(isAscending)
            ApiContract.Parameters.VAL_SORT_BY_TYPE -> sortByType(isAscending)
            ApiContract.Parameters.VAL_SORT_BY_SIZE -> sortBySize(isAscending)
        }
        return false
    }

    fun reverseSortOrder(itemList: List<Recent>) {
        viewState.onReverseSortOrder(itemList.reversed())
    }

    private fun sortByCreated() {
        CoroutineScope(Dispatchers.Default).launch {
            val list = recentDao.getRecents().reversed()
            withContext(Dispatchers.Main) {
                viewState.updateFiles(list)
            }
        }
    }

    private fun sortBySize(isAscending: Boolean) {
        CoroutineScope(Dispatchers.Default).launch {
            var list = recentDao.getRecents().sortedBy { it.size }
            if (!isAscending) {
                list = list.reversed()
            }
            withContext(Dispatchers.Main) {
                viewState.updateFiles(list)
            }
        }
    }

    private fun sortByType(isAscending: Boolean) {
        CoroutineScope(Dispatchers.Default).launch {
            var list = recentDao.getRecents().sortedBy { StringUtils.getExtensionFromPath(it.name) }
            if (!isAscending) {
                list = list.reversed()
            }
            withContext(Dispatchers.Main) {
                viewState.updateFiles(list)
            }
        }
    }

    private fun sortByUpdated(isAscending: Boolean) {
        CoroutineScope(Dispatchers.Default).launch {
            var list = recentDao.getRecents().sortedBy { it.date }
            if (!isAscending) {
                list = list.reversed()
            }
            withContext(Dispatchers.Main) {
                viewState.updateFiles(list)
            }
        }
    }

    private fun sortByOwner(isAscending: Boolean) {
//        val list: List<Recent?> = mAccountsSqlData.getRecent()
//        Collections.sort(list) { o1: Recent, o2: Recent ->
//            if (o1.getAccountsSqlData() != null && o2.getAccountsSqlData() != null) {
//                return@sort o1.getAccountsSqlData().getId().compareTo(o2.getAccountsSqlData().getId())
//            } else {
//                return@sort java.lang.Boolean.compare(o1.isLocal, o2.isLocal)
//            }
//        }
//        if (isAscending) {
//            Collections.reverse(list)
//        }
//        viewState.updateFiles(ArrayList<E>(list))
    }

    private fun sortByName(isAscending: Boolean) {
        CoroutineScope(Dispatchers.Default).launch {
            var list = recentDao.getRecents().sortedBy { it.name }
            if (!isAscending) {
                list = list.reversed()
            }
            withContext(Dispatchers.Main) {
                viewState.updateFiles(list)
            }
        }
    }

    private fun addRecent(recent: Recent) {
        CoroutineScope(Dispatchers.Default).launch {
            recentDao.updateRecent(recent.copy(date = Date().time))
        }
    }

    fun reverseList(itemList: List<Recent>, isAscending: Boolean) {
        setOrder(isAscending)
        viewState.updateFiles(itemList.reversed())
    }

    fun contextClick(recent: Recent, position: Int) {
        CoroutineScope(Dispatchers.Default).launch {
            contextItem = recent
            contextPosition = position
            val state = ContextBottomDialog.State()
            state.title = recent.name
            if (!recent.isLocal) {
                accountDao.getAccount(recent.ownerId ?: "")?.let {
                    state.info =
                        it.portal + mContext.getString(R.string.placeholder_point) + TimeUtils.formatDate(Date(recent.date))
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
        account?.let {
            if (it.isWebDav) {
                val provider = WebDavFileProvider(
                    mContext.webDavApi(),
                    WebDavApi.Providers.valueOf(it.webDavProvider ?: "")
                )
                item?.let { item ->
                    val file = CloudFile().apply {
                        id = item.idFile
                        title = item.path
                        webUrl = item.path
                        folderId = item.idFile?.substring(0, item.idFile?.lastIndexOf('/')?.plus(1) ?: -1)
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
                            viewState.onSnackBar(mContext.getString(R.string.upload_manager_complete))
                        })
                    )
                }
            }
        }
    }

    fun deleteRecent() {
        CoroutineScope(Dispatchers.Default).launch {
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
        explorer.files = listOf(explorerFile)
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
        explorer.files = listOf(explorerFile)
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
                }
            }
        } else {
            checkCloudFile(recent, position)
        }
        addRecent(recent)
        viewState.onMoveElement(recent, position)
    }

    private fun checkCloudFile(recent: Recent, position: Int) {
        CoroutineScope(Dispatchers.Default).launch {
            recent.ownerId?.let { id ->
                accountDao.getAccount(id)?.let { recentAccount ->
                    if (!recentAccount.isOnline) {
                        withContext(Dispatchers.Main) {
                            viewState.onError(mContext.getString(R.string.error_recent_enter_account))
                        }
                    } else if (recentAccount.isWebDav) {
                        openWebDavFile(recent, position)
                    } else {
                        openFile(recent)
                    }
                }

            }
        }
    }

    private fun openLocalFile(uri: Uri) {
        val name = getName(mContext, uri)
        when (StringUtils.getExtension(StringUtils.getExtensionFromPath(name.toLowerCase(Locale.ROOT)))) {
            StringUtils.Extension.DOC -> viewState.onOpenFile(OpenState.Docs(uri))
            StringUtils.Extension.SHEET -> viewState.onOpenFile(OpenState.Cells(uri))
            StringUtils.Extension.PRESENTATION -> viewState.onOpenFile(OpenState.Slide(uri))
            StringUtils.Extension.PDF -> viewState.onOpenFile(OpenState.Pdf(uri))
            StringUtils.Extension.IMAGE, StringUtils.Extension.IMAGE_GIF, StringUtils.Extension.VIDEO_SUPPORT -> {
                viewState.onOpenFile(OpenState.Media(getImages(File(uri.path)), false))
            }
            else -> viewState.onError(mContext.getString(R.string.error_unsupported_format))
        }
    }

    private suspend fun openWebDavFile(recent: Recent, position: Int) {
        accountDao.getAccount(recent.ownerId ?: "")?.let { account ->
            WebDavFileProvider(
                mContext.webDavApi(),
                WebDavApi.Providers.valueOf(account.webDavProvider ?: "")
            ).let { provider ->
                val cloudFile = CloudFile().apply {
                    title = recent.name
                    id = recent.idFile
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
                                    mContext.getString(R.string.dialogs_wait_title),
                                    null
                                )
                            }
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe({ file ->
                                temp = file
                                viewState.onDialogClose()
                                openLocalFile(Uri.parse(file.webUrl))
                                viewState.onMoveElement(recent, position)
                            }, {
                                fetchError(it)
                            })
                        )
                    }
                }
            }
        }
    }

    override fun addRecent(file: CloudFile) {
        CoroutineScope(Dispatchers.Default).launch {
            item?.let {
                recentDao.updateRecent(it.copy(date = Date().time))
            }
        }
    }

    fun clearRecent() {
//        val recents: MutableList<Recent> = mAccountSqlTool.recent
//        for (recent in recents) {
//            mAccountSqlTool.delete(recent)
//        }
//        recents.clear()
//        viewState.updateFiles(ArrayList<E>(recents))
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
        if (temp != null && checkReadWritePermission(mContext)) {
            val uri = Uri.parse(temp!!.webUrl)
            if (uri.path != null) {
                asyncDeletePath(uri.path!!)
            }
        }
        temp = null
    }

    private fun setOrder(isAsc: Boolean) {
        if (isAsc) {
            mPreferenceTool.sortOrder = ApiContract.Parameters.VAL_SORT_ORDER_ASC
        } else {
            mPreferenceTool.sortOrder = ApiContract.Parameters.VAL_SORT_ORDER_DESC
        }
    }


}