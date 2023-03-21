package app.editors.manager.mvp.presenters.storages

import android.accounts.Account
import android.net.Uri
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.work.Data
import androidx.work.OneTimeWorkRequest
import app.documents.core.network.common.utils.GoogleDriveUtils
import app.documents.core.network.manager.models.explorer.CloudFile
import app.documents.core.network.manager.models.explorer.GoogleDriveFolder
import app.documents.core.network.manager.models.explorer.Item
import app.documents.core.network.storages.googledrive.models.request.ShareRequest
import app.documents.core.providers.GoogleDriveFileProvider
import app.editors.manager.R
import app.editors.manager.app.App
import app.editors.manager.app.googleDriveLoginService
import app.editors.manager.managers.utils.StorageUtils
import app.editors.manager.managers.works.BaseDownloadWork
import app.editors.manager.mvp.models.explorer.CloudFile
import app.editors.manager.mvp.models.explorer.Explorer
import app.editors.manager.mvp.models.explorer.GoogleDriveFolder
import app.editors.manager.mvp.models.explorer.Item
import app.editors.manager.app.accountOnline
import app.editors.manager.app.googleDriveLoginProvider
import app.editors.manager.managers.providers.GoogleDriveStorageHelper
import app.editors.manager.managers.receivers.GoogleDriveUploadReceiver
import app.editors.manager.managers.works.BaseStorageDownloadWork
import app.editors.manager.managers.works.googledrive.DownloadWork
import app.editors.manager.mvp.models.states.OperationsState
import app.editors.manager.storages.base.presenter.BaseStorageDocsPresenter
import app.editors.manager.storages.base.view.DocsGoogleDriveView
import app.editors.manager.storages.googledrive.managers.providers.GoogleDriveFileProvider
import app.editors.manager.storages.googledrive.managers.receiver.GoogleDriveUploadReceiver
import app.editors.manager.storages.googledrive.managers.utils.GoogleDriveUtils
import app.editors.manager.storages.googledrive.managers.works.DownloadWork
import app.editors.manager.storages.googledrive.mvp.models.GoogleDriveCloudFile
import app.editors.manager.storages.googledrive.mvp.models.request.ShareRequest
import app.editors.manager.mvp.views.base.DocsGoogleDriveView
import app.editors.manager.ui.dialogs.ContextBottomDialog
import app.editors.manager.ui.views.custom.PlaceholderViews
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import lib.toolkit.base.managers.utils.*
import lib.toolkit.base.managers.utils.AccountUtils
import lib.toolkit.base.managers.utils.KeyboardUtils
import lib.toolkit.base.managers.utils.StringUtils
import lib.toolkit.base.managers.utils.TimeUtils

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

    val isFolderSelected get() = modelExplorerStack.selectedFolders.isNotEmpty()

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
                                viewState.onDocsAccess(
                                    true,
                                    context.getString(R.string.share_clipboard_external_copied)
                                )
                            } else {
                                viewState.onDocsAccess(
                                    false,
                                    context.getString(R.string.errors_client_forbidden)
                                )
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
        disposable.add(googleDriveFileProvider.copy(itemList, modelExplorerStack.currentId!!)
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
        val uploadUris = mutableListOf<Uri>()
        var index = 0

        uri?.let {
            uploadUris.add(uri)
        } ?: run {
            uris?.let {
                while (index != uris.count()) {
                    uploadUris.add(uris[index])
                    index++
                }
            }
        }

        val name = ContentResolverUtils.getName(context, uri?: Uri.EMPTY)

        val newTag = if (itemClicked?.title == name) {
            "KEY_UPDATE"
        } else {
            "KEY_UPLOAD"
        }

        viewState.onUpload(
            uploadUris = uploadUris,
            folderId = modelExplorerStack.currentId.orEmpty(),
            fileId = itemClicked?.id.orEmpty(),
            tag = newTag
        )
    }

    override fun onContextClick(item: Item, position: Int, isTrash: Boolean) {
        onClickEvent(item, position)
        isContextClick = true
        val state = ContextBottomDialog.State()
        state.title = itemClickedTitle
        state.info = TimeUtils.formatDate(itemClickedDate)
        state.isFolder = !isClickedItemFile
        state.isDocs = isClickedItemDocs
        state.isWebDav = false
        state.isOneDrive = false
        state.isDropBox = false
        state.isGoogleDrive = true
        state.isTrash = isTrash
        state.isItemEditable = true
        state.isContextEditable = true
        state.isCanShare = true
        if (!isClickedItemFile) {
            state.iconResId = R.drawable.ic_type_folder
        } else {
            state.iconResId = getIconContext(
                StringUtils.getExtensionFromPath(
                    itemClickedTitle
                )
            )
        }
        state.isPdf = isPdf
        if (state.isShared && state.isFolder) {
            state.iconResId = R.drawable.ic_type_folder_shared
        }
        viewState.onItemContext(state)
    }

    override fun onItemId(itemId: String) {
        itemClicked?.id = itemId
    }

    override fun refreshToken() {
        context.accountOnline?.getAccountName()?.let { accountName ->
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