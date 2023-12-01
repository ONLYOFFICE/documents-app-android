package app.editors.manager.viewModels.main

import android.annotation.SuppressLint
import android.app.Application
import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.runtime.Immutable
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import app.documents.core.network.common.contracts.ApiContract
import app.documents.core.network.manager.models.explorer.CloudFolder
import app.documents.core.network.manager.models.request.RequestBatchOperation
import app.documents.core.providers.RoomProvider
import app.editors.manager.R
import app.editors.manager.app.api
import app.editors.manager.app.appComponent
import com.bumptech.glide.Glide
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import lib.compose.ui.views.ChipData


@Suppress("UNCHECKED_CAST")
class AddRoomViewModelFactory(
    private val application: Application,
    private val roomProvider: RoomProvider,
    private val roomInfo: CloudFolder? = null,
    private val isCopy: Boolean = false
) : ViewModelProvider.AndroidViewModelFactory(application) {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return AddRoomViewModel(application, roomProvider, roomInfo, isCopy) as T
    }
}

data class AddRoomData(
    val type: Int,
    val name: String = "",
    val tags: MutableList<ChipData> = mutableListOf(),
    val imageUri: Any? = null
)

sealed class ViewState {
    data object None : ViewState()
    data object Loading : ViewState()
    class Success(val id: String? = null, val tagState: ChipViewState? = null) : ViewState()
    class Error(val message: String) : ViewState()
}

@Immutable
data class ChipViewState(
    val tag: ChipData,
    val isDelete: Boolean
)

class AddRoomViewModel(
    private val context: Application,
    private val roomProvider: RoomProvider,
    private val roomInfo: CloudFolder? = null,
    private val isCopy: Boolean = false
) : AndroidViewModel(application = context) {

    private val _roomState: MutableStateFlow<AddRoomData> = MutableStateFlow(
        if (roomInfo != null) {
            AddRoomData(
                type = if (roomInfo.roomType == -1) 2 else roomInfo.roomType,
                name = roomInfo.title,
                tags = roomInfo.tags.map { ChipData(it) }.toMutableList(),
                imageUri = if (roomInfo.logo?.medium?.isNotEmpty() == true) {
                    ApiContract.SCHEME_HTTPS + context.appComponent.networkSettings.getPortal() + roomInfo.logo!!.medium
                } else {
                    null
                }
            )
        } else {
            AddRoomData(2)
        }
    )
    val roomState: StateFlow<AddRoomData> = _roomState

    private val _viewState: MutableStateFlow<ViewState> = MutableStateFlow(ViewState.None)
    val viewState: StateFlow<ViewState> = _viewState


    private val addTags: MutableList<String> = mutableListOf()
    private val deleteTags: MutableList<String> = mutableListOf()
    private var isDeleteLogo: Boolean = false

    fun setType(roomType: Int) {
        _roomState.value = _roomState.value.copy(type = roomType)
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
            request.apply {
                load(imageUri)
                if (isCrop) {
                    request.circleCrop()
                }
            }
            return@async request.submit().get()

        }.await()
    }

    fun createRoom(roomType: Int, name: String, image: Any?) {
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
                    val id = roomProvider.createRoom(
                        title = name,
                        type = roomType
                    )

                    if (addTags.isNotEmpty()) {
                        roomProvider.addTags(id, addTags)
                    }

                    if (id.isNotEmpty() && image != null) {
                        roomProvider.setLogo(id, loadImage(image, false))
                    }

                    if (isDeleteLogo) {
                        roomProvider.deleteLogo(id)
                    }

                    while (checkCopy(id)) {
                        delay(100)
                    }

                    withContext(Dispatchers.Main) {
                        if (id.isNotEmpty()) {
                            _viewState.value = ViewState.Success(id)
                        } else {
                            _viewState.value = ViewState.Error(context.getString(R.string.rooms_error_create))
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

    fun edit(name: String) {
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
                    val isSuccess = roomProvider.renameRoom(id, name)

                    if (addTags.isNotEmpty()) {
                        roomProvider.addTags(id, addTags)
                    }

                    if (deleteTags.isNotEmpty()) {
                        roomProvider.deleteTags(id, deleteTags)
                    }

                    when {
                        isDeleteLogo -> {
                            roomProvider.deleteLogo(id)
                        }
                        roomState.value.imageUri is Uri -> {
                            roomProvider.setLogo(id, loadImage(roomState.value.imageUri!!, false))

                        }
                    }

                    if (isSuccess) {
                        _viewState.value = ViewState.Success(roomInfo?.id ?: "")
                    } else {
                        _viewState.value = ViewState.Error(context.getString(R.string.rooms_error_edit))
                    }
                } catch (error: Throwable) {
                    _viewState.value = ViewState.Error(error.message.toString())
                    delay(4000)
                    _viewState.value = ViewState.None
                }
            }
        }
    }

    private suspend fun checkCopy(id: String): Boolean {
        if (roomInfo == null) return false
        if (!isCopy) return false
        //TODO check only the first operation???
        context.api.copyCoroutines(RequestBatchOperation(destFolderId = id).apply {
            folderIds = listOf(roomInfo.id)
        }).response.forEach { operation ->
            if (operation.finished) return false
            while (true) {
                val op = context.api.statusCoroutines().response.find { it.id == operation.id } ?: break
                if (op.progress == 100 || op.finished) break
                delay(100)
            }
        }

        return false
    }

    fun saveData(name: String, tags: List<ChipData>) {
        _roomState.value = _roomState.value.copy(name = name, tags = tags.toMutableList())
    }

    fun createTag(tag: ChipData) {
        if (!addTags.contains(tag.text)) {
            addTags.add(tag.text)
        }
        if (deleteTags.contains(tag.text)) {
            deleteTags.remove(tag.text)
        }
        _viewState.value = ViewState.Success(null, ChipViewState(tag, false))
    }

    fun deleteTag(tag: ChipData) {
        if (!deleteTags.contains(tag.text)) {
            deleteTags.add(tag.text)
        }
        if (addTags.contains(tag.text)) {
            addTags.remove(tag.text)
        }
        _viewState.value = ViewState.Success(null, ChipViewState(tag, true))
    }
}