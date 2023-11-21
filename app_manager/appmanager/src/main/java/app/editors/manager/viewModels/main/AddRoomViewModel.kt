package app.editors.manager.viewModels.main

import android.annotation.SuppressLint
import android.app.Application
import android.net.Uri
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import app.documents.core.providers.RoomProvider
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
    private val roomProvider: RoomProvider
) :
    ViewModelProvider.AndroidViewModelFactory(application) {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return AddRoomViewModel(application, roomProvider) as T
    }
}

data class AddRoomData(val type: Int, val name: String = "",  val tags: MutableList<ChipData> = mutableListOf(), val roomImage: ImageBitmap? = null, val imageUri: Uri? = null)

sealed class ViewState {
    object None : ViewState()
    object Success : ViewState()
    class Error(val message: String) : ViewState()
    class Loading : ViewState()

}

class AddRoomViewModel(private val context: Application, private val roomProvider: RoomProvider) :
    AndroidViewModel(application = context) {

    private val _roomState: MutableStateFlow<AddRoomData> = MutableStateFlow(AddRoomData(2))
    val roomState: StateFlow<AddRoomData> = _roomState

    private val _viewState: MutableStateFlow<ViewState> = MutableStateFlow(ViewState.None)
    val viewState: StateFlow<ViewState> = _viewState


    fun setType(roomType: Int) {
        _roomState.value = _roomState.value.copy(type = roomType)
    }

    fun setImageUri(imageUri: Uri?) {
        if (imageUri == null) {
            _roomState.value = _roomState.value.copy(imageUri = null, roomImage = null)
        } else {
            viewModelScope.launch {
                _viewState.value = ViewState.Loading()
                withContext(Dispatchers.IO) {
                    val image = loadImage(imageUri)
                    launch {
                        _viewState.value = ViewState.None
                        _roomState.value = _roomState.value.copy(imageUri = imageUri, roomImage = image)
                    }
                }
            }
        }

    }

    private suspend fun loadImage(imageUri: Uri): ImageBitmap {
        return viewModelScope.async(Dispatchers.IO) {
            Glide.with(context)
                .asBitmap()
                .load(imageUri)
                .circleCrop()
                .submit()
                .get()
                .asImageBitmap()
        }.await()
    }

    @SuppressLint("CheckResult")
    fun createRoom(roomType: Int, name: String, image: Uri?, tags: List<ChipData>) {
        viewModelScope.launch {
            if (name.isEmpty()) {
                _viewState.value = ViewState.Error("")
                delay(4000)
                _viewState.value = ViewState.None
                return@launch
            }
            _viewState.value = ViewState.Loading()
            withContext(Dispatchers.IO) {
                try {
                    roomProvider.createRoom(name, roomType).blockingFirst()
                    _viewState.value = ViewState.Success
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

    fun saveData(name: String, tags: List<ChipData>) {
        _roomState.value = _roomState.value.copy(name = name, tags = tags.toMutableList())
    }
}