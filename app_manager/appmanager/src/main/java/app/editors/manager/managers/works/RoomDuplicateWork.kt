package app.editors.manager.managers.works

import android.content.Context
import android.content.Intent
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import app.documents.core.network.common.NetworkResult.Error
import app.documents.core.network.common.NetworkResult.Success
import app.documents.core.network.common.NetworkResult.Loading
import app.editors.manager.app.roomProvider
import app.editors.manager.managers.receivers.RoomDuplicateReceiver
import app.editors.manager.managers.utils.NotificationUtils
import kotlinx.coroutines.cancel
import kotlinx.coroutines.runBlocking

class RoomDuplicateWork(context: Context, workerParams: WorkerParameters) :
    Worker(context, workerParams) {

    companion object {

        val TAG: String = RoomDuplicateWork::class.java.simpleName

        const val KEY_ROOM_ID = "key_room_id"
        const val KEY_ROOM_TITLE = "key_room_title"

        fun getTag(id: Int?, title: String?) = "${title}_$id"
    }

    private val notificationUtils: NotificationUtils = NotificationUtils(applicationContext, TAG)
    private var id: String? = null
    private var title: String? = null

    override fun doWork(): Result {
        title = inputData.getString(KEY_ROOM_TITLE)
        id = inputData.getString(KEY_ROOM_ID)
        runBlocking {
            if (id != null && title != null) {
                applicationContext.roomProvider.duplicate(id.orEmpty()).collect { result ->
                    if (isStopped) {
                        notificationUtils.removeNotification(id.hashCode())
                        cancel()
                        return@collect
                    }
                    when (result) {
                        is Error -> onError()
                        is Success -> {
                            if (result.data >= 100) onComplete() else onProgress(result.data)
                        }
                        is Loading -> Unit
                    }
                }
            }
        }
        return Result.success()
    }

    private fun onError() {
        notificationUtils.showRoomDuplicateErrorNotification(id.hashCode(), title)
    }

    private fun onProgress(progress: Int) {
        notificationUtils.showRoomDuplicateProgressNotification(
            id.hashCode(),
            getId().toString(),
            title.orEmpty(),
            progress
        )
    }

    private fun onComplete() {
        notificationUtils.showRoomDuplicateCompleteNotification(id.hashCode(), title.orEmpty())
        LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(
            Intent(applicationContext, RoomDuplicateReceiver::class.java).apply {
                action = RoomDuplicateReceiver.ACTION_COMPLETE
            }
        )
    }
}