package app.documents.core.providers

import android.net.Uri
import androidx.core.net.toFile
import androidx.core.net.toUri
import app.documents.core.account.AccountRepository
import app.documents.core.model.cloud.PortalProvider
import app.documents.core.model.cloud.Recent
import app.documents.core.network.common.Result
import app.documents.core.network.common.asResult
import app.documents.core.network.manager.models.explorer.CloudFile
import app.documents.core.network.manager.models.explorer.CloudFolder
import app.documents.core.network.manager.models.explorer.Explorer
import app.documents.core.network.manager.models.explorer.Item
import app.documents.core.network.manager.models.explorer.Operation
import app.documents.core.network.manager.models.request.RequestCreate
import app.documents.core.network.manager.models.response.ResponseOperation
import io.reactivex.Observable
import io.reactivex.Single
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import lib.toolkit.base.managers.utils.EditType
import lib.toolkit.base.managers.utils.StringUtils
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Response
import java.io.File
import javax.inject.Inject

class RecentFileProvider @Inject constructor(
    private val accountRepository: AccountRepository,
    private val cloudFileProvider: CloudFileProvider,
    private val webDavFileProvider: WebDavFileProvider,
    private val oneDriveFileProvider: OneDriveFileProvider,
    private val googleDriveFileProvider: GoogleDriveFileProvider,
    private val dropboxFileProvider: DropboxFileProvider
) : BaseFileProvider, BaseCloudFileProvider {

    override fun openFile(
        cloudFile: CloudFile,
        editType: EditType,
        canBeShared: Boolean
    ): Flow<Result<FileOpenResult>> = flowOf()

    override fun updateDocument(id: String, body: MultipartBody.Part): Single<Boolean> {
        return cloudFileProvider.updateDocument(id, body)
    }

    fun openFile(recent: Recent, editType: EditType): Flow<Result<FileOpenResult>> {
        return flow {
            emit(FileOpenResult.Loading())
            if (recent.source == null) {
                openLocalFile(recent.path, editType)
            } else {
                val owner = recent.ownerId ?: throw RuntimeException()
                val recentAccount = accountRepository.getAccount(owner)

                if (accountRepository.getOnlineAccount() == null) {
                    emit(FileOpenResult.RecentNoAccount())
                    return@flow
                }

                if (recentAccount == null || recentAccount.id != accountRepository.getOnlineAccount()?.id) {
                    emit(FileOpenResult.RecentFileNotFound())
                    return@flow
                }

                when (val provider = recentAccount.portal.provider) {
                    is PortalProvider.Storage -> {
                        openStorageFile(recent, provider, editType)
                    }

                    is PortalProvider.Webdav -> {
                        openWebDavFile(recent, editType)
                    }

                    else -> {
                        openCloudFile(recent, editType)
                    }
                }
            }
        }.asResult()
    }

    fun updateWebdavDocument(uri: Uri, id: String): Observable<Int> {
        return webDavFileProvider.upload(id, listOf(uri))
    }

    private suspend fun FlowCollector<FileOpenResult>.openLocalFile(
        path: String,
        editType: EditType
    ) {
        val uri = path.toUri()
        val file = if (uri.scheme != null) {
            uri.toFile()
        } else {
            File(path)
        }
        emit(FileOpenResult.OpenLocally(file, "", editType))
    }

    private suspend fun FlowCollector<FileOpenResult>.openCloudFile(
        recent: Recent,
        editType: EditType
    ) {
        cloudFileProvider.openFile(
            id = recent.fileId,
            editType = editType,
            canBeShared = false
        ).collect { result ->
            when (result) {
                is Result.Error -> emit(FileOpenResult.RecentFileNotFound())
                is Result.Success<FileOpenResult> -> emit(result.result)
            }
        }
    }

    private suspend fun FlowCollector<FileOpenResult>.openStorageFile(
        recent: Recent,
        provider: PortalProvider,
        editType: EditType
    ) {
        when (provider) {
            is PortalProvider.Onedrive -> oneDriveFileProvider
            is PortalProvider.GoogleDrive -> googleDriveFileProvider
            is PortalProvider.Dropbox -> dropboxFileProvider
            else -> return emit(FileOpenResult.RecentNoAccount())
        }.openFile(
            cloudFile = CloudFile().apply {
                title = recent.name
                id = recent.fileId
                fileExst = StringUtils.getExtensionFromPath(recent.name)
                pureContentLength = recent.size
            },
            editType = editType,
            canBeShared = false
        ).collect { result ->
            when (result) {
                is Result.Error -> emit(FileOpenResult.RecentFileNotFound())
                is Result.Success<FileOpenResult> -> emit(result.result)
            }
        }
    }

    private suspend fun FlowCollector<FileOpenResult>.openWebDavFile(
        recent: Recent,
        editType: EditType
    ) {
        webDavFileProvider.openFile(
            cloudFile = CloudFile().apply {
                title = recent.name
                id = recent.fileId
                fileExst = StringUtils.getExtensionFromPath(recent.name)
                pureContentLength = recent.size
            },
            editType = editType,
            canBeShared = false
        ).collect { result ->
            when (result) {
                is Result.Error -> emit(FileOpenResult.RecentFileNotFound())
                is Result.Success<FileOpenResult> -> emit(result.result)
            }
        }
    }

    override fun getFiles(id: String?, filter: Map<String, String>?): Observable<Explorer> {
        return Observable.just(Explorer())
    }

    override fun createFile(folderId: String, title: String): Observable<CloudFile> {
        return Observable.just(CloudFile())
    }

    override fun createFolder(folderId: String, body: RequestCreate): Observable<CloudFolder> {
        return Observable.just(CloudFolder())
    }

    override fun rename(item: Item, newName: String, version: Int?): Observable<Item> {
        return Observable.just(CloudFile())
    }

    override fun delete(items: List<Item>, from: CloudFolder?): Observable<List<Operation>> {
        return Observable.just(emptyList())
    }

    override fun transfer(
        items: List<Item>,
        to: CloudFolder,
        conflict: Int,
        isMove: Boolean,
        isOverwrite: Boolean
    ): Observable<List<Operation>>? {
        return null
    }

    override fun fileInfo(item: Item?): Observable<CloudFile> {
        return Observable.just(CloudFile())
    }

    override fun getStatusOperation(): ResponseOperation? {
        return null
    }

    override fun terminate(): Observable<List<Operation>>? {
        return null
    }

    override fun getDownloadResponse(
        cloudFile: CloudFile,
        token: String?
    ): Single<Response<ResponseBody>> {
        return Single.never()
    }
}