package app.editors.manager.mvp.presenters.storages

import android.accounts.Account
import android.content.ClipData
import android.net.Uri
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.work.Data
import androidx.work.OneTimeWorkRequest
import app.documents.core.network.common.utils.GoogleDriveUtils
import app.documents.core.network.manager.models.explorer.CloudFile
import app.documents.core.network.manager.models.explorer.GoogleDriveFolder
import app.documents.core.network.manager.models.explorer.Item
import app.documents.core.network.storages.googledrive.models.request.ShareRequest
import app.editors.manager.R
import app.editors.manager.app.App
import app.editors.manager.app.googleDriveLoginProvider
import app.editors.manager.managers.providers.GoogleDriveFileProvider
import app.editors.manager.managers.receivers.GoogleDriveUploadReceiver
import app.editors.manager.managers.works.BaseStorageDownloadWork
import app.editors.manager.managers.works.googledrive.DownloadWork
import app.editors.manager.mvp.views.base.DocsGoogleDriveView
import app.editors.manager.ui.dialogs.ContextBottomDialog
import app.editors.manager.ui.views.custom.PlaceholderViews
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import lib.toolkit.base.managers.utils.AccountUtils
import lib.toolkit.base.managers.utils.KeyboardUtils
import lib.toolkit.base.managers.utils.StringUtils
import lib.toolkit.base.managers.utils.TimeUtils

class DocsGoogleDrivePresenter : BaseStorageDocsPresenter<DocsGoogleDriveView>(), GoogleDriveUploadReceiver.OnGoogleDriveUploadListener {

    private var uploadGoogleDriveReceiver: GoogleDriveUploadReceiver = GoogleDriveUploadReceiver()

    private val googleDriveFileProvider: GoogleDriveFileProvider by lazy { GoogleDriveFileProvider() }

    init {
        App.getApp().appComponent.inject(this)
    }

    override val externalLink: Unit
        get() {
            val request = ShareRequest(
                role = "reader",
                type = "anyone"
            )
            val externalLink =
                if (itemClicked is CloudFile) (itemClicked as CloudFile).webUrl else if (itemClicked is GoogleDriveFolder) (itemClicked as GoogleDriveFolder).webUrl else ""
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
        if (fileProvider == null) {
            fileProvider = googleDriveFileProvider
            getProvider()
        } else {
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
        val data = Data.Builder()
            .putString(BaseStorageDownloadWork.FILE_ID_KEY, item?.id)
            .putString(BaseStorageDownloadWork.FILE_URI_KEY, downloadTo.toString())
            .putString(
                DownloadWork.DOWNLOADABLE_ITEM_KEY,
                if (item is CloudFile) DownloadWork.DOWNLOADABLE_ITEM_FILE else DownloadWork.DOWNLOADABLE_ITEM_FOLDER
            )
            .build()

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
            else -> {
                itemClicked?.let { itemList.add(it) }
            }
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

    override fun copySelected() {
        copy()
    }

    fun upload(uri: Uri?, uris: ClipData?, tag: String) {
        val uploadUris = mutableListOf<Uri>()
        var index = 0

        uri?.let {
            uploadUris.add(uri)
        } ?: run {
            uris?.let {
                while (index != uris.itemCount) {
                    uploadUris.add(uris.getItemAt(index).uri)
                    index++
                }
            }
        }

        viewState.onUpload(
            uploadUris = uploadUris,
            folderId = modelExplorerStack.currentId.orEmpty(),
            fileId = itemClicked?.id.orEmpty(),
            tag = tag
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
        val account = Account(
            App.getApp().appComponent.accountOnline?.getAccountName(),
            context.getString(lib.toolkit.base.R.string.account_type)
        )
        val accData = AccountUtils.getAccountData(context, account)
        disposable.add(
            context.googleDriveLoginProvider.refreshToken(accData.refreshToken.orEmpty()).subscribe({ tokenResponse ->
                AccountUtils.setAccountData(context, account, accData.copy(accessToken = (tokenResponse.accessToken)))
                AccountUtils.setToken(context, account, tokenResponse.accessToken)
                googleDriveFileProvider.refreshInstance()
                getProvider()
            }, {
                viewState.onSignIn()
            })
        )
    }
}