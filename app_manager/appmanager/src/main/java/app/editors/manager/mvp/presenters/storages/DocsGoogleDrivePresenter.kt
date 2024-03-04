package app.editors.manager.mvp.presenters.storages

import android.net.Uri
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.work.Data
import androidx.work.OneTimeWorkRequest
import app.documents.core.network.common.utils.GoogleDriveUtils
import app.documents.core.network.manager.models.explorer.CloudFile
import app.documents.core.network.manager.models.explorer.GoogleDriveFolder
import app.documents.core.network.manager.models.explorer.Item
import app.documents.core.network.storages.googledrive.models.GoogleDriveCloudFile
import app.documents.core.network.storages.googledrive.models.request.ShareRequest
import app.documents.core.providers.GoogleDriveFileProvider
import app.editors.manager.R
import app.editors.manager.app.App
import app.editors.manager.app.accountOnline
import app.editors.manager.app.googleDriveLoginProvider
import app.editors.manager.managers.providers.GoogleDriveStorageHelper
import app.editors.manager.managers.receivers.GoogleDriveUploadReceiver
import app.editors.manager.managers.works.BaseDownloadWork
import app.editors.manager.managers.works.googledrive.DownloadWork
import app.editors.manager.mvp.models.states.OperationsState
import app.editors.manager.mvp.views.base.DocsGoogleDriveView
import app.editors.manager.ui.views.custom.PlaceholderViews
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import lib.toolkit.base.managers.utils.AccountUtils
import lib.toolkit.base.managers.utils.ContentResolverUtils
import lib.toolkit.base.managers.utils.KeyboardUtils

class DocsGoogleDrivePresenter : BaseStorageDocsPresenter<DocsGoogleDriveView>(), GoogleDriveUploadReceiver.OnGoogleDriveUploadListener {

    private var uploadGoogleDriveReceiver: GoogleDriveUploadReceiver = GoogleDriveUploadReceiver()

    private val googleDriveFileProvider: GoogleDriveFileProvider by lazy {
        GoogleDriveFileProvider(
            context = context,
            helper = GoogleDriveStorageHelper()
        )
    }

    init {
        App.getApp().appComponent.inject(this)
    }

    override val externalLink: Unit
        get() {
            val request = ShareRequest(
                role = "reader",
                type = "anyone"
            )
            val externalLink = when (val item = itemClicked) {
                is CloudFile -> item.webUrl
                is GoogleDriveFolder -> item.webUrl
                else -> ""
            }
            disposable.add(
                itemClicked?.id?.let { id ->
                    googleDriveFileProvider.share(id, request)
                        .subscribe({ response ->
                            if (response) {
                                KeyboardUtils.setDataToClipboard(
                                    context,
                                    externalLink,
                                    context.getString(R.string.share_clipboard_external_link_label)
                                )
                                viewState.onSnackBar(context.getString(R.string.share_clipboard_external_copied))
                            } else {
                                viewState.onSnackBar(context.getString(R.string.errors_client_forbidden))
                            }
                        },
                            { throwable: Throwable -> fetchError(throwable) }
                        )
                }!!
            )


        }

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        uploadGoogleDriveReceiver.setUploadListener(this)
        LocalBroadcastManager.getInstance(context).registerReceiver(
            uploadGoogleDriveReceiver, uploadGoogleDriveReceiver.filter
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        uploadGoogleDriveReceiver.setUploadListener(null)
        LocalBroadcastManager.getInstance(context).unregisterReceiver(uploadGoogleDriveReceiver)

    }

    override fun getProvider() {
        fileProvider?.let {
            getItemsById("root")
        } ?: run {
            fileProvider = googleDriveFileProvider
            getItemsById("root")
        }
    }

    override fun getNextList() {
        fileProvider?.let { provider ->
            val nextPage = GoogleDriveUtils.GOOGLE_DRIVE_NEXT_PAGE_TOKEN to modelExplorerStack.last()?.current?.parentId.orEmpty()
            disposable.add(
                provider.getFiles(modelExplorerStack.currentId, getArgs(filteringValue).plus(nextPage))
                    .doOnNext(modelExplorerStack::addOnNext)
                    .map { it.folders + it.files }
                    .subscribe(viewState::onDocsNext, ::fetchError)
            )
        }
    }

