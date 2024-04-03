package app.editors.manager.managers.tools

import android.content.Context
import android.content.SharedPreferences
import app.documents.core.network.common.contracts.ApiContract
import app.editors.manager.mvp.models.filter.Filter
import app.editors.manager.mvp.models.states.PasscodeLockState
import app.editors.manager.mvp.models.states.toJson
import javax.inject.Inject

class PreferenceTool @Inject constructor(val context: Context) {

    companion object {
        const val TAG = "PreferenceUtil"
        const val RATING_DEFAULT = 0
        const val RATING_THRESHOLD = 4
        private const val TAG_SUFFIX_INFO = "info"

        private const val KEY_1 = "KEY_1"
        private const val KEY_2 = "KEY_2"
        private const val KEY_8 = "KEY_8"
        private const val KEY_9 = "KEY_9"
        private const val KEY_10 = "KEY_10"
        private const val KEY_16 = "KEY_16"
        private const val KEY_17 = "KEY_17"
        private const val KEY_19 = "KEY_19"
        private const val KEY_20 = "KEY_20"
        private const val KEY_24 = "KEY_24"
        private const val KEY_27 = "KEY_27"
        private const val KEY_28 = "KEY_28"
        private const val KEY_29 = "KEY_29"
        private const val KEY_30 = "KEY_30"
        private const val KEY_31 = "KEY_31"
        private const val KEY_32 = "KEY_32"
        private const val KEY_WIFI_STATE = "KEY_WIFI_STATE"
        private const val KEY_ANALYTIC = "KEY_ANALYTIC"
        private const val KEY_STORAGE_ACCESS = "KEY_STORAGE_ACCESS"
        private const val KEY_PASSCODE = "KEY_PASSCODE"
        private const val KEY_TIMESTAMP = "KEY_TIMESTAMP"
        private const val KEY_DEVICE_TOKEN = "KEY_DEVICE_TOKEN"
        private const val KEY_FILTER = "KEY_FILTER"
        private const val KEY_SYSTEM_LOCALE = "KEY_SYSTEM_LOCALE"
        private const val KEY_SKIP_LOCALE_CONFIRMATION = "KEY_SKIP_LOCALE_CONFIRMATION"
    }

    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(TAG, Context.MODE_PRIVATE)

    var login: String?
        get() = sharedPreferences.getString(KEY_2, null)
        set(value) {
            sharedPreferences.edit().putString(KEY_2, value).apply()
        }

    var phoneNoise: String?
        get() = sharedPreferences.getString(KEY_8, null)
        set(value) {
            sharedPreferences.edit().putString(KEY_8, value).apply()
        }

    var sortBy: String?
        get() = sharedPreferences.getString(KEY_9, ApiContract.Parameters.VAL_SORT_BY_UPDATED)
        set(value) {
            sharedPreferences.edit().putString(KEY_9, value).apply()
        }

    var sortOrder: String?
        get() = sharedPreferences.getString(KEY_10, ApiContract.Parameters.VAL_SORT_ORDER_DESC)
        set(value) {
            sharedPreferences.edit().putString(KEY_10, value).apply()
        }

    var onBoarding: Boolean
        get() = sharedPreferences.getBoolean(KEY_20, false)
        set(value) {
            sharedPreferences.edit().putBoolean(KEY_20, value).apply()
        }

    var isRateOn: Boolean
        get() = sharedPreferences.getBoolean(KEY_24, true)
        set(value) {
            sharedPreferences.edit().putBoolean(KEY_24, value).apply()
        }

    var userSession: Long
        get() = sharedPreferences.getLong(KEY_27, 0L)
        set(value) {
            sharedPreferences.edit().putLong(KEY_27, value).apply()
        }

    fun setUserSession() {
        userSession += 1
    }

    var isProjectDisable: Boolean
        get() = sharedPreferences.getBoolean(KEY_28, false)
        set(value) {
            sharedPreferences.edit().putBoolean(KEY_28, value).apply()
        }

    fun setFavoritesEnable(value: Boolean) {
        sharedPreferences.edit().putBoolean(KEY_32, value).apply()
    }

    val isFavoritesEnabled: Boolean
        get() = sharedPreferences.getBoolean(KEY_32, true)

    fun setWifiState(wifiState: Boolean) {
        sharedPreferences.edit().putBoolean(KEY_WIFI_STATE, wifiState).apply()
    }

    var modules: String
        get() = sharedPreferences.getString("KEY_MODULES", "") ?: ""
        set(value) {
            sharedPreferences.edit().putString("KEY_MODULES", value).apply()
        }

    var fileData: String
        get() = sharedPreferences.getString("KEY_FILE_DATA", "") ?: ""
        set(value) {
            sharedPreferences.edit().putString("KEY_FILE_DATA", value).apply()
        }

    val uploadWifiState: Boolean
        get() = sharedPreferences.getBoolean(KEY_WIFI_STATE, false)

    var isAnalyticEnable: Boolean
        get() = sharedPreferences.getBoolean(KEY_ANALYTIC, true)
        set(isEnable) {
            sharedPreferences.edit().putBoolean(KEY_ANALYTIC, isEnable).apply()
        }

    var isShowStorageAccess: Boolean
        get() = sharedPreferences.getBoolean(KEY_STORAGE_ACCESS, true)
        set(isShow) {
            sharedPreferences.edit().putBoolean(KEY_STORAGE_ACCESS, isShow).apply()
        }

    var passcodeLock: PasscodeLockState
        get() = PasscodeLockState.fromJson(sharedPreferences.getString(KEY_PASSCODE, null))
        set(passcode) {
            sharedPreferences.edit().putString(KEY_PASSCODE, passcode.toJson()).apply()
        }

    var deviceMessageToken: String
        get() = sharedPreferences.getString(KEY_DEVICE_TOKEN, "") ?: ""
        set(value) {
            sharedPreferences.edit().putString(KEY_DEVICE_TOKEN, value).apply()
        }

    var filter: Filter
        get() = Filter.toObject(sharedPreferences.getString(KEY_FILTER, Filter.toJson(Filter())))
        set(filter) {
            sharedPreferences.edit().putString(KEY_FILTER, Filter.toJson(filter)).apply()
        }

    var systemLocale: String?
        get() = sharedPreferences.getString(KEY_SYSTEM_LOCALE, "")
        set(value) {
            sharedPreferences.edit().putString(KEY_SYSTEM_LOCALE, value).apply()
        }

    var skipLocaleConfirmation: Boolean
        get() = sharedPreferences.getBoolean(KEY_SKIP_LOCALE_CONFIRMATION, false)
        set(value) {
            sharedPreferences.edit().putBoolean(KEY_SKIP_LOCALE_CONFIRMATION, value).apply()
        }
}