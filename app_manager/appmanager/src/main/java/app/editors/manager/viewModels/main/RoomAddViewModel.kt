package app.editors.manager.viewModels.main

import android.content.ContentResolver
import androidx.lifecycle.viewModelScope
import app.documents.core.network.common.Result
import app.documents.core.network.manager.models.explorer.CloudFolder
import app.documents.core.network.manager.models.explorer.PathPart
import app.documents.core.providers.RoomProvider
import app.editors.manager.R
import app.editors.manager.mvp.models.ui.StorageQuota
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.Serializable

data class CopyItems(
    val folderIds: List<String> = emptyList(),
    val fileIds: List<String> = emptyList()
) : Serializable

class RoomAddViewModel(
    roomType: Int,
    roomProvider: RoomProvider,
    contentResolver: ContentResolver,
    private val copyItems: CopyItems? = null
) : RoomSettingsViewModel(roomProvider = roomProvider, contentResolver = contentResolver) {

    init {
        updateState {
            it.copy(type = roomType)
        }
        viewModelScope.launch {
            roomProvider.getRoomsQuota().collect { result ->
                if (result is Result.Success) {
                    if (result.result.enabled) {
                        updateState {
                            it.copy(
                                quota = StorageQuota.fromBytes(result.result.value)
                            )
                        }
                    }
                }
            }
        }
    }

    override fun applyChanges() {
        viewModelScope.launch(Dispatchers.IO) {
            setLoading(true)
            val roomId = createRoomAndGetId()
            if (roomId != null) {
                setOrDeleteRoomLogo(roomId)
                setWatermarkImage()
                copyItems(roomId)
                saveTags(roomId)
            } else {
                emitEffect(RoomSettingsEffect.Error(R.string.rooms_error_create))
            }
            setLoading(false)
        }
    }

    private suspend fun createRoomAndGetId(): String? {
        return runCatching {
            with(state.value) {
                if (storageState != null) {
                    roomProvider.createThirdPartyRoom(
                        folderId = storageState.id,
                        title = name,
                        asNewFolder = storageState.createAsNewFolder
                    )
                } else {
                    roomProvider.createRoom(
                        title = name,
                        type = type,
                        quota = quota.bytes,
                        lifetime = lifetime,
                        denyDownload = denyDownload,
                        indexing = indexing
                    )
                }
            }
        }.getOrNull()
    }

    private suspend fun copyItems(roomId: String) {
        copyItems?.let { items ->
            runCatching {
                roomProvider.copyItems(roomId, items.folderIds, items.fileIds)
            }.onFailure {
                emitEffect(RoomSettingsEffect.Error(R.string.room_duplicate_error))
            }
        }
    }

    fun setType(roomType: Int) {
        updateState { it.copy(type = roomType) }
    }

    fun connectStorage(folder: CloudFolder) {
        if (folder.providerKey.isEmpty()) {
            emitEffect(RoomSettingsEffect.Error(R.string.errors_unknown_error))
            return
        }

        updateStorageState {
            it.copy(
                id = folder.id,
                providerKey = folder.providerKey,
                location = null,
                createAsNewFolder = true
            )
        }
    }

    fun disconnectStorage() {
        updateState {
            it.copy(storageState = null)
        }
    }

    fun setCreateNewFolder(value: Boolean) {
        updateStorageState {
            it.copy(createAsNewFolder = value)
        }
    }

    fun setStorageLocation(pathParts: List<PathPart>) {
        updateStorageState {
            it.copy(
                id = pathParts.last().id,
                location = if (pathParts.size > 1) {
                    pathParts.toMutableList()
                        .also { it[0] = PathPart("", "") }
                        .joinToString("") { "${it.title}/" }
                } else null
            )
        }
    }
}