package app.editors.manager.storages.googledrive.mvp.presenters

import android.content.ClipData
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.work.Data
import androidx.work.OneTimeWorkRequest
import app.editors.manager.R
import app.editors.manager.app.App
import app.editors.manager.storages.googledrive.managers.works.DownloadWork
import app.editors.manager.storages.googledrive.managers.providers.GoogleDriveFileProvider
import app.editors.manager.storages.googledrive.managers.utils.GoogleDriveUtils
import app.editors.manager.storages.googledrive.managers.works.UploadWork
import app.editors.manager.storages.googledrive.mvp.models.request.ShareRequest
import app.editors.manager.mvp.models.explorer.*
import app.editors.manager.storages.base.presenter.BaseStorageDocsPresenter
import app.editors.manager.storages.base.view.BaseStorageDocsView
import app.editors.manager.storages.base.work.BaseStorageDownloadWork
import app.editors.manager.storages.base.work.BaseStorageUploadWork
import app.editors.manager.storages.googledrive.managers.receiver.GoogleDriveUploadReceiver
import app.editors.manager.ui.dialogs.ContextBottomDialog
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import lib.toolkit.base.managers.utils.AccountUtils
import lib.toolkit.base.managers.utils.KeyboardUtils
import lib.toolkit.base.managers.utils.StringUtils
import lib.toolkit.base.managers.utils.TimeUtils

class DocsGoogleDrivePresenter: BaseStorageDocsPresenter<BaseStorageDocsView>(), GoogleDriveUploadReceiver.OnGoogleDriveUploadListener {

    var uploadGoogleDriveReceiver: GoogleDriveUploadReceiver? = null

    init {
        App.getApp().appComponent.inject(this)
        uploadGoogleDriveReceiver = GoogleDriveUploadReceiver()

    }
    override val externalLink : Unit
        get() {
            val request = ShareRequest(
                role = "reader",
                type = "anyone"
            )
            val externalLink = if (itemClicked is CloudFile) (itemClicked as CloudFile).webUrl else if(itemClicked is GoogleDriveFolder) (itemClicked as GoogleDriveFolder).webUrl else ""
            disposable.add(
                itemClicked?.id?.let { id ->
                    (fileProvider as GoogleDriveFileProvider).share(id, request)
                        .subscribe({ response ->
                            if(response) {
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
                            {throwable: Throwable -> fetchError(throwable)}
                        )
                }!!
            )


        }

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        uploadGoogleDriveReceiver?.setUploadListener(this)
        LocalBroadcastManager.getInstance(context).registerReceiver(uploadGoogleDriveReceiver!!, uploadGoogleDriveReceiver?.filter!!)
    }

    override fun onDestroy() {
        super.onDestroy()
        uploadGoogleDriveReceiver?.setUploadListener(null)
        LocalBroadcastManager.getInstance(context).unregisterReceiver(uploadGoogleDriveReceiver!!)
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
        val id = modelExplorerStack?.currentId
        val args = getArgs(filteringValue)
        disposable.add(fileProvider?.getFiles(id!!, args)?.subscribe({ explorer: Explorer? ->
            modelExplorerStack?.addOnNext(explorer)
            val last = modelExplorerStack?.last()
            if (last != null) {
                viewState.onDocsNext(getListWithHeaders(last, true))
            }
        }) { throwable: Throwable -> fetchError(throwable) }!!)
    }

    override fun getArgs(filteringValue: String?): MutableMap<String, String> {
        val args = mutableMapOf<String, String>()
        if(modelExplorerStack?.last()?.current?.providerItem == true) {
            args[GoogleDriveUtils.GOOGLE_DRIVE_NEXT_PAGE_TOKEN] =
                modelExplorerStack?.last()?.current?.parentId!!
        }
        args.putAll(super.getArgs(filteringValue))
        return args
    }

    override fun getFileInfo() {
        if (itemClicked != null && itemClicked is CloudFile) {
            val file = itemClicked as CloudFile
            val extension = file.fileExst
            if (StringUtils.isImage(extension)) {
                addRecent(file)
                return
            }
        }
        downloadDisposable = fileProvider?.fileInfo(itemClicked!!)
            ?.subscribeOn(Schedulers.io())
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe(
                { file: CloudFile? ->
                    tempFile = file
                    viewState.onDialogClose()
                    viewState.onOpenLocalFile(file)
                }
            ) { throwable: Throwable -> fetchError(throwable) }
    }

    override fun download(downloadTo: Uri) {
        if(modelExplorerStack?.countSelectedItems == 0) {
            startDownload(downloadTo, itemClicked)
        } else {
            val itemList: MutableList<Item> = (modelExplorerStack?.selectedFiles!! + modelExplorerStack?.selectedFolders!!).toMutableList()
            itemList.forEach { item ->
                val fileName = if(item is CloudFile) item.title else DownloadWork.DOWNLOAD_ZIP_NAME
                val doc = DocumentFile.fromTreeUri(context, downloadTo)?.createFile(StringUtils.getMimeTypeFromExtension(fileName.substring(fileName.lastIndexOf("."))), fileName)
                startDownload(doc?.uri!!, item)
            }
        }
    }

    private fun startDownload(downloadTo: Uri, item: Item?) {
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
            modelExplorerStack?.selectedFiles?.isNotEmpty() == true -> {
                itemList.addAll(modelExplorerStack?.selectedFiles!!)
            }
            modelExplorerStack?.selectedFolders?.isNotEmpty() == true -> {
                viewState.onSnackBar(context.getString(R.string.storage_google_drive_copy_folder_error))
                itemList.addAll(modelExplorerStack?.selectedFiles!!)
            }
            else -> {
                itemClicked?.let { itemList.add(it) }
            }
        }
        showDialogWaiting(TAG_DIALOG_CANCEL_SINGLE_OPERATIONS)
        disposable.add((fileProvider as GoogleDriveFileProvider).copy(itemList, modelExplorerStack?.currentId!!)
            .subscribe ({},{
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

    override fun copySelected() {
        copy()
    }

    fun upload(uri: Uri?, uris: ClipData?, tag: String) {
        val uploadUris = mutableListOf<Uri>()
        var index = 0

        if(uri != null) {
            uploadUris.add(uri)
        } else if(uris != null) {
            while(index != uris.itemCount) {
                uploadUris.add(uris.getItemAt(index).uri)
                index++
            }
        }

        for (uri in uploadUris) {
            val data = Data.Builder()
                .putString(BaseStorageUploadWork.TAG_FOLDER_ID, modelExplorerStack?.currentId)
                .putString(BaseStorageUploadWork.TAG_UPLOAD_FILES, uri.toString())
                .putString(BaseStorageUploadWork.KEY_TAG, tag)
                .putString(UploadWork.KEY_FILE_ID, itemClicked?.id)
                .build()

            val request = OneTimeWorkRequest.Builder(UploadWork::class.java)
                .setInputData(data)
                .build()

            workManager.enqueue(request)
        }

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
}