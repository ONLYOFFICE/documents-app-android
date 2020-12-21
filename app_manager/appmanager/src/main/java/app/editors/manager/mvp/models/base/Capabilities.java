package app.editors.manager.mvp.models.base;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Capabilities {

    public static final String KEY_LDAP = "ldapEnabled";
    public static final String KEY_SSO_URL = "ssoUrl";
    public static final String KEY_SSO_LABEL = "ssoLabel";
    public static final String KEY_PROVIDERS = "providers";

    @SerializedName(KEY_LDAP)
    @Expose
    private boolean ldapEnabled = false;

    @SerializedName(KEY_SSO_URL)
    @Expose
    private String ssoUrl = "";

    @SerializedName(KEY_SSO_LABEL)
    @Expose
    private String ssoLabel = "";

    @SerializedName(KEY_PROVIDERS)
    @Expose
    private List<String> providers;

    public boolean getLdapEnabled() {
        return ldapEnabled;
    }

    public void setLdapEnabled(boolean ldapEnabled) {
        this.ldapEnabled = ldapEnabled;
    }

    public String getSsoUrl() {
        return ssoUrl;
    }

    public void setSsoUrl(String ssoUrl) {
        this.ssoUrl = ssoUrl;
    }

    public String getSsoLabel() {
        return ssoLabel;
    }

    public void setSsoLabel(String ssoLabel) {
        this.ssoLabel = ssoLabel;
    }

    public List<String> getProviders() {
        return providers;
    }

    public void setProviders(List<String> providers) {
        this.providers = providers;
    }
}
