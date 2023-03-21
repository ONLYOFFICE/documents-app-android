package app.documents.core.network.manager.models.base

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class Capabilities {

    @SerializedName(KEY_LDAP)
    @Expose
    var ldapEnabled = false

    @SerializedName(KEY_SSO_URL)
    @Expose
    var ssoUrl = ""

    @SerializedName(KEY_SSO_LABEL)
    @Expose
    var ssoLabel = ""

    @SerializedName(KEY_PROVIDERS)
    @Expose
    var providers: List<String>? = null

    companion object {
        const val KEY_LDAP = "ldapEnabled"
        const val KEY_SSO_URL = "ssoUrl"
        const val KEY_SSO_LABEL = "ssoLabel"
        const val KEY_PROVIDERS = "providers"
    }
}