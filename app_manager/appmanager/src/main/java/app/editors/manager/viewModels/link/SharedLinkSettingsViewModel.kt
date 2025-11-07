package app.editors.manager.viewModels.link

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.documents.core.network.share.models.ExternalLink
import app.documents.core.providers.RoomProvider
import app.editors.manager.managers.tools.ShareData
import app.editors.manager.ui.fragments.share.link.SharedLinkLifeTime
import app.editors.manager.ui.fragments.share.link.SharedLinkLifeTimeWithAmount
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import lib.toolkit.base.managers.utils.TimeUtils
import retrofit2.HttpException
import java.util.Calendar
import java.util.TimeZone

sealed class SharedLinkSettingsEffect {

    data object Close : SharedLinkSettingsEffect()
    data object Delete : SharedLinkSettingsEffect()
    data class Error(val code: Int? = null) : SharedLinkSettingsEffect()
}

class SharedLinkSettingsViewModel(
    externalLink: ExternalLink,
    expired: String?,
    private val roomProvider: RoomProvider,
    private val shareData: ShareData
) : ViewModel() {

    private val _state: MutableStateFlow<ExternalLink> = MutableStateFlow(
        externalLink.copy(sharedTo = externalLink.sharedTo.copy(expirationDate = expired))
    )
    val state: StateFlow<ExternalLink> = _state.asStateFlow()

    private val _loading: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    private val _effect: MutableSharedFlow<SharedLinkSettingsEffect> = MutableSharedFlow(1)
    val effect: SharedFlow<SharedLinkSettingsEffect> = _effect.asSharedFlow()

    fun setInternal(internal: Boolean) {
        tryRequest {
            if (state.value.sharedTo.internal != internal) {
                val link = state.value.copy(sharedTo = state.value.sharedTo.copy(internal = internal))
                roomProvider.updateSharedLink(shareData.itemId, link, shareData.isFolder)
                _state.value = link
            }
        }
    }

    fun regenerate() {
        setLifeTime(SharedLinkLifeTime.Days7)
    }

    fun delete() {
        tryRequest {
            val link = state.value.copy(access = 0)
            roomProvider.updateSharedLink(shareData.itemId, link, shareData.isFolder)
            _effect.tryEmit(SharedLinkSettingsEffect.Delete)
        }
    }

    fun setAccess(access: Int) {
        tryRequest {
            val link = state.value.copy(access = access)
            roomProvider.updateSharedLink(shareData.itemId, link, shareData.isFolder)
            _state.value = link
        }
    }

    fun setLifeTime(lifeTime: SharedLinkLifeTime) {
        tryRequest {
            var calendar: Calendar? = Calendar.getInstance()
            calendar?.timeZone = TimeZone.getTimeZone("gmt")

            when (lifeTime) {
                SharedLinkLifeTime.Unlimited -> calendar = null
                is SharedLinkLifeTime.Custom -> calendar?.time = lifeTime.date
                is SharedLinkLifeTimeWithAmount -> calendar?.add(lifeTime.field, lifeTime.amount)
            }

            val link = state.value.copy(
                sharedTo = state.value.sharedTo.copy(
                    expirationDate = calendar?.let { TimeUtils.DEFAULT_GMT_FORMAT.format(calendar.time) }
                )
            )

            _state.value = roomProvider.updateSharedLink(shareData.itemId, link, shareData.isFolder)
        }
    }

    private fun tryRequest(block: suspend () -> Unit) {
        viewModelScope.launch {
            try {
                _loading.value = true
                block.invoke()
            } catch (e: HttpException) {
                _effect.tryEmit(SharedLinkSettingsEffect.Error(e.code()))
            } catch (e: Exception) {
                _effect.tryEmit(SharedLinkSettingsEffect.Error())
            } finally {
                _loading.value = false
            }
        }
    }
}