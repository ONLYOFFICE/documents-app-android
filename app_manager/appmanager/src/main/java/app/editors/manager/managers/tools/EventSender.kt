package app.editors.manager.managers.tools

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import lib.toolkit.base.managers.tools.ResourcesProvider

interface BaseEvent {
    data class ShowMessage(val msg: String) : BaseEvent
}

interface EventSender {
    val events: Flow<BaseEvent>
    suspend fun sendMessage(msgId: Int)
    suspend fun sendEvent(event: BaseEvent)
}

class BaseEventSender(
    private val resourceProvider: ResourcesProvider
) : EventSender {

    private val _events = Channel<BaseEvent>(Channel.BUFFERED)
    override val events: Flow<BaseEvent> = _events.receiveAsFlow()

    override suspend fun sendMessage(msgId: Int) {
        _events.send(BaseEvent.ShowMessage(resourceProvider.getString(msgId)))
    }

    override suspend fun sendEvent(event: BaseEvent) {
        _events.send(event)
    }
}