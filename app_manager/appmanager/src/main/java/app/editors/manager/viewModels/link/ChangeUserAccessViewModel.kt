package app.editors.manager.viewModels.link

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.documents.core.network.share.models.GroupShare
import app.documents.core.providers.RoomProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ChangeUserAccessViewModel(
    private val roomProvider: RoomProvider,
    private val roomId: String,
    private val groupId: String
) : ViewModel() {

    private val _user: MutableStateFlow<List<GroupShare>?> = MutableStateFlow(emptyList())
    val users: StateFlow<List<GroupShare>?> = _user.asStateFlow()

    init {
        fetchUsers()
    }

    fun fetchUsers() {
        viewModelScope.launch(Dispatchers.IO) {
            _user.value = runCatching { roomProvider.getGroupUsers(roomId, groupId) }.getOrNull()
        }
    }
}