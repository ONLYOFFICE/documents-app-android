package app.editors.manager.viewModels.link

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import app.documents.core.model.cloud.Access
import app.documents.core.network.common.contracts.ApiContract
import app.documents.core.network.share.models.ExternalLink
import app.documents.core.network.share.models.Share
import app.editors.manager.R
import app.editors.manager.app.App
import app.editors.manager.app.roomProvider
import app.editors.manager.ui.fragments.share.link.ShareItemType
import app.editors.manager.viewModels.base.BaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import retrofit2.HttpException

data class SharingState(
    val isLoading: Boolean = true,
    val isCreateLoading: Boolean = false,
    val requestLoading: Boolean = false,
    val sharedLinks: List<ExternalLink> = emptyList(),
    val shareList: List<Share> = emptyList(),
    val canAddLinks: Boolean = true
)

sealed class SharingEffect {
    data class Error(val message: Int) : SharingEffect()
    data class Create(val url: String) : SharingEffect()
}

abstract class BaseSharingViewModel() : BaseViewModel() {

    private val _state: MutableStateFlow<SharingState> = MutableStateFlow(SharingState())
    val state: StateFlow<SharingState> = _state.asStateFlow()

    private val _effect: MutableSharedFlow<SharingEffect> = MutableSharedFlow(1)
    val effect: SharedFlow<SharingEffect> = _effect.asSharedFlow()

    private var operationJob: Job? = null

    protected abstract suspend fun getSharedLinks(): List<ExternalLink>
    protected abstract suspend fun createExternalLink(): ExternalLink?
    protected abstract suspend fun getUsers(): List<Share>
    protected abstract suspend fun setUserAccess(
        itemId: String,
        userId: String,
        access: Int
    ): Share?

    fun fetchInfo() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val links = getSharedLinks()
                _state.value = SharingState(
                    isLoading = false,
                    sharedLinks = links,
                    shareList = getUsers(),
                    canAddLinks = links.size < 6
                )
            } catch (httpException: HttpException) {
                onError(httpException)
            }
        }
    }

    fun setUserAccess(itemId: String, userId: String, access: Access) {
        _state.update { it.copy(requestLoading = true) }
        operationJob = viewModelScope.launch {
            try {
                val result = setUserAccess(itemId, userId, access.code)
                if (result != null) fetchInfo()
            } catch (httpException: HttpException) {
                onError(httpException)
            } finally {
                _state.update { it.copy(requestLoading = false) }
            }
        }
    }

    fun createSharedLink() {
        if (state.value.isCreateLoading) return
        _state.update { it.copy(isCreateLoading = true) }
        viewModelScope.launch {
            try {
                val link = createExternalLink()
                link?.let {
                    _effect.emit(SharingEffect.Create(link.sharedTo.shareLink))
                    _state.update {
                        it.copy(
                            sharedLinks = it.sharedLinks + link,
                            canAddLinks = it.sharedLinks.size < 5
                        )
                    }
                }
            } catch (e: HttpException) {
                onError(e)
            } finally {
                _state.update { it.copy(isCreateLoading = false) }
            }
        }
    }

    private fun onError(httpException: HttpException) {
        viewModelScope.launch {
            val errorMessage = httpException.response()?.errorBody()?.string().orEmpty()
            val message = when {
                MAXIMUM_LINKS_ERROR in errorMessage -> R.string.rooms_info_create_maximum_exceed
                httpException.code() == ApiContract.HttpCodes.CLIENT_FORBIDDEN -> R.string.errors_client_forbidden
                else -> R.string.errors_unknown_error
            }
            _effect.tryEmit(SharingEffect.Error(message))
        }
    }

    companion object {
        private const val MAXIMUM_LINKS_ERROR = "The maximum number of links"

        fun factory(type: ShareItemType): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = (this[APPLICATION_KEY] as App)
                if (type is ShareItemType.Room) {
                    RoomInfoViewModel(
                        roomProvider = app.roomProvider,
                        roomId = type.id
                    )
                } else {
                    SharingViewModel(
                        roomProvider = app.roomProvider,
                        itemId = type.id,
                        shareType = type
                    )
                }
            }
        }
    }
}