package app.editors.manager.mvp.presenters.storages

import android.accounts.Account
import android.net.Uri
import androidx.work.Data
import androidx.work.OneTimeWorkRequest
import app.documents.core.network.common.contracts.ApiContract
import app.documents.core.network.common.utils.OneDriveUtils
import app.documents.core.network.manager.models.explorer.CloudFile
import app.documents.core.network.manager.models.explorer.Explorer
import app.documents.core.network.manager.models.explorer.Item
import app.documents.core.network.storages.onedrive.api.OneDriveResponse
import app.documents.core.network.storages.onedrive.models.request.ExternalLinkRequest
import app.documents.core.network.storages.onedrive.models.response.AuthResponse
import app.documents.core.providers.OneDriveFileProvider
import app.editors.manager.R
import app.editors.manager.app.App
import app.editors.manager.app.oneDriveLoginProvider
import app.editors.manager.managers.providers.OneDriveStorageHelper
import app.editors.manager.managers.works.BaseDownloadWork
import app.editors.manager.managers.works.BaseStorageUploadWork
import app.editors.manager.managers.works.onedrive.DownloadWork
import app.editors.manager.managers.works.onedrive.UploadWork
import app.editors.manager.mvp.views.base.BaseStorageDocsView
import app.editors.manager.ui.dialogs.ContextBottomDialog
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import lib.toolkit.base.managers.utils.AccountUtils
import lib.toolkit.base.managers.utils.KeyboardUtils
import lib.toolkit.base.managers.utils.StringUtils
import lib.toolkit.base.managers.utils.TimeUtils
import moxy.InjectViewState
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
        val account = Account(App.getApp().appComponent.accountOnline?.getAccountName(), context.getString(lib.toolkit.base.R.string.account_type))
        val accData = AccountUtils.getAccountData(context, account)
        disposable.add(context.oneDriveLoginProvider.refreshToken(accData.refreshToken.orEmpty())
            .subscribe { oneDriveResponse ->
                when (oneDriveResponse) {
                    is OneDriveResponse.Success -> {
                        val response = oneDriveResponse.response as AuthResponse
                        AccountUtils.setAccountData(context, account, accData.copy(accessToken = response.access_token))
                        AccountUtils.setToken(context, account, response.access_token)
                        App.getApp().refreshOneDriveInstance()
                        getItemsById("")
                    }
                    is OneDriveResponse.Error -> viewState.onError(oneDriveResponse.error.message)
                }
            })
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

    override fun onContextClick(item: Item, position: Int, isTrash: Boolean) {
        onClickEvent(item, position)
        isContextClick = true
        val state = ContextBottomDialog.State()
        state.title = itemClickedTitle
        state.info = TimeUtils.formatDate(itemClickedDate)
        state.isFolder = !isClickedItemFile
        state.isDocs = isClickedItemDocs
        state.isWebDav = false
        state.isOneDrive = true
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