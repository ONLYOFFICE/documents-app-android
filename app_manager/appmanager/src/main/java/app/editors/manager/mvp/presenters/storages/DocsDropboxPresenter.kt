package app.editors.manager.mvp.presenters.storages

import android.content.ClipData
import android.net.Uri
import androidx.work.Data
import androidx.work.OneTimeWorkRequest
import app.documents.core.network.common.contracts.ApiContract
import app.documents.core.network.common.utils.DropboxUtils
import app.documents.core.network.manager.models.explorer.CloudFile
import app.documents.core.network.manager.models.explorer.Explorer
import app.documents.core.network.manager.models.explorer.Item
import app.documents.core.network.storages.dropbox.login.DropboxResponse
import app.documents.core.network.storages.dropbox.models.request.TokenRefreshRequest
import app.documents.core.network.storages.dropbox.models.request.TokenRequest
import app.documents.core.network.storages.dropbox.models.response.RefreshTokenResponse
import app.editors.manager.R
import app.editors.manager.app.App
import app.editors.manager.app.accountOnline
import app.editors.manager.app.dropboxLoginProvider
import app.editors.manager.managers.providers.DropboxFileProvider
import app.editors.manager.managers.works.BaseStorageDownloadWork
import app.editors.manager.managers.works.BaseStorageUploadWork
import app.editors.manager.managers.works.dropbox.DownloadWork
import app.editors.manager.managers.works.dropbox.UploadWork
import app.editors.manager.mvp.views.base.BaseStorageDocsView
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


class DocsDropboxPresenter: BaseStorageDocsPresenter<BaseStorageDocsView>() {

    init {
        App.getApp().appComponent.inject(this)
    }

    override val externalLink : Unit
        get() {
            itemClicked?.let { item ->
                (fileProvider as DropboxFileProvider).share(item.id)?.let { externalLinkResponse ->
                    disposable.add(externalLinkResponse
                        .subscribe({ response ->
                            item.shared = !item.shared
                            response.link.let { link ->
                                KeyboardUtils.setDataToClipboard(
                                    context,
                                    link,
                                    context.getString(R.string.share_clipboard_external_link_label)
                                )
                            }
                            viewState.onDocsAccess(
                                true,
                                context.getString(R.string.share_clipboard_external_copied)
                            )
                        }) { throwable: Throwable -> fetchError(throwable) }
                    )
                }
            }
        }

    override fun getProvider() {
        fileProvider?.let {
            CoroutineScope(Dispatchers.Default).launch {
                App.getApp().appComponent.accountsDao.getAccountOnline()?.let {
                    withContext(Dispatchers.Main) {
                        getItemsById(DropboxUtils.DROPBOX_ROOT)
                    }

                }
            }
        } ?: run {
            CoroutineScope(Dispatchers.Default).launch {
                App.getApp().appComponent.accountsDao.getAccountOnline()?.let { cloudAccount ->
                    AccountUtils.getAccount(context, cloudAccount.getAccountName())?.let {
                        fileProvider = DropboxFileProvider()
                        withContext(Dispatchers.Main) {
                            getItemsById(DropboxUtils.DROPBOX_ROOT)
                        }
                    }
                } ?: run {
                    throw Error("Not accounts")
                }
            }
        }
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

    override fun getNextList() {
        val id = modelExplorerStack.currentId
        val args = getArgs(filteringValue)
        fileProvider?.let { provider ->
            disposable.add(provider.getFiles(id, args).subscribe({ explorer: Explorer? ->
                modelExplorerStack.addOnNext(explorer)
                val last = modelExplorerStack.last()
                if (last != null) {
                    viewState.onDocsNext(getListWithHeaders(last, true))
                }
            }) { throwable: Throwable -> fetchError(throwable) })
        }
    }


    fun upload(uri: Uri?, uris: ClipData?, tag: String) {
        val uploadUris = mutableListOf<Uri>()
        var index = 0

        uri?.let {
            uploadUris.add(uri)
        } ?: run {
            uris?.let {
                while(index != uris.itemCount) {
                    uploadUris.add(uris.getItemAt(index).uri)
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

    override fun getArgs(filteringValue: String?): Map<String, String> {
        val args = mutableMapOf<String, String>()
        if(modelExplorerStack.last()?.current?.providerItem == true) {
            args[DropboxUtils.DROPBOX_CONTINUE_CURSOR] =
                modelExplorerStack.last()?.current?.parentId!!
        }
        if(modelExplorerStack.last()?.current?.providerItem == true && this.filteringValue.isNotEmpty()) {
            args[DropboxUtils.DROPBOX_SEARCH_CURSOR] =
                modelExplorerStack.last()?.current?.parentId!!
        }
        args.putAll(super.getArgs(filteringValue))
        return args
    }


    override fun copy(): Boolean {
        return if (super.copy()) {
            transfer(ApiContract.Operation.DUPLICATE, false)
            true
        } else {
            false
        }
    }

    override fun getFileInfo() {
        showDialogWaiting(TAG_DIALOG_CANCEL_UPLOAD)
        fileProvider?.let { provider ->
            downloadDisposable = provider.fileInfo(itemClicked)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    { file: CloudFile? ->
                        tempFile = file
                        viewState.onDialogClose()
                        file?.let { addRecent(it) }
                        viewState.onOpenLocalFile(file)
                    }
                ) { throwable: Throwable -> fetchError(throwable) }
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
        state.isGoogleDrive = false
        state.isDropBox = true
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

    override fun refreshToken() {
        context.accountOnline?.getAccountName()?.let { accountName ->
            AccountUtils.getAccount(context, accountName)?.let { account ->
                val refreshToken = AccountUtils.getAccountData(context, account).refreshToken.orEmpty()
                val request = TokenRefreshRequest(refresh_token = refreshToken)
                context.dropboxLoginProvider.updateRefreshToken(
                    mapOf(
                        TokenRefreshRequest::refresh_token.name to request.refresh_token,
                        TokenRequest::grant_type.name to request.grant_type,
                    )
                ).subscribe({ responseRefresh ->
                    if (responseRefresh is DropboxResponse.Success) {
                        val response = responseRefresh.response as RefreshTokenResponse
                        AccountUtils.setToken(context, accountName, response.accessToken)
                        App.getApp().refreshDropboxInstance()
                        getItemsById(DropboxUtils.DROPBOX_ROOT)
                    } else viewState.onRefreshToken()
                }) { viewState.onRefreshToken() }
            }
        }
    }

    override fun fetchError(throwable: Throwable) {
        when (throwable.message) {
            DropboxUtils.DROPBOX_ERROR_EMAIL_NOT_VERIFIED -> {
                viewState.onError(context.getString(R.string.storage_dropbox_email_not_verified_error))
            }
            DropboxUtils.DROPBOX_EXPIRED_ACCESS_TOKEN -> refreshToken()
            DropboxUtils.DROPBOX_INVALID_ACCESS_TOKEN -> viewState.onRefreshToken()
            else -> super.fetchError(throwable)
        }
    }
}