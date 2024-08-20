package app.editors.manager.managers.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter


class RoomDuplicateReceiver : BroadcastReceiver() {

    interface Listener {
        fun onHideDuplicateNotification(workerId: String?)
        fun onDuplicateComplete()
    }

    companion object {
        const val ACTION_HIDE = "app.editors.manager.managers.ACTION_HIDE"
        const val ACTION_COMPLETE = "app.editors.manager.managers.ACTION_COMPLETE"

        const val KEY_NOTIFICATION_HIDE = "KEY_NOTIFICATION_HIDE"

        @JvmStatic
        fun getFilters() = IntentFilter().apply {
            addAction(ACTION_HIDE)
            addAction(ACTION_COMPLETE)
        }
    }

    private var listener: Listener? = null

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            ACTION_COMPLETE -> listener?.onDuplicateComplete()
            ACTION_HIDE -> listener?.onHideDuplicateNotification(intent.getStringExtra(KEY_NOTIFICATION_HIDE))
        }
    }

    fun setListener(listener: Listener?) {
        this.listener = listener
    }
}