    override fun getItemsById(id: String?) {
        id?.let {
            setPlaceholderType(PlaceholderViews.Type.LOAD)
            fileProvider?.let { provider ->
                disposable.add(
                    provider.getFiles(id, getArgs(filteringValue).putFilters())
                        .doOnNext { it.filterType = preferenceTool.filter.type.filterVal }
                        .subscribe(::loadSuccess, ::fetchError)
                )
            }
        }
    }

    override fun getFileInfo() {
        downloadDisposable = fileProvider?.fileInfo(itemClicked)
            ?.subscribeOn(Schedulers.io())
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe(
                { file: CloudFile? ->
                    tempFile = file
                    viewState.onDialogClose()
                    file?.let { addRecent(it) }
                    viewState.onOpenLocalFile(file)
                }
            ) { throwable: Throwable -> fetchError(throwable) }
    }

    override fun startDownload(downloadTo: Uri, item: Item?) {
        val data = Data.Builder().apply {
            putString(BaseDownloadWork.FILE_ID_KEY, item?.id)
            putString(BaseDownloadWork.FILE_URI_KEY, downloadTo.toString())
            when (item) {
                is GoogleDriveCloudFile -> {
                    val isGoogleMimeType = GoogleDriveFileProvider.GoogleMimeType.isGoogleMimeType(item.mimeType)
                    putString(DownloadWork.GOOGLE_MIME_TYPE, item.mimeType.takeIf { isGoogleMimeType })
                }
            }
        }.build()

        val request = OneTimeWorkRequest.Builder(DownloadWork::class.java)
            .setInputData(data)
            .build()

        workManager.enqueue(request)
    }

    override fun copy(): Boolean {
        val itemList = mutableListOf<Item>()
        when {
            modelExplorerStack.selectedFiles.isNotEmpty() -> {
                itemList.addAll(modelExplorerStack.selectedFiles)
            }
            modelExplorerStack.selectedFolders.isNotEmpty() -> {
                viewState.onSnackBar(context.getString(R.string.storage_google_drive_copy_folder_error))
                itemList.addAll(modelExplorerStack.selectedFiles)
            }
            else -> itemClicked?.let { itemList.add(it) }
        }
        showDialogWaiting(TAG_DIALOG_CANCEL_SINGLE_OPERATIONS)
        disposable.add(googleDriveFileProvider.copy(itemList)
            .subscribe({}, {
                fetchError(it)
                if (isSelectionMode) {
                    setSelection(false)
                    updateViewsState()
                }
            }, {
                viewState.onDocsBatchOperation()
                if (isSelectionMode) {
                    setSelection(false)
                    updateViewsState()
                }
            })
        )
        return false
    }

    override fun moveCopySelected(operationsState: OperationsState.OperationType) {
        if (operationsState == OperationsState.OperationType.COPY) {
            copy()
        }
    }

    override fun upload(uri: Uri?, uris: List<Uri>?, tag: String?) {
        viewState.onUpload(
            folderId = modelExplorerStack.currentId.orEmpty(),
            fileId = itemClicked?.id.orEmpty(),
            uploadUris = uri?.let(::listOf) ?: uris ?: listOf(),
            tag = if (itemClicked?.title == ContentResolverUtils.getName(context, uri ?: Uri.EMPTY))
                "KEY_UPDATE"
            else "KEY_UPLOAD"
        )
    }

    override fun onItemId(itemId: String) {
        itemClicked?.id = itemId
    }

    override fun refreshToken() {
        context.accountOnline?.accountName?.let { accountName ->
            AccountUtils.getAccount(context, accountName)?.let { account ->
                val refreshToken = AccountUtils.getAccountData(context, account).refreshToken.orEmpty()
                context.googleDriveLoginProvider
                    .refreshToken(refreshToken)
                    .subscribe({ tokenResponse ->
                        AccountUtils.setToken(context, account, tokenResponse.accessToken)
                        App.getApp().refreshGoogleDriveInstance()
                        getItemsById("root")
                    }) { viewState.onSignIn() }
            }
        }
    }
}