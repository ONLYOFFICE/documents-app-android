package app.editors.manager.managers.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import app.editors.manager.app.appComponent
import app.editors.manager.ui.activities.main.AppLocaleConfirmationActivity

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
object AppLocaleReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action == Intent.ACTION_LOCALE_CHANGED) {
            val appLocaleHelper = context.appComponent.appLocaleHelper
            // App locale has changed by system if intent extras is null
            if (intent.extras == null && appLocaleHelper.checkAppLocale()) {
                AppLocaleConfirmationActivity.show(context)
            }
        }
    }
}