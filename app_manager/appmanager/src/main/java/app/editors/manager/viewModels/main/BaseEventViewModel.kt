package app.editors.manager.viewModels.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import lib.toolkit.base.managers.tools.ResourcesProvider

interface BaseEvent {
    data class ShowMessage(val msg: String): BaseEvent
}

abstract class BaseEventViewModel(
    private val resourceProvider: ResourcesProvider
) : ViewModel() {
    protected val _events = Channel<BaseEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    protected fun sendMessage(msgId: Int) {
        viewModelScope.launch {
            _events.send(BaseEvent.ShowMessage(resourceProvider.getString(msgId)))
        }
    }
}