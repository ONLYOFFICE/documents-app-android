package app.editors.manager.managers.utils

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import com.google.android.gms.tasks.Task
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

object InAppUpdateUtils {

    private const val REQUEST_CODE_UPDATE = 100
    private const val PREFS_NAME = "InAppUpdatePrefs"
    private const val LAST_CHECK_DATE_KEY = "lastCheckDate"

    private val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMdd")
        .withZone(ZoneId.systemDefault())

    private fun getCurrentDate(): String {
        return dateFormatter.format(Instant.now())
    }

    private fun shouldCheckForUpdate(context: Context): Boolean {
        val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val lastCheckDate = prefs.getString(LAST_CHECK_DATE_KEY, null)
        val currentDate = getCurrentDate()
        return lastCheckDate == null || lastCheckDate != currentDate
    }

    private fun updateLastCheckDate(context: Context) {
        val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val editor = prefs.edit()
        editor.putString(LAST_CHECK_DATE_KEY, getCurrentDate())
        editor.apply()
    }

    fun checkForUpdate(activity: Activity) {
        if (!shouldCheckForUpdate(activity)) return

        val appUpdateManager: AppUpdateManager = AppUpdateManagerFactory.create(activity)
        val appUpdateInfoTask: Task<AppUpdateInfo> = appUpdateManager.appUpdateInfo

        appUpdateInfoTask.addOnSuccessListener { appUpdateInfo ->
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE &&
                appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)) {

                val appUpdateOptions = AppUpdateOptions.newBuilder(AppUpdateType.FLEXIBLE).build()

                appUpdateManager.startUpdateFlow(appUpdateInfo, activity, appUpdateOptions)
                updateLastCheckDate(activity)
            }
        }

        appUpdateManager.registerListener { state ->
            if (state.installStatus() == InstallStatus.DOWNLOADED) {
                // Prompt the user to complete the update
                appUpdateManager.completeUpdate()
            }
        }
    }

    fun handleActivityResult(requestCode: Int, resultCode: Int, activity: Activity) {
        if (requestCode == REQUEST_CODE_UPDATE && resultCode != Activity.RESULT_OK) {
            checkForUpdate(activity)
        }
    }
}