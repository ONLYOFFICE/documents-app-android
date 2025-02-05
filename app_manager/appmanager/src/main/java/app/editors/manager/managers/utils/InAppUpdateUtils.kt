package app.editors.manager.managers.utils

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.fragment.app.FragmentActivity
import app.editors.manager.BuildConfig
import app.editors.manager.ui.fragments.onboarding.WhatsNewDialog
import com.google.android.gms.tasks.Task
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import java.util.concurrent.TimeUnit

object InAppUpdateUtils {

    private const val REQUEST_CODE_UPDATE = 100
    const val PREFS_NAME = "InAppUpdatePrefs"
    private const val LAST_VERSION_KEY = "last_version"
    private const val UPDATE_COMPLETED_KEY = "update_completed"
    private const val LAST_UPDATE_PROMPT_KEY = "last_update_prompt"

    fun checkForUpdate(activity: Activity) {
        if (!GoogleUtils.isGooglePlayServicesAvailable(activity)) {
            if (shouldShowWhatsNew(activity)) {
                WhatsNewDialog.show(activity as FragmentActivity)
            }
            return
        }

        val prefs: SharedPreferences = activity.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val lastPromptTime = prefs.getLong(LAST_UPDATE_PROMPT_KEY, 0)
        val currentTime = System.currentTimeMillis()

        if (currentTime - lastPromptTime < TimeUnit.DAYS.toMillis(1)) {
            return
        }

        val appUpdateManager: AppUpdateManager = AppUpdateManagerFactory.create(activity)
        val appUpdateInfoTask: Task<AppUpdateInfo> = appUpdateManager.appUpdateInfo

        appUpdateInfoTask.addOnSuccessListener { appUpdateInfo ->
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE &&
                appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)) {

                val appUpdateOptions = AppUpdateOptions.newBuilder(AppUpdateType.FLEXIBLE).build()
                appUpdateManager.startUpdateFlowForResult(appUpdateInfo, activity, appUpdateOptions, REQUEST_CODE_UPDATE)
                prefs.edit { putLong(LAST_UPDATE_PROMPT_KEY, currentTime) }
            }
        }

        appUpdateInfoTask.addOnFailureListener {
            if (shouldShowWhatsNew(activity)) {
                WhatsNewDialog.show(activity as FragmentActivity)
            }
        }

        appUpdateManager.registerListener { state ->
            if (state.installStatus() == InstallStatus.DOWNLOADED) {
                appUpdateManager.completeUpdate()
                setUpdateCompleted(activity, true)
            }
        }
    }

    private fun setUpdateCompleted(context: Context, completed: Boolean) {
        val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit {
            putBoolean(UPDATE_COMPLETED_KEY, completed)
        }
    }

    fun shouldShowWhatsNew(context: Context, currentVersion: String = BuildConfig.VERSION_NAME): Boolean {
        val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val lastVersion = prefs.getString(LAST_VERSION_KEY, null)
        val updateCompleted = prefs.getBoolean(UPDATE_COMPLETED_KEY, false)
        return if (lastVersion != currentVersion || updateCompleted) {
            prefs.edit { putString(LAST_VERSION_KEY, currentVersion) }
            setUpdateCompleted(context, false)
            true
        } else {
            false
        }
    }

    fun handleActivityResult(requestCode: Int, resultCode: Int, activity: Activity) {
        if (requestCode == REQUEST_CODE_UPDATE && resultCode != Activity.RESULT_OK) {
            checkForUpdate(activity)
        }
    }
}