package app.editors.manager.viewModels.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.documents.core.network.manager.ManagerService
import app.documents.core.network.share.ShareService
import app.documents.core.network.share.models.Share
import app.documents.core.network.share.models.request.RequestExternal
import app.documents.core.network.share.models.request.RequestShare
import app.documents.core.network.share.models.request.RequestShareItem
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
import lib.toolkit.base.managers.utils.StringUtils
import kotlin.time.Duration.Companion.milliseconds

data class ShareState(
    val loading: Boolean = false,
    val requestLoading: Boolean = false,
    val users: List<Share> = emptyList(),
    val groups: List<Share> = emptyList(),
    val externalLink: Share = Share(),
    val extension: StringUtils.Extension = StringUtils.Extension.UNKNOWN,
    val webUrl: String? = null
)

sealed class ShareEffect {
    data class Error(val throwable: Throwable) : ShareEffect()
    data class InternalLink(val url: String) : ShareEffect()
}

@OptIn(FlowPreview::class)
class ShareViewModel(
    private val itemId: String,
    private val folder: Boolean,
    private val shareApi: ShareService,
    private val managerApi: ManagerService
) : ViewModel() {

    companion object {

        private const val TAG_FOLDER_PATH = "products/files/#"
    }

    private var cachedShareList: List<Share> = emptyList()

    private val _state: MutableStateFlow<ShareState> = MutableStateFlow(ShareState(loading = true))
    val state: StateFlow<ShareState> = _state.asStateFlow()

    private val _effect: MutableSharedFlow<ShareEffect> = MutableSharedFlow(1)
    val effect: SharedFlow<ShareEffect> = _effect.asSharedFlow()

    private val searchFlow: MutableSharedFlow<String> = MutableSharedFlow(1)

    init {
        fetchShareList()
        collectSearchFlow()
    }

    private fun fetchShareList() {
        viewModelScope.launch {
            try {
                val shareList = if (folder) {
                    shareApi.getShareFolder(itemId).response
                } else {
                    val fileInfo = managerApi.getCloudFileInfo(itemId).response
                    _state.update {
                        it.copy(
                            webUrl = fileInfo.webUrl,
                            extension = StringUtils.getExtension(fileInfo.fileExst)
                        )
                    }
                    shareApi.getShareFile(itemId).response
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
            if (folder) {
                _effect.tryEmit(ShareEffect.InternalLink("$TAG_FOLDER_PATH$itemId"))
                return@launch
            }

            if (!state.value.webUrl.isNullOrEmpty()) {
                _effect.tryEmit(ShareEffect.InternalLink(state.value.externalLink.sharedTo.shareLink))
            } else {
                _effect.tryEmit(ShareEffect.Error(NullPointerException()))
            }
        }
    }

    fun search(value: String) {
        searchFlow.tryEmit(value)
    }

    fun setExternalLinkAccess(access: Int) {
        request {
            shareApi.getExternalLink(itemId, RequestExternal(share = access.toString()))
            _state.update { it.copy(externalLink = it.externalLink.copy(access = access.toString())) }
        }
    }

    fun setUserAccess(userId: String, access: Int) {
        if (!folder) {
            request {
                shareApi.setFileAccess(itemId, RequestShare(listOf(RequestShareItem(userId, access.toString()))))
                _state.update { state ->
                    state.copy(
                        users = state.users.map { user ->
                            if (user.sharedTo.id == userId) {
                                user.copy(access = access.toString())
                            } else user
                        }
                    )
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