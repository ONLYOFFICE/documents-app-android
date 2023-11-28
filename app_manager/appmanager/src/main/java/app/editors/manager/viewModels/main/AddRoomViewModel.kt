package app.editors.manager.viewModels.main

import android.annotation.SuppressLint
import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.runtime.Immutable
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import app.documents.core.network.manager.models.explorer.CloudFolder
import app.documents.core.network.manager.models.request.RequestBatchOperation
import app.documents.core.providers.RoomProvider
import app.editors.manager.R
import app.editors.manager.app.accountOnline
import app.editors.manager.app.api
import app.editors.manager.managers.utils.GlideUtils
import com.bumptech.glide.Glide
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import lib.compose.ui.views.ChipData
import lib.toolkit.base.managers.utils.AccountUtils
import lib.toolkit.base.managers.utils.mutableStateIn

@Suppress("UNCHECKED_CAST")
class AddRoomViewModelFactory(
    private val application: Application,
    private val roomProvider: RoomProvider,
    private val roomInfo: CloudFolder? = null,
    private val isCopy: Boolean = false
) :
    ViewModelProvider.AndroidViewModelFactory(application) {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return AddRoomViewModel(application, roomProvider, roomInfo, isCopy) as T
    }
}

data class AddRoomData(
    val type: Int,
    val name: String = "",
    val tags: MutableList<ChipData> = mutableListOf(),
    val roomImage: Bitmap? = null,
    val imageUri: Uri? = null
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

    private val _roomState: MutableStateFlow<AddRoomData> = flow {
        if (roomInfo != null) {
            emit(
                AddRoomData(
                    type = if (roomInfo.roomType == -1) 2 else roomInfo.roomType,
                    name = roomInfo.title,
                    tags = roomInfo.tags.map { ChipData(it ?: "") }.toMutableList(),
                    roomImage = loadImage(),
                    imageUri = roomInfo.logo?.medium?.let { Uri.parse(it) }
                )
            )
        } else {
            emit(AddRoomData(2))
        }
    }.mutableStateIn(viewModelScope, AddRoomData(2))
    val roomState: StateFlow<AddRoomData> = _roomState

    private val _viewState: MutableStateFlow<ViewState> = MutableStateFlow(ViewState.None)
    val viewState: StateFlow<ViewState> = _viewState


    fun setType(roomType: Int) {
        _roomState.value = _roomState.value.copy(type = roomType)
    }

    fun setImageUri(imageUri: Uri?) {
        viewModelScope.launch {
            _viewState.value = ViewState.Loading
            if (imageUri == null) {
                if (roomInfo != null) {
                    roomProvider.deleteLogo(roomInfo.id)
                }
                _viewState.value = ViewState.None
                _roomState.value = _roomState.value.copy(imageUri = null, roomImage = null)
            } else {
                withContext(Dispatchers.IO) {
                    val image = loadImage(imageUri)
                    _viewState.value = ViewState.None
                    _roomState.value = _roomState.value.copy(imageUri = imageUri, roomImage = image)
                }
            }
        }
    }

    @SuppressLint("CheckResult")
    private suspend fun loadImage(imageUri: Uri?, url: String? = null, isCrop: Boolean = true): Bitmap {
        return viewModelScope.async(Dispatchers.IO) {
            val request = Glide.with(context).asBitmap()
            request.apply {
                if (url != null) {
                    load(url)
                } else {
                    load(imageUri)
                }
                if (isCrop) {
                    request.circleCrop()
                }
            }
            return@async request.submit().get()

        }.await()
    }

    private suspend fun loadImage(): Bitmap {
        try {
            return viewModelScope.async(Dispatchers.IO) {
                val cloudAccount = context.accountOnline
                AccountUtils.getToken(
                    context,
                    context.accountOnline?.getAccountName() ?: ""
                )?.let {
                    val url = cloudAccount?.scheme + cloudAccount?.portal + roomInfo?.logo?.medium

                    return@async Glide.with(context)
                        .asBitmap()
                        .load(GlideUtils.getCorrectLoad(url, it))
                        .apply(GlideUtils.avatarOptions)
                        .submit()
                        .get()
                } ?: run {
                    return@async Glide.with(context).asBitmap().load(R.drawable.ic_empty_image)
                        .submit()
                        .get()
                }
            }.await()
        } catch (error: Throwable) {
            return checkNotNull(ContextCompat.getDrawable(context, R.drawable.ic_empty_image)).toBitmap()
        }
    }

    @SuppressLint("CheckResult")
    fun createRoom(roomType: Int, name: String, image: Uri?, tags: List<ChipData>) {
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
                        type = roomType,
                        logo = if (image != null && image.toString().isNotEmpty()) loadImage(image) else null,
                        tags = tags.map { it.text })

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
                    withContext(Dispatchers.Main) {
                        _viewState.value = ViewState.Error(error.message.toString())
                        delay(4000)
                        _viewState.value = ViewState.None
                    }
                }
            }
        }
    }

    private suspend fun checkCopy(id: String): Boolean {
        if (roomInfo == null) return false
        if (!isCopy) return false
        val operation = context.api.copy(RequestBatchOperation(destFolderId = id).apply {
            folderIds = listOf(roomInfo.id)
        }).blockingFirst().body()?.response?.get(0)
        while (true) {
            val op = context.api.status().blockingGet().response.find { it.id == operation?.id } ?: break
            if (op.progress == 100 || op.finished) break
            delay(100)
        }
        return false
    }

    fun saveData(name: String, tags: List<ChipData>) {
        _roomState.value = _roomState.value.copy(name = name, tags = tags.toMutableList())
    }

    fun createTag(tag: ChipData) {
        viewModelScope.launch {
            _viewState.value = ViewState.Loading
            withContext(Dispatchers.IO) {
                try {
                    roomProvider.createTag(tag.text)
                    _viewState.value = ViewState.Success(null, ChipViewState(tag, false))
                } catch (error: Throwable) {
                    _viewState.value = ViewState.Error(error.message.toString())
                    // Need delay to short snackbar
                    delay(4000)
                    _viewState.value = ViewState.None
                }
            }
        }
    }

    fun deleteTag(tag: ChipData) {
        viewModelScope.launch {
            _viewState.value = ViewState.Loading
            withContext(Dispatchers.IO) {
                try {
                    roomProvider.deleteTag(tag = tag.text)
                    _viewState.value = ViewState.Success(null, ChipViewState(tag, true))
                } catch (error: Throwable) {
                    _viewState.value = ViewState.Error(error.message.toString())
                    // Need delay to short snackbar
                    delay(4000)
                    _viewState.value = ViewState.None
                }
            }
        }
    }

    fun edit(name: String, tags: List<ChipData>) {
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
                    val isSuccess = roomProvider.renameRoom(roomInfo?.id ?: "", name)

                    withContext(Dispatchers.Main) {
                        if (isSuccess) {
                            _viewState.value = ViewState.Success(roomInfo?.id ?: "")
                        } else {
                            _viewState.value = ViewState.Error(context.getString(R.string.rooms_error_edit))
                        }
                    }
                } catch (error: Throwable) {
                    withContext(Dispatchers.Main) {
                        _viewState.value = ViewState.Error(error.message.toString())
                        delay(4000)
                        _viewState.value = ViewState.None
                    }
                }
            }
        }
    }
}