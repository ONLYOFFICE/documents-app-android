package app.editors.manager.managers.tools

import android.content.Context
import android.content.SharedPreferences
import app.documents.core.network.ApiContract
import java.util.*
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

        private val PERSONAL_ADDRESSES: Set<String> = object : TreeSet<String>() {
            init {
                add(ApiContract.PERSONAL_SUBDOMAIN + ".")
            }
        }
    }

    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(TAG, Context.MODE_PRIVATE)

    fun setDefault() {
        setDefaultPortal()
        setDefaultUser()
    }

    private fun setDefaultUser() {
        login = null
        phoneNoise = null
        sortBy = ApiContract.Parameters.VAL_SORT_BY_UPDATED
        sortOrder = ApiContract.Parameters.VAL_SORT_ORDER_DESC
        socialProvider = null
        selfId = ""
        isProjectDisable = false
        isNoPortal = true
        isShowStorageAccess = true
    }

    fun setDefaultPortal() {
        portal = null
        scheme = ApiContract.SCHEME_HTTPS
        isNoPortal = true
    }

    var portal: String?
        get() = sharedPreferences.getString(KEY_1, null)
        set(value) {
            sharedPreferences.edit().putString(KEY_1, value).apply()
        }
    val isPortalInfo: Boolean
        get() = portal?.endsWith(TAG_SUFFIX_INFO) == true
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
    val isPersonalPortal: Boolean
        get() {
            val portal = portal
            if (portal != null) {
                for (address in PERSONAL_ADDRESSES) {
                    if (portal.contains(address)) {
                        return true
                    }
                }
            }
            return false
        }

    var scheme: String?
        get() = sharedPreferences.getString(KEY_16, ApiContract.SCHEME_HTTPS)
        set(value) {
            sharedPreferences.edit().putString(KEY_16, value).apply()
        }
    var socialProvider: String?
        get() = sharedPreferences.getString(KEY_17, null)
        set(value) {
            sharedPreferences.edit().putString(KEY_17, value).apply()
        }
    var selfId: String?
        get() = sharedPreferences.getString(KEY_19, "")
        set(value) {
            sharedPreferences.edit().putString(KEY_19, value).apply()
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
        userSession = userSession + 1
    }

    var isNoPortal: Boolean
        get() = sharedPreferences.getBoolean(KEY_31, true)
        set(isNoPortal) {
            sharedPreferences.edit().putBoolean(KEY_31, isNoPortal).apply()
        }
    var secretKey: String?
        get() = sharedPreferences.getString(KEY_30, "")
        set(secretKey) {
            sharedPreferences.edit().putString(KEY_30, secretKey).apply()
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
    var serverVersion: String?
        get() = sharedPreferences.getString(KEY_29, "")
        set(value) {
            sharedPreferences.edit().putString(KEY_29, value).apply()
        }

    fun setWifiState(wifiState: Boolean) {
        sharedPreferences.edit().putBoolean(KEY_WIFI_STATE, wifiState).apply()
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

}