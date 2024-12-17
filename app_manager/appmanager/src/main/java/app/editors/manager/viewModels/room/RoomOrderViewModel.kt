package app.editors.manager.viewModels.room

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import app.documents.core.providers.RoomProvider

class RoomOrderViewModel(folderId: String, roomProvider: RoomProvider) : ViewModel() {

    @Suppress("UNCHECKED_CAST")
    class Factory(val folderId: String, val roomProvider: RoomProvider) : ViewModelProvider.Factory {

        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return RoomOrderViewModel(folderId, roomProvider) as T
        }
    }

    fun reorder() {

    }

    fun apply() {

    }
}