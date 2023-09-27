package app.editors.manager.mvp.presenters.storages

import android.net.Uri
import androidx.work.Data
import androidx.work.OneTimeWorkRequest
import app.documents.core.network.common.contracts.ApiContract
import app.documents.core.network.common.utils.DropboxUtils
import app.documents.core.network.manager.models.explorer.CloudFile
import app.documents.core.network.manager.models.explorer.Explorer
import app.documents.core.network.manager.models.explorer.Item
import app.documents.core.providers.DropboxFileProvider
import app.editors.manager.R
import app.editors.manager.app.App
import app.editors.manager.app.accountOnline
import app.editors.manager.app.dropboxLoginProvider
import app.editors.manager.managers.providers.DropboxStorageHelper
import app.editors.manager.managers.utils.FirebaseUtils
import app.editors.manager.managers.works.BaseDownloadWork
import app.editors.manager.managers.works.BaseStorageUploadWork
import app.editors.manager.managers.works.dropbox.DownloadWork
import app.editors.manager.managers.works.dropbox.UploadWork
import app.editors.manager.mvp.views.base.BaseStorageDocsView
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.launch
import lib.toolkit.base.managers.utils.AccountUtils
import lib.toolkit.base.managers.utils.KeyboardUtils
import moxy.presenterScope
import retrofit2.HttpException

class DocsDropboxPresenter : BaseStorageDocsPresenter<BaseStorageDocsView>() {

    private val dropboxFileProvider: DropboxFileProvider by lazy {
        DropboxFileProvider(
            context = context,
            helper = DropboxStorageHelper()
        )
    }

    init {
        App.getApp().appComponent.inject(this)
    }

    override val externalLink: Unit
        get() {
            itemClicked?.let { item ->
                dropboxFileProvider.share(item.id)?.let { externalLinkResponse ->
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
                            viewState.onSnackBar(context.getString(R.string.share_clipboard_external_copied))
                        }) { throwable: Throwable -> fetchError(throwable) }
                    )
                }
            }
        }

    override fun getProvider() {
        fileProvider?.let {
            getItemsById(DropboxUtils.DROPBOX_ROOT)
        } ?: run {
            fileProvider = dropboxFileProvider
            getItemsById(DropboxUtils.DROPBOX_ROOT)
        }
    }

    override fun startDownload(downloadTo: Uri, item: Item?) {
        val data = Data.Builder()
            .putString(BaseDownloadWork.FILE_ID_KEY, item?.id)
            .putString(BaseDownloadWork.FILE_URI_KEY, downloadTo.toString())
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


    override fun upload(uri: Uri?, uris: List<Uri>?, tag: String?) {
        val uploadUris = mutableListOf<Uri>()
        var index = 0

        uri?.let {
            uploadUris.add(uri)
        } ?: run {
            uris?.let {
                while (index != uris.size) {
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

    override fun getArgs(filteringValue: String?): Map<String, String> {
        val args = mutableMapOf<String, String>()
        if (modelExplorerStack.last()?.current?.providerItem == true) {
            args[DropboxUtils.DROPBOX_CONTINUE_CURSOR] =
                modelExplorerStack.last()?.current?.parentId!!
        }
        if (modelExplorerStack.last()?.current?.providerItem == true && this.filteringValue.isNotEmpty()) {
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

    override fun refreshToken() {
        presenterScope.launch {
            context.accountOnline?.getAccountName()?.let { accountName ->
                AccountUtils.getAccount(context, accountName)?.let { account ->
                    try {
                        val refreshToken = AccountUtils.getAccountData(context, account).refreshToken.orEmpty()
                        val refreshResponse = context.dropboxLoginProvider.updateRefreshToken(refreshToken)
                        AccountUtils.setToken(context, accountName, refreshResponse.accessToken)
                        App.getApp().refreshDropboxInstance()
                        getItemsById(DropboxUtils.DROPBOX_ROOT)
                    } catch (e: HttpException) {
                        FirebaseUtils.addCrash(e)
                        viewState.onAuthorization()
                    }
                }
            }
        }
    }

    override fun fetchError(throwable: Throwable) {
        when (throwable.message) {
            DropboxUtils.DROPBOX_ERROR_EMAIL_NOT_VERIFIED -> {
                viewState.onError(context.getString(R.string.storage_dropbox_email_not_verified_error))
            }
            DropboxUtils.DROPBOX_EXPIRED_ACCESS_TOKEN -> refreshToken()
            DropboxUtils.DROPBOX_INVALID_ACCESS_TOKEN -> viewState.onAuthorization()
            else -> super.fetchError(throwable)
        }
    }
}