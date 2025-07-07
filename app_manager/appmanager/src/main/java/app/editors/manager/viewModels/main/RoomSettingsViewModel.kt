package app.editors.manager.viewModels.main

import android.content.ContentResolver
import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.runtime.Immutable
import app.documents.core.model.login.User
import app.documents.core.network.common.contracts.ApiContract
import app.documents.core.network.manager.models.explorer.Lifetime
import app.documents.core.network.manager.models.explorer.Watermark
import app.documents.core.network.manager.models.explorer.WatermarkType
import app.documents.core.providers.RoomProvider
import app.editors.manager.R
import app.editors.manager.mvp.models.ui.StorageQuota
import app.editors.manager.viewModels.base.BaseLogoViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import lib.compose.ui.views.ChipList

data class RoomSettingsStorage(
    val id: String,
    val providerKey: String,
    val providerId: Int? = null,
    val location: String?,
    val createAsNewFolder: Boolean = false,
)

@Immutable
data class RoomSettingsWatermarkState(
    val watermark: Watermark = Watermark(),
    val imageUri: Uri? = null,
    val imagePreview: Bitmap? = null,
)

@Immutable
data class RoomSettingsState(
    val roomId: String? = null,
    val type: Int = -1,
    val name: String = "",
    val filesCount: Int = 0,
    val indexing: Boolean = false,
    val denyDownload: Boolean = false,
    val tags: ChipList = ChipList(),
    val owner: User = User(),
    val lifetime: Lifetime = Lifetime(enabled = false),
    val quota: StorageQuota = StorageQuota(),
    val storageState: RoomSettingsStorage? = null
)

sealed class RoomSettingsEffect {

    data class Success(val id: String? = null) : RoomSettingsEffect()
    data class Error(val message: Int) : RoomSettingsEffect()
}

abstract class RoomSettingsViewModel(
    contentResolver: ContentResolver,
    protected val roomProvider: RoomProvider,
) : BaseLogoViewModel(contentResolver, roomProvider) {

    private val _state: MutableStateFlow<RoomSettingsState> = MutableStateFlow(RoomSettingsState())
    val state: StateFlow<RoomSettingsState> = _state.asStateFlow()

    private val _watermarkState: MutableStateFlow<RoomSettingsWatermarkState> =
        MutableStateFlow(RoomSettingsWatermarkState())
    val watermarkState: StateFlow<RoomSettingsWatermarkState> = _watermarkState.asStateFlow()

    private val _loading: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    private val _effect: MutableSharedFlow<RoomSettingsEffect> = MutableSharedFlow(1)
    val effect: SharedFlow<RoomSettingsEffect> = _effect.asSharedFlow()

    val canApplyChangesFlow: Flow<Boolean> = combine(
        watermarkState,
        state,
        loading,
        transform = { watermarkState, state, loading ->
            if (loading) {
                return@combine false
            }

            if (state.name.isEmpty()) {
                return@combine false
            }

            if (state.lifetime.enabled && state.lifetime.value == 0) {
                return@combine false
            }

            if (state.type == ApiContract.RoomType.VIRTUAL_ROOM && watermarkState.watermark.enabled) {
                with(watermarkState) {
                    when (watermark.type) {
                        WatermarkType.Image -> {
                            if (imageUri == null && watermark.imageUrl == null) {
                                return@combine false
                            }
                        }

                        WatermarkType.ViewerInfo -> {
                            if (watermark.additions == 0) {
                                return@combine false
                            }
                        }
                    }
                }
            }

            return@combine true
        }
    )

    protected var initialTags: List<String> = emptyList()

    protected fun updateState(block: (RoomSettingsState) -> RoomSettingsState) {
        _state.update(block)
    }

    protected fun emitEffect(effect: RoomSettingsEffect) {
        _effect.tryEmit(effect)
    }

    protected fun setLoading(enabled: Boolean) {
        _loading.value = enabled
    }

    // set room logo if uri is not null, delete logo if web url is null
    protected suspend fun setOrDeleteRoomLogo(roomId: String) {
        try {
            getUploadedRoomLogo { throw it }?.let { logo ->
                roomProvider.setLogo(
                    roomId = roomId,
                    size = logo.size,
                    url = logo.tmpFile
                )
            } ?: run {
                if (logoState.value.logoWebUrl == null) {
                    roomProvider.deleteLogo(roomId)
                }
            }
        } catch (_: Exception) {
            emitEffect(RoomSettingsEffect.Error(R.string.rooms_error_logo_size_exceed))
        }
    }

    // upload watermark image if uri is not null and update state
    protected suspend fun setWatermarkImage() {
        try {
            with(watermarkState.value) {
                if (imageUri != null && imagePreview != null) {
                    _watermarkState.update {
                        it.copy(
                            watermark = it.watermark.copy(
                                imageUrl = roomProvider.uploadImage(imagePreview),
                                imageHeight = imagePreview.height,
                                imageWidth = imagePreview.width
                            )
                        )
                    }
                }
            }
        } catch (_: Exception) {
            emitEffect(RoomSettingsEffect.Error(R.string.rooms_error_logo_size_exceed))
        }
    }

    protected suspend fun saveTags(roomId: String) {
        roomProvider.addTags(roomId, state.value.tags.list - initialTags.toSet())
        roomProvider.deleteTags(roomId, initialTags - state.value.tags.list.toSet())
    }

    abstract fun applyChanges()

    fun setWatermarkImageUri(uri: Uri?) {
        _watermarkState.update {
            if (uri != null) {
                it.copy(
                    watermark = it.watermark.copy(rotate = 0),
                    imageUri = uri,
                    imagePreview = getBitmapFromUri(uri)
                )
            } else {
                it.copy(
                    watermark = it.watermark.copy(imageUrl = null),
                    imageUri = null,
                    imagePreview = null
                )
            }
        }
    }

    fun setName(name: String) {
        _state.update { it.copy(name = name) }
    }

    fun addTag(tag: String) {
        _state.update { it.copy(tags = ChipList(it.tags.list + tag)) }
    }

    fun removeTag(tag: String) {
        _state.update { it.copy(tags = ChipList(it.tags.list - tag)) }
    }

    fun setIndexing(enabled: Boolean) {
        _state.update { it.copy(indexing = enabled) }
    }

    fun setRestrict(enabled: Boolean) {
        _state.update { it.copy(denyDownload = enabled) }
    }

    fun updateLifeTimeState(block: (Lifetime) -> Lifetime) {
        _state.update { it.copy(lifetime = block(it.lifetime)) }
    }

    fun updateStorageState(block: (RoomSettingsStorage?) -> RoomSettingsStorage?) {
        _state.update {
            it.copy(
                storageState = block.invoke(
                    it.storageState ?: RoomSettingsStorage("", "", null, null)
                )
            )
        }
    }

    fun updateStorageQuota(block: (StorageQuota) -> StorageQuota) {
        _state.update { it.copy(quota = block(it.quota)) }
    }

    fun updateWatermarkState(block: (RoomSettingsWatermarkState) -> RoomSettingsWatermarkState) {
        _watermarkState.update(block)
    }

}