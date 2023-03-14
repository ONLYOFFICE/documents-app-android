package app.editors.manager.storages.googledrive.mvp.presenters

import android.accounts.Account
import android.net.Uri
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.work.Data
import androidx.work.OneTimeWorkRequest
import app.editors.manager.BuildConfig
import app.editors.manager.R
import app.editors.manager.app.App
import app.editors.manager.app.googleDriveLoginService
import app.editors.manager.managers.utils.StorageUtils
import app.editors.manager.mvp.models.explorer.CloudFile
import app.editors.manager.mvp.models.explorer.Explorer
import app.editors.manager.mvp.models.explorer.GoogleDriveFolder
import app.editors.manager.mvp.models.explorer.Item
import app.editors.manager.mvp.models.states.OperationsState
import app.editors.manager.storages.base.presenter.BaseStorageDocsPresenter
import app.editors.manager.storages.base.view.DocsGoogleDriveView
import app.editors.manager.storages.base.work.BaseStorageDownloadWork
import app.editors.manager.storages.googledrive.managers.providers.GoogleDriveFileProvider
import app.editors.manager.storages.googledrive.managers.receiver.GoogleDriveUploadReceiver
import app.editors.manager.storages.googledrive.managers.utils.GoogleDriveUtils
import app.editors.manager.storages.googledrive.managers.works.DownloadWork
import app.editors.manager.storages.googledrive.mvp.models.request.ShareRequest
import app.editors.manager.ui.dialogs.ContextBottomDialog
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import lib.toolkit.base.managers.utils.*

class DocsGoogleDrivePresenter : BaseStorageDocsPresenter<DocsGoogleDriveView>(), GoogleDriveUploadReceiver.OnGoogleDriveUploadListener {

    private var uploadGoogleDriveReceiver: GoogleDriveUploadReceiver = GoogleDriveUploadReceiver()

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
                    (fileProvider as GoogleDriveFileProvider).share(id, request)
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
            CoroutineScope(Dispatchers.Default).launch {
                App.getApp().appComponent.accountsDao.getAccountOnline()?.let {
                    withContext(Dispatchers.Main) {
                        getItemsById("root")
                    }
                }
            }
        } ?: run {
            CoroutineScope(Dispatchers.Default).launch {
                App.getApp().appComponent.accountsDao.getAccountOnline()?.let { cloudAccount ->
                    AccountUtils.getAccount(context, cloudAccount.getAccountName())?.let {
                        fileProvider = GoogleDriveFileProvider()
                        withContext(Dispatchers.Main) {
                            getItemsById("root")
                        }
                    }
                } ?: run {
                    throw Error("Not accounts")
                }
            }
        }
    }

    override fun getNextList() {
        val id = modelExplorerStack.currentId
        val args = getArgs(filteringValue)
        disposable.add(fileProvider?.getFiles(id, args)?.subscribe({ explorer: Explorer? ->
            modelExplorerStack.addOnNext(explorer)
            val last = modelExplorerStack.last()
            last?.let {
                viewState.onDocsNext(getListWithHeaders(it, true))
            }

        }) { throwable: Throwable -> fetchError(throwable) }!!)
    }

    override fun getArgs(filteringValue: String?): MutableMap<String, String> {
        val args = mutableMapOf<String, String>()
        if (modelExplorerStack.last()?.current?.providerItem == true) {
            args[GoogleDriveUtils.GOOGLE_DRIVE_NEXT_PAGE_TOKEN] =
                modelExplorerStack.last()?.current?.parentId!!
        }
        args.putAll(super.getArgs(filteringValue))
        return args
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
        disposable.add((fileProvider as GoogleDriveFileProvider).copy(itemList, modelExplorerStack.currentId!!)
            .subscribe({}, {
                fetchError(it)
                if (isSelectionMode) {
                    setSelection(false)
                    updateViewsState()
                }
                false
            }, {
                viewState.onDocsBatchOperation()
                if (isSelectionMode) {
                    setSelection(false)
                    updateViewsState()
                }
                true
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
        val account = Account(
            App.getApp().appComponent.accountOnline?.getAccountName(),
            context.getString(lib.toolkit.base.R.string.account_type)
        )
        val accData = AccountUtils.getAccountData(context, account)
        val map = mapOf(
            StorageUtils.ARG_CLIENT_ID to BuildConfig.GOOGLE_COM_CLIENT_ID,
            StorageUtils.ARG_CLIENT_SECRET to BuildConfig.GOOGLE_COM_CLIENT_SECRET,
            StorageUtils.ARG_GRANT_TYPE to StorageUtils.OneDrive.VALUE_GRANT_TYPE_REFRESH,
            StorageUtils.ARG_REFRESH_TOKEN to accData.refreshToken.orEmpty(),
        )

        disposable.add(
            App.getApp().googleDriveLoginService.getToken(map).subscribe({ tokenResponse ->
                AccountUtils.setAccountData(context, account, accData.copy(accessToken = (tokenResponse.accessToken)))
                AccountUtils.setToken(context, account, tokenResponse.accessToken)
                (fileProvider as GoogleDriveFileProvider).refreshInstance()
                getProvider()
            }, {
                viewState.onSignIn()
            })
        )
    }
}