package app.editors.manager.viewModels.main

import android.annotation.SuppressLint
import android.app.Application
import android.graphics.Bitmap
import android.net.Uri
import android.util.Size
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import app.documents.core.model.login.User
import app.documents.core.network.common.Result
import app.documents.core.network.common.contracts.ApiContract
import app.documents.core.network.manager.models.explorer.CloudFolder
import app.documents.core.network.manager.models.explorer.Item
import app.documents.core.network.manager.models.explorer.Lifetime
import app.documents.core.network.manager.models.explorer.PathPart
import app.documents.core.network.manager.models.explorer.Watermark
import app.documents.core.providers.RoomProvider
import app.editors.manager.R
import app.editors.manager.app.accountOnline
import app.editors.manager.mvp.models.ui.StorageQuota
import com.bumptech.glide.Glide
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import lib.compose.ui.views.ChipData
import java.io.Serializable

data class CopyItems(
    val folderIds: List<String> = emptyList(),
    val fileIds: List<String> = emptyList()
) : Serializable

data class StorageState(
    val id: String,
    val providerKey: String,
    val providerId: Int? = null,
    val location: String?,
    val createAsNewFolder: Boolean = false,
)

data class AddRoomData(
    val type: Int,
    val name: String = "",
    val owner: User = User(),
    val tags: MutableList<ChipData> = mutableListOf(),
    val imageUri: Any? = null,
    val storageState: StorageState? = null,
    val lifetime: Lifetime? = null,
    val denyDownload: Boolean = false,
    val watermark: Watermark? = null,
    val indexing: Boolean = false,
    val storageQuota: StorageQuota? = null
) {

    val canApplyChanges: Boolean
        get() = lifetime?.value?.isNotEmpty() ?: name.isNotEmpty()
}

sealed class ViewState {
    data object None : ViewState()
    data object Loading : ViewState()
    class Success(val id: String? = null) : ViewState()
    class Error(val message: String) : ViewState()
}

sealed class AddRoomEffect {

    data class Error(val message: String) : AddRoomEffect()
}

