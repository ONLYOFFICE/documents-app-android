package app.editors.manager.managers.utils

import android.os.Bundle
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import app.editors.manager.BuildConfig
import app.editors.manager.R
import app.editors.manager.app.App.Companion.getApp
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings

object FirebaseUtils {

    private const val KEY_RATING = "android_documents_rating"
    private const val KEY_CAPTCHA = "recaptcha_for_portal_registration"
    private const val KEY_TERMS_OF_SERVICE = "link_terms_of_service"
    private const val KEY_PRIVACY_POLICY = "link_privacy_policy"
    private const val TIME_FETCH: Long = 3600

    private var firebaseAnalytics: FirebaseAnalytics? = null

    @JvmStatic
    fun addCrash(message: String) {
        if (getApp().isAnalyticEnable) {
            FirebaseCrashlytics.getInstance().log(message)
        }
    }

    @JvmStatic
    fun addCrash(throwable: Throwable) {
        if (getApp().isAnalyticEnable) {
            FirebaseCrashlytics.getInstance().recordException(throwable)
        }
    }

    private fun getRemoteConfig(): FirebaseRemoteConfig? {
        return FirebaseRemoteConfig.getInstance().apply {
            setConfigSettingsAsync(
                FirebaseRemoteConfigSettings.Builder()
                    .setFetchTimeoutInSeconds(3600L)
                    .build()
            )
            setDefaultsAsync(R.xml.remote_config_defaults)
        }
    }


    fun checkRatingConfig(onRatingApp: OnRatingApp?) {
        if (getApp().isAnalyticEnable) {
            getRemoteConfig()?.let { config ->
                config.fetch(if (BuildConfig.DEBUG) 0 else TIME_FETCH).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        config.activate()
                        val isRatingApp = config.getBoolean(KEY_RATING)
                        onRatingApp?.onRatingApp(isRatingApp)
                    }
                }
            }

        }
    }

    @JvmStatic
    fun isCaptchaEnable(block: (isEnable: Boolean) -> Unit) {
        if (getApp().isAnalyticEnable) {
            getRemoteConfig()?.let { config ->
                config.fetch(if (BuildConfig.DEBUG) 0 else TIME_FETCH).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        config.activate()
                        block(config.getBoolean(KEY_CAPTCHA))
                    }
                }
            }
        }
    }

    fun getServiceUrls(): LiveData<Array<String>?> {
        val liveData = MutableLiveData<Array<String>>(null)
        getRemoteConfig()?.let { config ->
            config.fetch(if (BuildConfig.DEBUG) 0 else TIME_FETCH).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    config.activate()
                    liveData.value =
                        arrayOf(config.getString(KEY_TERMS_OF_SERVICE), config.getString(KEY_PRIVACY_POLICY))
                }
            }
        }
        return liveData
    }

    private fun addAnalytics(event: String, bundle: Bundle) {
        if (getApp().isAnalyticEnable) {
            if (firebaseAnalytics == null) {
                firebaseAnalytics = FirebaseAnalytics.getInstance(getApp())
            }
            firebaseAnalytics!!.logEvent(event, bundle)
        } else {
            firebaseAnalytics = null
        }
    }

    @JvmStatic
    fun addAnalyticsCreatePortal(portal: String, login: String) {
        val bundle = Bundle()
        bundle.putString(AnalyticsKeys.PORTAL, portal)
        bundle.putString(AnalyticsKeys.LOGIN, login)
        addAnalytics(AnalyticsEvents.CREATE_PORTAL, bundle)
    }

    @JvmStatic
    fun addAnalyticsCheckPortal(portal: String, result: String, error: String?) {
        val bundle = Bundle()
        bundle.putString(AnalyticsKeys.PORTAL, portal)
        bundle.putString(AnalyticsEvents.OPERATION_RESULT, result)
        bundle.putString(AnalyticsEvents.OPERATION_DETAILS, error ?: AnalyticsKeys.NONE)
        addAnalytics(AnalyticsEvents.CHECK_PORTAL, bundle)
    }

    fun addAnalyticsLogin(portal: String, provider: String?) {
        val bundle = Bundle()
        bundle.putString(AnalyticsKeys.PORTAL, portal)
        bundle.putString(AnalyticsKeys.PROVIDER, provider ?: AnalyticsKeys.NONE)
        addAnalytics(AnalyticsEvents.LOGIN_PORTAL, bundle)
    }

    @JvmStatic
    fun addAnalyticsSwitchAccount(portal: String?) {
        val bundle = Bundle()
        bundle.putString(AnalyticsKeys.PORTAL, portal)
        addAnalytics(AnalyticsEvents.SWITCH_ACCOUNT, bundle)
    }

    @JvmStatic
    fun addAnalyticsCreateEntity(portal: String, isFile: Boolean, extension: String?) {
        val bundle = Bundle()
        bundle.putString(AnalyticsKeys.PORTAL, portal)
        bundle.putString(AnalyticsKeys.ON_DEVICE, "false")
        bundle.putString(AnalyticsKeys.TYPE, if (isFile) AnalyticsKeys.TYPE_FILE else AnalyticsKeys.TYPE_FOLDER)
        bundle.putString(AnalyticsKeys.FILE_EXT, extension ?: AnalyticsKeys.NONE)
        addAnalytics(AnalyticsEvents.CREATE_ENTITY, bundle)
    }

    @JvmStatic
    fun addAnalyticsOpenEntity(portal: String, extension: String) {
        val bundle = Bundle()
        bundle.putString(AnalyticsKeys.PORTAL, portal)
        bundle.putString(AnalyticsKeys.ON_DEVICE, "false")
        bundle.putString(AnalyticsKeys.FILE_EXT, extension)
        addAnalytics(AnalyticsEvents.OPEN_EDITOR, bundle)
    }

    @JvmStatic
    fun addAnalyticsOpenExternal(portal: String, extension: String) {
        val bundle = Bundle()
        bundle.putString(AnalyticsKeys.PORTAL, portal)
        bundle.putString(AnalyticsKeys.ON_DEVICE, "false")
        bundle.putString(AnalyticsKeys.FILE_EXT, extension)
        addAnalytics(AnalyticsEvents.OPEN_EXTERNAL, bundle)
    }

    object AnalyticsEvents {
        const val CREATE_PORTAL = "portal_create"
        const val CREATE_ENTITY = "create_entity"
        const val CHECK_PORTAL = "check_portal"
        const val LOGIN_PORTAL = "portal_login"
        const val SWITCH_ACCOUNT = "account_switch"
        const val OPEN_PDF = "open_pdf"
        const val OPEN_EDITOR = "open_editor"
        const val OPEN_MEDIA = "open_media"
        const val OPEN_EXTERNAL = "open_external"
        const val OPERATION_RESULT = "operation_result"
        const val OPERATION_DETAILS = "operation_details"
    }

    object AnalyticsKeys {
        const val NONE = "none"
        const val SUCCESS = "success"
        const val FAILED = "failed"
        const val PORTAL = "portal"
        const val LOGIN = "email"
        const val PROVIDER = "provider"
        const val ON_DEVICE = "onDevice"
        const val TYPE = "type"
        const val FILE_EXT = "fileExt"
        const val TYPE_FILE = "file"
        const val TYPE_FOLDER = "folder"
    }

    interface OnRatingApp {
        fun onRatingApp(isRating: Boolean)
    }
}