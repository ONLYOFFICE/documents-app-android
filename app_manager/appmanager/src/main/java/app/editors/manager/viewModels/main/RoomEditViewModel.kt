package app.editors.manager.viewModels.main

import android.content.ContentResolver
import androidx.lifecycle.viewModelScope
import app.documents.core.model.login.User
import app.documents.core.network.common.Result
import app.documents.core.network.manager.models.explorer.Lifetime
import app.documents.core.network.manager.models.explorer.Watermark
import app.documents.core.network.manager.models.explorer.WatermarkType
import app.documents.core.providers.RoomProvider
import app.editors.manager.R
import app.editors.manager.mvp.models.ui.StorageQuota
import app.editors.manager.viewModels.base.RoomSettingsLogoState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import lib.compose.ui.views.ChipList

class RoomEditViewModel(
    private val roomId: String,
    roomProvider: RoomProvider,
    contentResolver: ContentResolver,
) : RoomSettingsViewModel(roomProvider = roomProvider, contentResolver = contentResolver) {

    private val _loadingRoom: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val loadingRoom: StateFlow<Boolean> = _loadingRoom.asStateFlow()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            _loadingRoom.value = true
            roomProvider.getRoomInfo(roomId).collect { result ->
                when (result) {
                    is Result.Error -> emitEffect(RoomSettingsEffect.Error(R.string.errors_unknown_error))
                    is Result.Success -> {
                        val roomInfo = result.result

                        initialTags = result.result.tags.toList()

                        updateState {
                            RoomSettingsState(
                                type = if (roomInfo.roomType == -1) 2 else roomInfo.roomType,
                                name = roomInfo.title,
                                owner = roomInfo.createdBy.run {
                                    User(
                                        id = id,
                                        displayName = displayName
                                    )
                                },
                                tags = ChipList(roomInfo.tags.toList()),
                                lifetime = roomInfo.lifetime?.copy(enabled = true) ?: Lifetime(enabled = false),
                                denyDownload = roomInfo.denyDownload,
                                indexing = roomInfo.indexing,
                                storageState = RoomSettingsStorage(
                                    id = roomInfo.id,
                                    providerKey = roomInfo.providerKey,
                                    providerId = roomInfo.providerId,
                                    location = null
                                ).takeIf { roomInfo.providerItem },
                                quota = StorageQuota.fromBytes(roomInfo.quotaLimit),
                                filesCount = roomInfo.filesCount
                            )
                        }
                        updateLogoState {
                            RoomSettingsLogoState(
                                logoWebUrl = roomInfo.logo?.medium
                            )
                        }
                        updateWatermarkState {
                            RoomSettingsWatermarkState(
                                watermark = roomInfo.watermark?.copy(
                                    type = if (roomInfo.watermark?.imageUrl != null) {
                                        WatermarkType.Image
                                    } else {
                                        WatermarkType.ViewerInfo
                                    }
                                ) ?: Watermark(enabled = false)
                            )
                        }
                    }
                }
            }
            _loadingRoom.value = false
        }
        viewModelScope.launch(Dispatchers.IO) {
            roomProvider.getRoomsQuota().collect { result ->
                if (result is Result.Success) {
                    updateState {
                        it.copy(
                            quota = it.quota.copy(visible = result.result.enabled)
                        )
                    }
                }
            }
        }
    }

    override fun applyChanges() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                setLoading(true)
                val uploadLogoAsync = async { setOrDeleteRoomLogo(roomId) }
                val uploadWatermarkAsync = async { setWatermarkImage() }
                awaitAll(uploadLogoAsync, uploadWatermarkAsync)
                saveTags(roomId)
                roomProvider.editRoom(
                    id = roomId,
                    newTitle = state.value.name,
                    quota = state.value.quota.takeIf(StorageQuota::enabled)?.bytes ?: -1,
                    lifetime = state.value.lifetime,
                    denyDownload = state.value.denyDownload,
                    indexing = state.value.indexing,
                    watermark = watermarkState.value.watermark
                )
                emitEffect(RoomSettingsEffect.Success())
                setLoading(false)
            } catch (e: Exception) {
                emitEffect(RoomSettingsEffect.Error(R.string.rooms_error_edit))
            }
        }
    }

    fun setOwner(user: User) {
        updateState { it.copy(owner = user) }
    }
}