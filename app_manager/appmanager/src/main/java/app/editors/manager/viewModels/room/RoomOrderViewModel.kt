package app.editors.manager.viewModels.room

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import app.documents.core.model.cloud.Order
import app.documents.core.model.cloud.Order.Companion.ENTRY_TYPE_FILE
import app.documents.core.model.cloud.Order.Companion.ENTRY_TYPE_FOLDER
import app.documents.core.network.common.NetworkResult
import app.documents.core.network.manager.models.explorer.CloudFile
import app.documents.core.network.manager.models.explorer.Item
import app.documents.core.providers.RoomProvider
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RoomOrderHelper @Inject constructor() {

    private val orderToItemMap: MutableMap<Int, Item> = mutableMapOf()

    val hasChanges: Boolean
        get() = orderToItemMap.isNotEmpty()

    fun setItems(items: List<Item>) {
        items.forEach { item -> orderToItemMap[item.index] = item }
    }

    fun getOrderSet(): List<Order> {
        return orderToItemMap.map { (order, item) ->
            Order(
                order = order.toString(),
                entryId = item.id,
                entryType = if (item is CloudFile) ENTRY_TYPE_FILE else ENTRY_TYPE_FOLDER
            )
        }
    }

    fun clear() {
        orderToItemMap.clear()
    }
}

sealed class RoomOrderEffect {

    data object Success : RoomOrderEffect()
    data object Refresh : RoomOrderEffect()
    data object Error : RoomOrderEffect()
}

class RoomOrderViewModel(
    private val roomProvider: RoomProvider,
    private val roomOrderHelper: RoomOrderHelper
) : ViewModel() {

    private val _loading: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    private val _effect: MutableSharedFlow<RoomOrderEffect> = MutableSharedFlow(1)
    val effect: SharedFlow<RoomOrderEffect> = _effect.asSharedFlow()

    @Suppress("UNCHECKED_CAST")
    class Factory(
        private val roomProvider: RoomProvider,
        private val roomOrderHelper: RoomOrderHelper
    ) : ViewModelProvider.Factory {

        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return RoomOrderViewModel(roomProvider, roomOrderHelper) as T
        }
    }

    override fun onCleared() {
        roomOrderHelper.clear()
    }

    fun reorder(roomId: String) {
        _loading.value = true
        viewModelScope.launch {
            roomProvider.reorder(roomId)
                .collect { result ->
                    _loading.value = false
                    when (result) {
                        is NetworkResult.Error -> _effect.emit(RoomOrderEffect.Error)
                        is NetworkResult.Success<*> -> _effect.emit(RoomOrderEffect.Refresh)
                        is NetworkResult.Loading -> Unit
                    }
                }
        }
    }

    fun apply() {
        _loading.value = true
        viewModelScope.launch {
            roomProvider.order(roomOrderHelper.getOrderSet().toList())
                .collect { result ->
                    _loading.value = false
                    when (result) {
                        is NetworkResult.Error -> _effect.emit(RoomOrderEffect.Error)
                        is NetworkResult.Success<*> -> _effect.emit(RoomOrderEffect.Success)
                        is NetworkResult.Loading -> Unit
                    }
                }
        }
    }
}