class AddRoomViewModel(
    private val context: Application,
    private val roomProvider: RoomProvider,
    private val roomInfo: Item? = null,
    private val roomType: Int? = null,
    private val copyItems: CopyItems? = null,
) : AndroidViewModel(application = context) {

    private val _roomState: MutableStateFlow<AddRoomData> = MutableStateFlow(
        if (roomInfo != null && roomInfo is CloudFolder) {
            AddRoomData(
                type = if (roomInfo.roomType == -1) 2 else roomInfo.roomType,
                name = roomInfo.title,
                owner = roomInfo.createdBy.run { User(id = id, displayName = displayName) },
                tags = roomInfo.tags.map { ChipData(it) }.toMutableList(),
                imageUri = if (roomInfo.logo?.medium?.isNotEmpty() == true) {
                    ApiContract.SCHEME_HTTPS + context.accountOnline?.portalUrl + roomInfo.logo!!.medium
                } else {
                    null
                },
                lifetime = roomInfo.lifetime,
                denyDownload = roomInfo.denyDownload,
                watermark = roomInfo.watermark,
                indexing = roomInfo.indexing,
                storageState = StorageState(
                    id = roomInfo.id,
                    providerKey = roomInfo.providerKey,
                    providerId = roomInfo.providerId,
                    location = null
                ).takeIf { roomInfo.providerItem }
            )
        } else if (roomType != null) {
            AddRoomData(roomType)
        } else {
            AddRoomData(2)
        }
    )
    val roomState: StateFlow<AddRoomData> = _roomState

    private val _viewState: MutableStateFlow<ViewState> = MutableStateFlow(ViewState.None)
    val viewState: StateFlow<ViewState> = _viewState

    private val _effect: MutableSharedFlow<AddRoomEffect> = MutableSharedFlow(1)
    val effect: SharedFlow<AddRoomEffect> = _effect.asSharedFlow()

    private val roomTags: Set<String> = (roomInfo as? CloudFolder)?.tags?.toSet().orEmpty()
    private var isDeleteLogo: Boolean = false

    init {
        if (roomInfo is CloudFolder && roomInfo.quotaLimit != null) {
            _roomState.update {
                it.copy(
                    storageQuota = StorageQuota.fromBytes(roomInfo.quotaLimit ?: 0)
                )
            }
        } else {
            viewModelScope.launch {
                roomProvider.getRoomsQuota().collect { result ->
                    if (result is Result.Success) {
                        if (result.result.enabled) {
                            _roomState.update {
                                it.copy(
                                    storageQuota = StorageQuota.fromBytes(result.result.value)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    fun setImageUri(imageUri: Uri?) {
        viewModelScope.launch {
            _viewState.value = ViewState.Loading
            if (imageUri == null) {
                isDeleteLogo = true
                _viewState.value = ViewState.None
                _roomState.value = _roomState.value.copy(imageUri = null)
            } else {
                isDeleteLogo = false
                _viewState.value = ViewState.None
                _roomState.value = _roomState.value.copy(imageUri = imageUri)

            }
        }
    }

    @SuppressLint("CheckResult")
    private suspend fun loadImage(imageUri: Any?, isCrop: Boolean = true): Bitmap {
        return viewModelScope.async(Dispatchers.IO) {
            val request = Glide.with(context).asBitmap()
            request.load(imageUri)
            request.override(200)
            request.encodeFormat(Bitmap.CompressFormat.JPEG)
            if (isCrop) request.circleCrop()
            return@async request.submit().get()
        }.await()
    }

    fun createRoom(roomType: Int, name: String, image: Any?, tags: List<String>) {
        val lifetime = _roomState.value.lifetime
        if (lifetime != null && lifetime.value.isEmpty()) {
            error(context.getString(R.string.rooms_error_logo_size_exceed))
        }

        viewModelScope.launch {
            if (name.isEmpty()) {
                _viewState.value = ViewState.Error(context.getString(R.string.rooms_error_name))
                delay(4000)
                _viewState.value = ViewState.None
                return@launch
            }
            _viewState.value = ViewState.Loading
            withContext(Dispatchers.IO) {
                try {
                    var imageUrl: String? = null
                    var imageSize: Size? = null

                    if (image != null) {
                        try {
                            val bitmap = loadImage(image, false)
                            val response = roomProvider.uploadLogo(bitmap)
                            imageUrl = response.data
                            imageSize = Size(bitmap.width, bitmap.height)
                        } catch (_: Exception) {
                        }
                    }

                    if (image != null && (imageUrl == null || imageSize == null)) {
                        error(context.getString(R.string.rooms_error_logo_size_exceed))
                    }

                    val id = with(roomState.value.storageState) {
                        if (this != null) {
                            roomProvider.createThirdPartyRoom(
                                folderId = id,
                                title = name,
                                asNewFolder = createAsNewFolder
                            )
                        } else {
                            roomProvider.createRoom(
                                title = name,
                                type = roomType,
                                quota = _roomState.value.storageQuota?.bytes
                            )
                        }
                    }

                    roomProvider.addTags(id, tags)

                    if (isDeleteLogo) {
                        roomProvider.deleteLogo(id)
                    } else if (id.isNotEmpty() && imageUrl != null && imageSize != null) {
                        roomProvider.setLogo(id, imageSize, imageUrl)
                    }

                    copyItems(id)

                    withContext(Dispatchers.Main) {
                        if (id.isNotEmpty()) {
                            _viewState.value = ViewState.Success(id)
                        } else {
                            _viewState.value =
                                ViewState.Error(context.getString(R.string.rooms_error_create))
                        }
                    }
                } catch (error: Throwable) {
                    _viewState.value = ViewState.Error(error.message.toString())
                    delay(4000)
                    _viewState.value = ViewState.None
                }
            }
        }
    }

    fun edit(name: String, tags: List<String>) {
        viewModelScope.launch {
            if (name.isEmpty()) {
                _viewState.value = ViewState.Error(context.getString(R.string.rooms_error_name))
                delay(4000)
                _viewState.value = ViewState.None
                return@launch
            }
            _viewState.value = ViewState.Loading
            withContext(Dispatchers.IO) {
                try {
                    val id = roomInfo?.id ?: ""
                    val imageUri = roomState.value.imageUri

                    var imageUrl: String? = null
                    var imageSize: Size? = null

                    if (imageUri != null) {
                        try {
                            val bitmap = loadImage(imageUri, false)
                            val response = roomProvider.uploadLogo(bitmap)
                            imageUrl = response.data
                            imageSize = Size(bitmap.width, bitmap.height)
                        } catch (_: Exception) {
                        }
                    }

                    if (imageUri != null && (imageUrl == null || imageSize == null)) {
                        error(context.getString(R.string.rooms_error_logo_size_exceed))
                    }

                    val isSuccess = roomProvider.editRoom(
                        id = id,
                        newTitle = name,
                        quota = _roomState.value.storageQuota?.bytes
                    )

                    roomProvider.deleteTags(id, (roomTags - tags.toSet()).toList())
                    roomProvider.addTags(id, tags - roomTags)

                    if (isDeleteLogo) {
                        roomProvider.deleteLogo(id)
                    } else if (id.isNotEmpty() && imageUrl != null && imageSize != null) {
                        roomProvider.setLogo(id, imageSize, imageUrl)
                    }

                    if (isSuccess) {
                        _viewState.value = ViewState.Success(roomInfo?.id ?: "")
                    } else {
                        _viewState.value =
                            ViewState.Error(context.getString(R.string.rooms_error_edit))
                    }
                } catch (error: Throwable) {
                    _viewState.value = ViewState.Error(error.message.toString())
                    delay(4000)
                    _viewState.value = ViewState.None
                }
            }
        }
    }

    private suspend fun copyItems(roomId: String) {
        copyItems?.let { items -> roomProvider.copyItems(roomId, items.folderIds, items.fileIds) }
    }

    fun saveData(name: String, tags: List<ChipData>) {
        _roomState.value = _roomState.value.copy(name = name, tags = tags.toMutableList())
    }

    fun connectStorage(folder: CloudFolder) {
        if (folder.providerKey.isEmpty()) {
            _viewState.value = ViewState.Error(context.getString(R.string.errors_unknown_error))
            return
        }

        _roomState.update {
            it.copy(
                storageState = StorageState(
                    id = folder.id,
                    providerKey = folder.providerKey,
                    location = null,
                    createAsNewFolder = true
                )
            )
        }
    }

    fun disconnectStorage() {
        _roomState.update {
            it.copy(storageState = null)
        }
    }

    fun setCreateNewFolder(value: Boolean) {
        _roomState.update {
            it.copy(
                storageState = it.storageState?.copy(createAsNewFolder = value)
            )
        }
    }

    fun setStorageLocation(pathParts: List<PathPart>) {
        _roomState.update {
            it.copy(
                storageState = it.storageState?.copy(
                    id = pathParts.last().id,
                    location = if (pathParts.size > 1) {
                        pathParts.toMutableList()
                            .also { it[0] = PathPart("", "") }
                            .joinToString("") { "${it.title}/" }
                    } else null
                )
            )
        }
    }

    fun setOwner(user: User) {
        _roomState.update { it.copy(owner = user) }
    }

    fun setRoomType(newType: Int) {
        _roomState.update { it.copy(type = newType) }
    }

    fun updateState(addRoomData: AddRoomData) {
        _roomState.value = addRoomData
    }
}