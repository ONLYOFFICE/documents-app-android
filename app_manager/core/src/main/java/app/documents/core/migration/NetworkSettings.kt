package app.documents.core.migration

import android.content.Context
import app.documents.core.model.cloud.Scheme
import app.documents.core.network.common.contracts.ApiContract
import javax.inject.Inject

internal class NetworkSettings @Inject constructor(context: Context) {

    companion object {
        private val TAG: String = NetworkSettings::class.java.simpleName

        private const val KEY_URL = "KEY_URL"
        private const val KEY_SCHEME = "KEY_SCHEME"
        private const val KEY_SSO = "KEY_SSO"
        private const val KEY_SSO_LABEL = "KEY_SSO_LABEL"
        private const val KEY_LDAP = "KEY_LDAP"
        private const val KEY_VERSION = "KEY_VERSION"
        private const val KEY_DOCUMENT_VERSION = "KEY_DOCUMENT_VERSION"
        private const val KEY_DOC_SPACE = "DOC_SPACE"
    }

    private val preferences = context.getSharedPreferences(TAG, Context.MODE_PRIVATE)

    val ssoUrl: String
        get() = preferences.getString(KEY_SSO, "") ?: ""

    val ssoLabel: String
        get() = preferences.getString(KEY_SSO_LABEL, "") ?: ""

    val ldap: Boolean
        get() = preferences.getBoolean(KEY_LDAP, false)

    val serverVersion: String
        get() = preferences.getString(KEY_VERSION, "") ?: ""

    val documentServerVersion: String
        get() = preferences.getString(KEY_DOCUMENT_VERSION, "") ?: ""

    val isDocSpace: Boolean
        get() = preferences.getBoolean(KEY_DOC_SPACE, false)

    val scheme: Scheme
        get() = Scheme.valueOf(preferences.getString(KEY_SCHEME, ApiContract.SCHEME_HTTPS) ?: ApiContract.SCHEME_HTTPS)

    val portalUrl: String
        get() = preferences.getString(KEY_URL, "")?.removeSuffix("/") ?: ""

}