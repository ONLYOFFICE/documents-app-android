package lib.toolkit.base.managers.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri

class ExportReceiver : BroadcastReceiver() {

    companion object {

        const val ACTION_EXPORT = "lib.toolkit.base.managers.receivers.ACTION_EXPORT"

        const val EXTRA_FILE_URI = "EXTRA_FILE_URI"

        @JvmStatic
        fun getFilters() = IntentFilter(ACTION_EXPORT)
    }

    @FunctionalInterface
    interface OnExportFile{
        fun onExportFile(uri: Uri)
    }

    var onExportReceiver: OnExportFile? = null

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == ACTION_EXPORT) {
            intent.extras?.getParcelable<Uri>(EXTRA_FILE_URI)?.let { uri ->
                onExportReceiver?.onExportFile(uri)
            }
        }
    }

}