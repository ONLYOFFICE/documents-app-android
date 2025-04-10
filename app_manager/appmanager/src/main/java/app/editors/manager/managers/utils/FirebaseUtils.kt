package app.editors.manager.managers.utils

import android.content.Context
import android.os.Bundle
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import app.documents.core.model.cloud.CloudAccount
import app.editors.manager.BuildConfig
import app.editors.manager.R
import app.editors.manager.app.App.Companion.getApp
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import lib.toolkit.base.managers.utils.FileUtils
import retrofit2.HttpException

object FirebaseUtils {

    private const val KEY_RATING = "android_documents_rating"
    private const val KEY_CAPTCHA = "recaptcha_for_portal_registration"
    private const val KEY_TERMS_OF_SERVICE = "link_terms_of_service"
    private const val KEY_PRIVACY_POLICY = "link_privacy_policy"
    private const val KEY_ALLOW_COAUTHORING = "allow_coauthoring"
    private const val KEY_SDK_FULLY = "check_sdk_fully"
    private const val KEY_GOOGLE_DRIVE = "allow_google_drive"
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

    @JvmStatic
    fun addCrash(httpException: HttpException) {
        if (getApp().isAnalyticEnable) {
            val message = httpException.response()?.errorBody()?.string()
            if (message != null) addCrash(message = message) else addCrash(throwable = httpException)
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

    fun isGoogleDriveEnable(block: (isEnable: Boolean) -> Unit) {
        if (getApp().isAnalyticEnable) {
            getRemoteConfig()?.let { config ->
                config.fetch(if (BuildConfig.DEBUG) 0 else TIME_FETCH).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        config.activate()
                        block(config.getBoolean(KEY_GOOGLE_DRIVE))
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

    fun checkSdkVersion(
        context: Context,
        account: CloudAccount,
        onResult: (isCoauthoring: Boolean) -> Unit
    ) {
        getSdk { allowCoauthoring, checkSdkFully ->
            if (allowCoauthoring) {
                onResult(false)
                return@getSdk
            }

            val webSdk = account
                .portal
                .version
                .documentServerVersion
                .replace(".", "")

            if (webSdk.isEmpty()) {
                onResult(false)
                return@getSdk
            }

            val localSdk = FileUtils.readSdkVersion(context).replace(".", "")

            var maxVersionIndex = 2

            if (!checkSdkFully) {
                maxVersionIndex = 1
            }

            for (i in 0..maxVersionIndex) {
                if (webSdk[i] != localSdk[i]) {
                    onResult(false)
                    return@getSdk
                }
            }

            onResult(true)
        }
    }

    /**
     * @param block First allow_coauthoring, second check_sdk_fully
     */
    fun getSdk(block: (allowCoauthoring: Boolean, checkSdkFully: Boolean) -> Unit)  {
        getRemoteConfig()?.let { config ->
            config.fetch(if (BuildConfig.DEBUG) 0 else TIME_FETCH)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        config.activate()
                        block(
                            config.getBoolean(KEY_ALLOW_COAUTHORING),
                            config.getBoolean(KEY_SDK_FULLY)
                        )
                    } else {
                        block(true, false)
                    }
                }
        } ?: {
            block(true, false)
        }
    }

    fun getLocalServicesUrl(context: Context): Array<String> {
        return arrayOf(context.getString(R.string.app_url_terms), context.getString(R.string.app_url_policy))
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