package app.editors.manager.mvp.presenters.storages

import android.net.Uri
import androidx.work.Data
import androidx.work.OneTimeWorkRequest
import app.documents.core.network.common.Result
import app.documents.core.network.common.contracts.ApiContract
import app.documents.core.network.common.utils.OneDriveUtils
import app.documents.core.network.manager.models.explorer.CloudFile
import app.documents.core.network.manager.models.explorer.Explorer
import app.documents.core.network.manager.models.explorer.Item
import app.documents.core.network.storages.onedrive.models.request.ExternalLinkRequest
import app.documents.core.providers.OneDriveFileProvider
import app.editors.manager.R
import app.editors.manager.app.App
import app.editors.manager.managers.providers.OneDriveStorageHelper
import app.editors.manager.managers.works.BaseDownloadWork
import app.editors.manager.managers.works.BaseStorageUploadWork
import app.editors.manager.managers.works.onedrive.DownloadWork
import app.editors.manager.managers.works.onedrive.UploadWork
import app.editors.manager.mvp.views.base.BaseStorageDocsView
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.launch
import lib.toolkit.base.managers.utils.KeyboardUtils
import moxy.InjectViewState
import moxy.presenterScope
import retrofit2.HttpException

@InjectViewState
class DocsOneDrivePresenter: BaseStorageDocsPresenter<BaseStorageDocsView>() {

    override val externalLink : Unit
        get() {
            itemClicked?.let {
                val request = ExternalLinkRequest(
                    type = OneDriveUtils.VAL_SHARE_TYPE_READ_WRITE,
                    scope = OneDriveUtils.VAL_SHARE_SCOPE_ANON
                )
                oneDriveFileProvider.share(it.id, request)?.let { externalLinkResponse ->
                    disposable.add(externalLinkResponse
                        .subscribe( {response ->
                            it.shared = !it.shared
                            response.link?.webUrl?.let { link ->
                                KeyboardUtils.setDataToClipboard(
                                    context,
                                    link,
                                    context.getString(R.string.share_clipboard_external_link_label)
                                )
                                viewState.onSnackBar(context.getString(R.string.share_clipboard_external_copied))
                            }
                        }) { throwable: Throwable -> fetchError(throwable) }
                    )
                }
            }
        }

    private val oneDriveFileProvider: OneDriveFileProvider by lazy {
        OneDriveFileProvider(
            context = context,
            helper = OneDriveStorageHelper()
        )
    }

    init {
        App.getApp().appComponent.inject(this)
    }

    override fun getProvider() {
        fileProvider?.let {
            getItemsById("")
        } ?: run {
            fileProvider = oneDriveFileProvider
            getItemsById("")
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun refreshToken() {
        presenterScope.launch {
            App.getApp().refreshLoginComponent(null)
            App.getApp().loginComponent.onedriveLoginRepository.refreshToken()
                .collect { result ->
                    when (result) {
                        is Result.Error -> fetchError(result.exception)
                        is Result.Success -> {
                            App.getApp().refreshOneDriveInstance()
                            getItemsById("")
                        }
                    }
                }
        }
    }

    override fun startDownload(downloadTo: Uri, item: Item?) {
        val data = Data.Builder()
            .putString(BaseDownloadWork.FILE_ID_KEY, item?.id)
            .putString(BaseDownloadWork.FILE_URI_KEY, downloadTo.toString())
            .build()

        val request = OneTimeWorkRequest.Builder(DownloadWork::class.java)
            .setInputData(data)
            .build()

        workManager.enqueue(request)
    }

    override fun getNextList() {
        val id = modelExplorerStack.currentId
        val loadPosition = modelExplorerStack.loadPosition

        id?.let {
            if (loadPosition > 0) {
                val args = getArgs(filteringValue).toMutableMap()
                args[ApiContract.Parameters.ARG_START_INDEX] = loadPosition.toString()
                fileProvider?.let { provider ->
                    disposable.add(provider.getFiles(id, args).subscribe({ explorer: Explorer? ->
                        modelExplorerStack.addOnNext(explorer)
                        modelExplorerStack.last()?.let {
                            viewState.onDocsNext(getListWithHeaders(it, true))
                        }
                    }) { throwable: Throwable -> fetchError(throwable) })
                }
            }
        }
    }

    override fun getFileInfo() {
        showDialogWaiting(TAG_DIALOG_CANCEL_UPLOAD)
        fileProvider?.let { provider ->
            downloadDisposable = provider.fileInfo(itemClicked)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ file: CloudFile? ->
                        tempFile = file
                        viewState.onDialogClose()
                        file?.let { addRecent(it) }
                        viewState.onOpenLocalFile(file)
                    }
                ) { throwable: Throwable -> fetchError(throwable) }
        }
    }

    override fun upload(uri: Uri?, uris: List<Uri>?, tag: String?) {
        val uploadUris = mutableListOf<Uri>()
        var index = 0

        uri?.let {
            uploadUris.add(uri)
        } ?: run {
            uris?.let {
                while(index != uris.size) {
                    uploadUris.add(uris[index])
                    index++
                }
            }
        }

        for (uploadUri in uploadUris) {
            val data = Data.Builder()
                .putString(BaseStorageUploadWork.TAG_FOLDER_ID, modelExplorerStack.currentId)
                .putString(BaseStorageUploadWork.TAG_UPLOAD_FILES, uploadUri.toString())
                .putString(BaseStorageUploadWork.KEY_TAG, tag)
                .build()

            val request = OneTimeWorkRequest.Builder(UploadWork::class.java)
                .setInputData(data)
                .build()

            workManager.enqueue(request)
        }

    }

    override fun copy(): Boolean {
        return if (super.copy()) {
            transfer(ApiContract.Operation.DUPLICATE, false)
            true
        } else {
            false
        }
    }

    override fun fetchError(throwable: Throwable) {
        super.fetchError(throwable)
        if (throwable is HttpException) {
            if (throwable.code() == 423) {
                viewState.onError(App.getApp().applicationContext.getString(R.string.storage_onedrive_error_opened))
            }
            if (throwable.code() == 409) {
                viewState.onError(App.getApp().applicationContext.getString(R.string.storage_onedrive_error_exist))
            }
        }
    }

    fun isFoldersInSelection(): Boolean = modelExplorerStack.selectedFolders.isEmpty()

}