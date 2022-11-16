package app.documents.core.storage.preference

import android.content.Context
import app.documents.core.network.common.contracts.ApiContract
import app.documents.core.storage.account.CloudAccount

class NetworkSettings(context: Context) {

    companion object {
        private val TAG: String = NetworkSettings::class.java.simpleName

        private const val KEY_SSL_STATE = "KEY_SSL_STATE"
        private const val KEY_CIPHER = "KEY_CIPHER"
        private const val KEY_URL = "KEY_URL"
        private const val KEY_SCHEME = "KEY_SCHEME"
        private const val KEY_SSO = "KEY_SSO"
        private const val KEY_SSO_LABEL = "KEY_SSO_LABEL"
        private const val KEY_LDAP = "KEY_LDAP"
        private const val KEY_VERSION = "KEY_VERSION"
        private const val KEY_DOC_SPACE = "DOC_SPACE"

        private const val TAG_SUFFIX_INFO = "info"
    }

    private val preferences = context.getSharedPreferences(TAG, Context.MODE_PRIVATE)

    val isPortalInfo: Boolean
        get() = getPortal().endsWith(TAG_SUFFIX_INFO)

    var ssoUrl: String
        set(value) {
            preferences.edit().putString(KEY_SSO, value).apply()
        }
        get() = preferences.getString(KEY_SSO, "") ?: ""

    var ssoLabel: String
        set(value) {
            preferences.edit().putString(KEY_SSO_LABEL, value).apply()
        }
        get() = preferences.getString(KEY_SSO_LABEL, "") ?: ""

    var ldap: Boolean
        set(value) {
            preferences.edit().putBoolean(KEY_LDAP, value).apply()
        }
        get() = preferences.getBoolean(KEY_LDAP, false)

    var serverVersion: String
    set(value) {
        preferences.edit().putString(KEY_VERSION, value).apply()
    }
    get() = preferences.getString(KEY_VERSION, "") ?: ""

    var isDocSpace: Boolean
        set(value) {
            preferences.edit().putBoolean(KEY_DOC_SPACE, value).apply()
        }
        get() = preferences.getBoolean(KEY_DOC_SPACE, false)

    fun setBaseUrl(string: String) {
        val builder = StringBuilder(string)
        if (!string.endsWith("/") && string.isNotEmpty()) {
            builder.append("/")
        }
        preferences.edit().putString(KEY_URL, builder.toString()).apply()
    }

    fun setScheme(scheme: String) {
        preferences.edit().putString(KEY_SCHEME, scheme).apply()
    }

    fun getScheme(): String {
        return preferences.getString(KEY_SCHEME, ApiContract.SCHEME_HTTPS) ?: ApiContract.SCHEME_HTTPS
    }

    fun getPortal(): String {
        return preferences.getString(KEY_URL, "")?.removeSuffix("/") ?: ""
    }

    fun getBaseUrl(): String {
        val url: String =  preferences.getString(KEY_URL, ApiContract.DEFAULT_HOST) ?: ApiContract.DEFAULT_HOST
        return if (url.contains(ApiContract.SCHEME_HTTPS) || url.contains(ApiContract.SCHEME_HTTP)) {
            url
        } else {
            getScheme() + url
        }

    }

    fun setSslState(isSsl: Boolean) {
        preferences.edit().putBoolean(KEY_SSL_STATE, isSsl).apply()
    }

    fun getSslState() = preferences.getBoolean(KEY_SSL_STATE, true)

    fun setCipher(isCipher: Boolean) {
        preferences.edit().putBoolean(KEY_CIPHER, isCipher).apply()
    }

    fun getCipher() = preferences.getBoolean(KEY_CIPHER, false)

    fun setDefault() {
        serverVersion = ""
        ssoLabel = ""
        ssoUrl = ""
        ldap = false
        setSslState(true)
        setCipher(false)
        setBaseUrl("")
        setScheme(ApiContract.SCHEME_HTTPS)
    }

    fun setSettingsByAccount(account: CloudAccount) {
        setDefault()
        setBaseUrl(account.scheme + account.portal)
        setCipher(account.isSslCiphers)
        setSslState(account.isSslState)
        setScheme(account.scheme ?: ApiContract.SCHEME_HTTPS)
        serverVersion = account.serverVersion
    }
}