package app.documents.core.manager

import android.content.Context
import android.net.Uri
import app.documents.core.model.cloud.CloudAccount
import app.documents.core.model.cloud.Recent
import app.documents.core.network.common.NetworkClient
import app.documents.core.network.manager.ManagerService
import app.documents.core.network.manager.models.explorer.CloudFile
import app.documents.core.providers.CloudFileProvider
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import lib.toolkit.base.managers.utils.EditType
import lib.toolkit.base.managers.utils.StringUtils
import java.io.File

sealed class FileOpenResult {

    class Loading : FileOpenResult()
    class Success : FileOpenResult()
}

class FileOpenRepositoryImpl(
    private val context: Context,
    private val cloudAccount: CloudAccount?,
    private val cloudFileProvider: CloudFileProvider
) : FileOpenRepository {

    private val _resultFlow: MutableSharedFlow<FileOpenResult> = MutableSharedFlow(1)
    override val resultFlow: SharedFlow<FileOpenResult> = _resultFlow.asSharedFlow()

    override fun openLocalFile(uri: Uri, extension: String, editType: EditType) {
        TODO("Not yet implemented")
    }

    override fun openLocalFile(cloudFile: CloudFile, editType: EditType) {
//        LocalContentTools.isOpenFormat(item.clearExt)
    }

    private fun openFile(file: CloudFile, editType: EditType?) {
        val path = file.id
        val uri = Uri.fromFile(File(path))
        val ext = StringUtils.getExtensionFromPath(file.id.lowercase())
        openFile(uri, ext, editType)
    }

    private fun openFile(uri: Uri, ext: String, editType: EditType?) {
//        when (StringUtils.getExtension(ext)) {
//            StringUtils.Extension.DOC, StringUtils.Extension.HTML, StringUtils.Extension.EBOOK, StringUtils.Extension.FORM -> {
//                if (ext.contains(LocalContentTools.HWP_EXTENSION) || ext.contains(LocalContentTools.HWPX_EXTENSION)) {
//                    viewState.onShowEditors(uri, EditorsType.DOCS, EditType.VIEW)
//                } else {
//                    viewState.onShowEditors(uri, EditorsType.DOCS, editType)
//                }
//            }
//
//            StringUtils.Extension.SHEET -> {
//                viewState.onShowEditors(uri, EditorsType.CELLS, editType)
//            }
//
//            StringUtils.Extension.PRESENTATION -> {
//                viewState.onShowEditors(uri, EditorsType.PRESENTATION, editType)
//            }
//
//            StringUtils.Extension.PDF -> {
//                if (FileUtils.isOformPdf(context.contentResolver.openInputStream(uri))) {
//                    viewState.onShowEditors(uri, EditorsType.DOCS, editType ?: EditType.FILL)
//                } else {
//                    viewState.onShowPdf(uri)
//                }
//            }
//
//            StringUtils.Extension.IMAGE, StringUtils.Extension.IMAGE_GIF, StringUtils.Extension.VIDEO_SUPPORT -> showMedia(
//                uri
//            )
//
//            else -> viewState.onError(context.getString(R.string.error_unsupported_format))
//        }
    }

    override fun openCloudFile(id: String, editType: EditType) {

    }

    override fun openRecentFile(recent: Recent, editType: EditType) {
//        if (recentItem.source == null) {
//            recentItem.path.let { path ->
//                Uri.parse(path)?.let { uri ->
//                    if (uri.scheme != null) {
//                        openLocalFile(uri)
//                    } else {
//                        openLocalFile(Uri.fromFile(File(path)))
//                    }
//                    addRecent(recentItem)
//                }
//            }
//        } else {
//            presenterScope.launch {
//                if (checkCloudFile(recentItem)) {
//                    addRecent(recentItem)
//                }
//            }
//        }
    }

    private suspend fun checkCloudFile(recent: Recent): Boolean {
//        recent.ownerId?.let { id ->
//            cloudDataSource.getAccount(id)?.let { recentAccount ->
//                if (recentAccount.id != accountPreferences.onlineAccountId) {
//                    withContext(Dispatchers.Main) {
//                        viewState.onError(context.getString(R.string.error_recent_enter_account))
//                    }
//                    return false
//                } else if (recentAccount.isWebDav) {
//                    openWebDavFile(recent)
//                } else if (recentAccount.isDropbox || recentAccount.isGoogleDrive || recentAccount.isOneDrive) {
//                    openStorageFile(recent = recent, recentAccount)
//                } else {
//                    openCloudFile(recent)
//                }
//                return true
//            }
//        }
        return false
    }

    private fun openStorageFile(recent: Recent, recentAccount: CloudAccount) {
//        when {
//            recentAccount.isOneDrive -> OneDriveFileProvider(context, OneDriveStorageHelper())
//            recentAccount.isGoogleDrive -> GoogleDriveFileProvider(context, GoogleDriveStorageHelper())
//            recentAccount.isDropbox -> DropboxFileProvider(context, DropboxStorageHelper())
//            else -> null
//        }?.let { provider ->
//            showDialogWaiting(TAG_DIALOG_CANCEL_DOWNLOAD)
//            val cloudFile = CloudFile().apply {
//                title = recent.name
//                id = recent.fileId
//                fileExst = StringUtils.getExtensionFromPath(recent.name)
//                pureContentLength = recent.size
//            }
//            downloadDisposable = provider.fileInfo(cloudFile)
//                .subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(
//                    { file: CloudFile? ->
//                        viewState.onDialogClose()
//                        file?.let { addRecent(it) }
//                        viewState.onOpenLocalFile(file, null)
//                    }
//                ) { throwable: Throwable -> fetchError(throwable) }
//        } ?: run {
//            viewState.onError(context.getString(R.string.error_recent_enter_account))
//        }
    }

    private fun openLocalFile(uri: Uri) {
//        val name = getName(context, uri)
//        when (StringUtils.getExtension(StringUtils.getExtensionFromPath(name.lowercase(Locale.ROOT)))) {
//            StringUtils.Extension.DOC, StringUtils.Extension.FORM -> {
//                viewState.onOpenFile(OpenState.Docs(uri))
//            }
//
//            StringUtils.Extension.SHEET -> viewState.onOpenFile(OpenState.Cells(uri))
//            StringUtils.Extension.PRESENTATION -> viewState.onOpenFile(OpenState.Slide(uri))
//            StringUtils.Extension.PDF -> viewState.onOpenFile(OpenState.Pdf(uri, FileUtils.isOformPdf(context.contentResolver.openInputStream(uri))))
//            StringUtils.Extension.IMAGE, StringUtils.Extension.IMAGE_GIF, StringUtils.Extension.VIDEO_SUPPORT -> {
//                viewState.onOpenFile(OpenState.Media(getImages(uri), false))
//            }
//
//            else -> viewState.onError(context.getString(R.string.error_unsupported_format))
//        }
    }

    private suspend fun openWebDavFile(recent: Recent) {
//        cloudDataSource.getAccount(recent.ownerId ?: "")?.let {
//            val provider = context.webDavFileProvider
//            val cloudFile = CloudFile().apply {
//                title = recent.name
//                id = recent.fileId
//                fileExst = StringUtils.getExtensionFromPath(recent.name)
//                pureContentLength = recent.size
//            }
//            withContext(Dispatchers.Main) {
//                if (StringUtils.isImage(cloudFile.fileExst)) {
//                    viewState.onOpenFile(OpenState.Media(getWebDavImage(recent), true))
//                } else {
//                    disposable.add(provider.fileInfo(cloudFile)
//                        .doOnSubscribe {
//                            viewState.onDialogWaiting(
//                                context.getString(R.string.dialogs_wait_title),
//                                null
//                            )
//                        }
//                        .subscribeOn(Schedulers.io())
//                        .observeOn(AndroidSchedulers.mainThread())
//                        .subscribe({ file ->
//                            temp = file
//                            viewState.onDialogClose()
//                            openLocalFile(Uri.parse(file.webUrl))
//                            getRecentFiles(checkFiles = false)
//                        }, ::fetchError)
//                    )
//                }
//            }
//        }
    }

    private suspend fun openCloudFile(recent: Recent) {
//        cloudDataSource.getAccount(recent.ownerId ?: "")?.let { account ->
//            AccountUtils.getToken(
//                context,
//                Account(account.accountName, context.getString(lib.toolkit.base.R.string.account_type))
//            )?.let {
//                val fileProvider = context.cloudFileProvider
//                disposable.add(
//                    fileProvider.fileInfo(CloudFile().apply {
//                        id = recent.fileId
//                    }).flatMap { cloudFile ->
//                        fileProvider.opeEdit(cloudFile, cloudFile.allowShare && !account.isVisitor, null).toObservable()
//                            .zipWith(Observable.fromCallable { cloudFile }) { info, file ->
//                                return@zipWith arrayOf(file, info)
//                            }
//                    }.subscribe({ response ->
//                        checkExt(response[0] as CloudFile, response[1] as String)
//                    }, { throwable ->
//                        if (throwable is HttpException) {
//                            when (throwable.code()) {
//                                ApiContract.HttpCodes.CLIENT_UNAUTHORIZED ->
//                                    viewState.onError(context.getString(R.string.errors_client_unauthorized))
//
//                                ApiContract.HttpCodes.CLIENT_FORBIDDEN ->
//                                    viewState.onError(context.getString(R.string.error_recent_account))
//
//                                else ->
//                                    onErrorHandle(throwable.response()?.errorBody(), throwable.code())
//                            }
//                        } else {
//                            viewState.onError(context.getString(R.string.error_recent_account))
//                        }
//                    })
//                )
//            } ?: run {
//                viewState.onError(context.getString(R.string.error_recent_enter_account))
//            }
//        }
    }

    private fun checkExt(file: CloudFile, info: String) {
//        if (file.rootFolderType.toInt() != ApiContract.SectionType.CLOUD_TRASH) {
//            when (StringUtils.getExtension(file.fileExst)) {
//                StringUtils.Extension.DOC, StringUtils.Extension.FORM, StringUtils.Extension.SHEET,
//                StringUtils.Extension.PRESENTATION, StringUtils.Extension.IMAGE, StringUtils.Extension.IMAGE_GIF,
//                StringUtils.Extension.VIDEO_SUPPORT, StringUtils.Extension.PDF -> {
//                    checkSdkVersion { isCheck ->
//                        if (isCheck) {
//                            viewState.onOpenDocumentServer(/* file = */ file, /* info = */ info, /* type = */ null)
//                        } else {
//                            downloadTempFile(file, null)
//                        }
//                    }
//                }
//
//                else -> viewState.onError(context.getString(R.string.error_unsupported_format))
//            }
//        } else {
//            viewState.onError(context.getString(R.string.error_recent_account))
//        }
    }

    // refactor with serialization
    override fun openCloudFile(id: String, portal: String, token: String, editType: EditType) {
        try {
            val api = NetworkClient.getRetrofit<ManagerService>(portal, token, context)
//            val response = withContext(Dispatchers.IO) {
//                JSONObject(
//                    api.openFile(id).blockingGet().body()?.string()
//                ).getJSONObject(
//                    KEY_RESPONSE
//                )
//
//            }
//
//            val json = withContext(Dispatchers.IO) {
//                JSONObject(api.getDocService().blockingGet().body()?.string())
//            }
//
//            val docService = if (json.optJSONObject(KEY_RESPONSE) != null) {
//                json.getJSONObject(KEY_RESPONSE).getString("docServiceUrlApi")
//                    .replace(STATIC_DOC_URL, "")
//            } else {
//                json.getString(KEY_RESPONSE)
//                    .replace(STATIC_DOC_URL, "")
//            }
//
//            val result = withContext(Dispatchers.IO) {
//                response
//                    .put("url", docService)
//                    .put("fileId", id)
//                    .put("canShareable", false)
//            }
//
//            withContext(Dispatchers.Main) {
//                viewState.onDialogClose()
//                delay(50)
//                viewState.onOpenDocumentServer(
//                    /* file = */ CloudFile().apply {
//                        id = model.file?.id.toString()
//                        title = model.file?.title ?: ""
//                        fileExst = model.file?.extension ?: ""
//                    },
//                    /* info = */ result.toString(),
//                    /* type = */ null
//                )
//            }
        } catch (e: Exception) {
//            fetchError(e)
        }
    }

    override fun openCloudFile(cloudFile: CloudFile, editType: EditType) {
//        if (cloudFile.isPdfForm && editType == EditType.Fill()) {
//            if (isUserSection) {
//                viewState.showFillFormChooserFragment()
//            } else {
//                fillPdfForm()
//            }
//            return
//        }
//
//        val extension = cloudFile.fileExst
//        when (StringUtils.getExtension(extension)) {
//            StringUtils.Extension.DOC,
//            StringUtils.Extension.SHEET,
//            StringUtils.Extension.PRESENTATION,
//            StringUtils.Extension.FORM,
//            StringUtils.Extension.PDF -> {
//                checkSdkVersion { result ->
//                    if (result) {
//                        openDocumentServer(
//                            cloudFile = cloudFile,
//                            canShareable = isItemShareable,
//                            editType = if (LocalContentTools.isOpenFormat(cloudFile.clearExt) ||
//                                cloudFile.access == Access.Read
//                            ) {
//                                EditType.View
//                            } else {
//                                editType
//                            }
//                        )
//                    } else {
//                        downloadTempFile(
//                            cloudFile = cloudFile,
//                            editType = if (cloudFile.access == Access.Read) {
//                                EditType.View
//                            } else {
//                                editType
//                            }
//                        )
//                    }
//                }
//            }
//
//            StringUtils.Extension.IMAGE,
//            StringUtils.Extension.IMAGE_GIF,
//            StringUtils.Extension.VIDEO_SUPPORT -> {
//                viewState.onFileMedia(getListMedia(cloudFile.id), false)
//            }
//
//            else -> viewState.onFileDownloadPermission()
//        }
//        FirebaseUtils.addAnalyticsOpenEntity(account.portalUrl, extension)
    }

    private fun openDocumentServer(
        cloudFile: CloudFile,
        canShareable: Boolean,
        editType: EditType?
    ) {
//        with(fileProvider as CloudFileProvider) {
//            val token = AccountUtils.getToken(context, account.accountName)
//            disposable.add(
//                openDocument(cloudFile, token, canShareable, editType).subscribe({ result ->
//                    viewState.onDialogClose()
//                    if (result.isPdf) {
//                        downloadTempFile(cloudFile, null)
//                    } else if (result.info != null) {
//                        viewState.onOpenDocumentServer(cloudFile, result.info, editType)
//                    }
//                }) { error ->
//                    fetchError(error)
//                }
//            )
//        }
//        addRecent(cloudFile)
    }

    private fun downloadTempFile(cloudFile: CloudFile, editType: EditType?)/*: Single<File> */{
//        return context.cloudFileProvider
//            .getCachedFile(context, cloudFile, account.accountName)
//            .subscribeOn(Schedulers.io())
//            .observeOn(AndroidSchedulers.mainThread())
    }

    private fun checkSdkVersion(
        version: String? = null,
        onResult: (isCoauthoring: Boolean) -> Unit
    ) {
//        FirebaseUtils.getSdk { allowCoauthoring, checkSdkFully ->
//            if (allowCoauthoring) {
//                onResult(false)
//                return@getSdk
//            }
//
//            val webSdk = account
//                .portal
//                .version
//                .documentServerVersion
//                .replace(".", "")
//
//            if (webSdk.isEmpty()) {
//                onResult(false)
//                return@getSdk
//            }
//
//            val localSdk = FileUtils.readSdkVersion(context).replace(".", "")
//
//            var maxVersionIndex = 2
//
//            if (!checkSdkFully) {
//                maxVersionIndex = 1
//            }
//
//            for (i in 0..maxVersionIndex) {
//                if (webSdk[i] != localSdk[i]) {
//                    onResult(false)
//                    return@getSdk
//                }
//            }
//
//            onResult(true)
//        }
    }
}