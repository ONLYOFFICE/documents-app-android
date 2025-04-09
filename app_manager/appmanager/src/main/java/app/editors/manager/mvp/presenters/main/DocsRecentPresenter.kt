package app.editors.manager.mvp.presenters.main

import android.annotation.SuppressLint
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import app.documents.core.account.AccountPreferences
import app.documents.core.model.cloud.Recent
import app.documents.core.network.common.contracts.ApiContract
import app.documents.core.network.manager.models.explorer.CloudFile
import app.documents.core.network.manager.models.explorer.Current
import app.documents.core.network.manager.models.explorer.Explorer
import app.editors.manager.R
import app.editors.manager.app.App
import app.editors.manager.app.webDavFileProvider
import app.editors.manager.mvp.views.main.DocsRecentView
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import lib.toolkit.base.managers.utils.EditType
import lib.toolkit.base.managers.utils.EditorsType
import lib.toolkit.base.managers.utils.FileUtils.asyncDeletePath
import lib.toolkit.base.managers.utils.PermissionUtils.checkReadWritePermission
import lib.toolkit.base.managers.utils.StringUtils
import moxy.InjectViewState
import moxy.presenterScope
import java.io.File
import java.util.Date
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
        recent?.let {
            item = recent
            fileOpenRepository.openRecentFile(recent, EditType.Edit())
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