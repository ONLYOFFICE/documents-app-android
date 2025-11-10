package app.editors.manager.viewModels.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.documents.core.model.cloud.Access
import app.documents.core.network.manager.ManagerService
import app.documents.core.network.manager.models.explorer.AccessTarget
import app.documents.core.network.share.ShareService
import app.documents.core.network.share.models.Share
import app.documents.core.network.share.models.request.RequestExternal
import app.documents.core.network.share.models.request.RequestShare
import app.documents.core.network.share.models.request.RequestShareItem
import app.editors.manager.managers.tools.ShareData
import app.editors.manager.mvp.models.ui.AccessUI
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

data class ShareState(
    val loading: Boolean = false,
    val requestLoading: Boolean = false,
    val users: List<Share> = emptyList(),
    val groups: List<Share> = emptyList(),
    val externalLink: Share = Share(),
    val webUrl: String? = null,
    val folder: Boolean = false,
    val accessList: List<AccessUI> = emptyList()
)

sealed class ShareEffect {
    data class Error(val throwable: Throwable) : ShareEffect()
    data class InternalLink(val url: String, val withPortal: Boolean) : ShareEffect()
}

@OptIn(FlowPreview::class)
class ShareViewModel(
    private val shareData: ShareData,
    private val shareApi: ShareService,
    private val managerApi: ManagerService
) : ViewModel() {

    companion object {

        private const val TAG_FOLDER_PATH = "products/files/#"
    }

    private var cachedShareList: List<Share> = emptyList()

    private val _state: MutableStateFlow<ShareState> = MutableStateFlow(
        ShareState(
            loading = true,
            folder = shareData.isFolder,
            accessList = shareData.getAccessList(AccessTarget.User)
        )
    )
    val state: StateFlow<ShareState> = _state.asStateFlow()

    private val _effect: MutableSharedFlow<ShareEffect> = MutableSharedFlow(1)
    val effect: SharedFlow<ShareEffect> = _effect.asSharedFlow()

    private val searchFlow: MutableSharedFlow<String> = MutableSharedFlow(1)

    init {
        fetchShareList()
        collectSearchFlow()
    }

    fun fetchShareList() {
        viewModelScope.launch {
            try {
                val shareList = if (shareData.isFolder) {
                    shareApi.getShareFolder(shareData.itemId).response
                } else {
                    val fileInfo = managerApi.getCloudFileInfo(shareData.itemId).response
                    _state.update { it.copy(webUrl = fileInfo.webUrl) }
                    shareApi.getShareFile(shareData.itemId).response
                }

                cachedShareList = shareList
                mapShareList()
            } catch (e: Exception) {
                _effect.emit(ShareEffect.Error(e))
            }
        }
    }

    private fun collectSearchFlow() {
        viewModelScope.launch {
            searchFlow
                .distinctUntilChanged()
                .debounce(500.milliseconds)
                .collect(::mapShareList)
        }
    }

    private fun mapShareList(searchValue: String = "") {
        val users = mutableListOf<Share>()
        val groups = mutableListOf<Share>()
        var externalLink = Share()

        cachedShareList.forEach { share ->
            with(share.sharedTo) {
                when {
                    shareLink.isNotEmpty() -> externalLink = share
                    userName.isNotEmpty() -> {
                        if (displayNameHtml.startsWith(searchValue, true)) {
                            users.add(share)
                        }
                    }
                    name.isNotEmpty() -> {
                        if (!manager.isNullOrEmpty() && name.startsWith(searchValue, true)) {
                            groups.add(share)
                        }
                    }
                }
            }
        }

        _state.update {
            it.copy(
                loading = false,
                users = users,
                groups = groups,
                externalLink = externalLink
            )
        }
    }

    fun copyInternalLink() {
        viewModelScope.launch {
            if (shareData.isFolder) {
                _effect.tryEmit(ShareEffect.InternalLink("$TAG_FOLDER_PATH${shareData.itemId}", false))
                return@launch
            }

            state.value.webUrl?.let { url ->
                _effect.tryEmit(ShareEffect.InternalLink(url, true))
            } ?: run {
                _effect.tryEmit(ShareEffect.Error(NullPointerException()))
            }
        }
    }

    fun search(value: String) {
        searchFlow.tryEmit(value)
    }

    fun setExternalLinkAccess(access: Access) {
        request {
            shareApi.getExternalLink(shareData.itemId, RequestExternal(share = access.code))
            _state.update { it.copy(externalLink = it.externalLink.copy(_access = access.code)) }
        }
    }

    fun setMemberAccess(userId: String, access: Access, isGroup: Boolean) {
        request {
            val request = RequestShare(listOf(RequestShareItem(userId, access.code)))
            if (!shareData.isFolder) shareApi.setFileAccess(shareData.itemId, request) else shareApi.setFolderAccess(shareData.itemId, request)
            _state.update { state ->
                if (access == Access.None) {
                    if (isGroup) {
                        state.copy(
                            groups = state.groups
                                .toMutableList()
                                .apply { removeIf { it.sharedTo.id == userId } }
                        )

                    } else {
                        state.copy(
                            users = state.users
                                .toMutableList()
                                .apply { removeIf { it.sharedTo.id == userId } }
                        )
                    }
                } else {
                    if (isGroup) {
                        state.copy(
                            groups = state.groups.map { group ->
                                if (group.sharedTo.id == userId)
                                    group.copy(_access = access.code) else
                                    group
                            }
                        )
                    } else {
                        state.copy(
                            users = state.users.map { user ->
                                if (user.sharedTo.id == userId)
                                    user.copy(_access = access.code) else
                                    user
                            }
                        )
                    }
                }
            }
        }
    }

    fun request(block: suspend () -> Unit) {
        viewModelScope.launch {
            try {
                _state.update { it.copy(requestLoading = true) }
                block.invoke()
            } catch (e: Exception) {
                _effect.tryEmit(ShareEffect.Error(e))
            } finally {
                _state.update { it.copy(requestLoading = false) }
            }
        }
    }
}