package app.editors.manager.managers.tools;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.Html;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Set;
import java.util.TreeSet;

import app.editors.manager.app.Api;
import app.editors.manager.mvp.models.base.AppContext;
import lib.toolkit.base.managers.utils.CryptUtils;


public class PreferenceTool {

    public static final String TAG = "PreferenceUtil";
    public static final int RATING_DEFAULT = 0;
    public static final int RATING_THRESHOLD = 4;
    private static final String TAG_SUFFIX_INFO = "info";


    private static Set<String> PERSONAL_ADDRESSES = new TreeSet<String>() {{
        add(Api.PERSONAL_SUBDOMAIN + ".");
    }};


    private final String KEY_1 = "KEY_1";
    private final String KEY_2 = "KEY_2";
    private final String KEY_3 = "KEY_3";
    private final String KEY_4 = "KEY_4";
    private final String KEY_5 = "KEY_5";
    private final String KEY_6 = "KEY_6";
    private final String KEY_7 = "KEY_7";
    private final String KEY_8 = "KEY_8";
    private final String KEY_9 = "KEY_9";
    private final String KEY_10 = "KEY_10";
    private final String KEY_11 = "KEY_11";
    private final String KEY_12 = "KEY_12";
    private final String KEY_13 = "KEY_13";
    private final String KEY_14 = "KEY_14";
    private final String KEY_15 = "KEY_15";
    private final String KEY_16 = "KEY_16";
    private final String KEY_17 = "KEY_17";
    private final String KEY_18 = "KEY_18";
    private final String KEY_19 = "KEY_19";
    private final String KEY_20 = "KEY_20";
    private final String KEY_21 = "KEY_21";
    private final String KEY_22 = "KEY_22";
    private final String KEY_23 = "KEY_23";
    private final String KEY_24 = "KEY_24";
    private final String KEY_25 = "KEY_25";
    private final String KEY_26 = "KEY_26";
    private final String KEY_27 = "KEY_27";
    private final String KEY_28 = "KEY_28";
    private final String KEY_29 = "KEY_29";
    private final String KEY_30 = "KEY_30";
    private final String KEY_31 = "KEY_31";
    private final String KEY_WIFI_STATE = "KEY_WIFI_STATE";
    private final String KEY_ANALYTIC = "KEY_ANALYTIC";
    private final String KEY_STORAGE_ACCESS = "KEY_STORAGE_ACCESS";

    private SharedPreferences mSharedPreferences;

    public PreferenceTool(@NonNull Context context) {
        mSharedPreferences = context.getSharedPreferences(TAG, context.MODE_PRIVATE);
    }

    public void setDefault() {
        setDefaultPortal();
        setDefaultUser();
    }

    public void setDefaultUser() {
        setLogin(null);
        setPassword(null);
        setToken(null);
        setPhoneNoise(null);
        setSortBy(Api.Parameters.VAL_SORT_BY_UPDATED);
        setSortOrder(Api.Parameters.VAL_SORT_ORDER_DESC);
        setAdmin(false);
        setVisitor(false);
        setOwner(false);
        setUserAvatarUrl("");
        setUserDisplayName("");
        setUserFirstName("");
        setUserLastName("");
        setSocialProvider(null);
        setSocialToken(null);
        setSelfId("");
        setProjectDisable(false);
        setServerVersion("");
        setNoPortal(true);
        setShowStorageAccess(true);
    }

    public void setDefaultPortal() {
        setPortal(null);
        setSsoUrl(null);
        setSsoLabel(null);
        setLdap(false);
        setScheme(Api.SCHEME_HTTPS);
        setSslState(true);
        setSslCiphers(false);
        setNoPortal(true);
    }

    @Nullable
    public String getPortal() {
        return mSharedPreferences.getString(KEY_1, null);
    }

    public void setPortal(final String value) {
        mSharedPreferences.edit().putString(KEY_1, value).commit();
    }

    public boolean isPortalInfo() {
        return getPortal() != null && getPortal().endsWith(TAG_SUFFIX_INFO);
    }

    @Nullable
    public String getLogin() {
        return mSharedPreferences.getString(KEY_2, null);
    }

    public void setLogin(final String value) {
        mSharedPreferences.edit().putString(KEY_2, value).commit();
    }

    @Nullable
    public String getPassword() {
        return CryptUtils.decryptAES128(mSharedPreferences.getString(KEY_3, null), getKey());
    }

    public void setPassword(final String value) {
        mSharedPreferences.edit().putString(KEY_3, CryptUtils.encryptAES128(value, getKey())).commit();
    }

    @Nullable
    public String getToken() {
        return mSharedPreferences.getString(KEY_4, null);
    }

    public void setToken(final String value) {
        mSharedPreferences.edit().putString(KEY_4, value).commit();
    }

    @Nullable
    public String getSsoUrl() {
        return mSharedPreferences.getString(KEY_5, null);
    }

    public void setSsoUrl(final String value) {
        mSharedPreferences.edit().putString(KEY_5, value).commit();
    }

    @Nullable
    public String getSsoLabel() {
        return mSharedPreferences.getString(KEY_6, null);
    }

    public void setSsoLabel(final String value) {
        mSharedPreferences.edit().putString(KEY_6, value).commit();
    }

    public boolean getLdap() {
        return mSharedPreferences.getBoolean(KEY_7, false);
    }

    public void setLdap(final boolean value) {
        mSharedPreferences.edit().putBoolean(KEY_7, value).commit();
    }

    @Nullable
    public String getPhoneNoise() {
        return mSharedPreferences.getString(KEY_8, null);
    }

    public void setPhoneNoise(final String value) {
        mSharedPreferences.edit().putString(KEY_8, value).commit();
    }

    @NonNull
    public String getSortBy() {
        return mSharedPreferences.getString(KEY_9, Api.Parameters.VAL_SORT_BY_UPDATED);
    }

    public void setSortBy(final String value) {
        mSharedPreferences.edit().putString(KEY_9, value).commit();
    }

    @NonNull
    public String getSortOrder() {
        return mSharedPreferences.getString(KEY_10, Api.Parameters.VAL_SORT_ORDER_DESC);
    }

    public void setSortOrder(final String value) {
        mSharedPreferences.edit().putString(KEY_10, value).commit();
    }

    public boolean getIsAdmin() {
        return mSharedPreferences.getBoolean(KEY_11, false);
    }

    public void setAdmin(final boolean value) {
        mSharedPreferences.edit().putBoolean(KEY_11, value).commit();
    }

    public boolean getIsVisitor() {
        return mSharedPreferences.getBoolean(KEY_12, false);
    }

    public void setVisitor(final boolean value) {
        mSharedPreferences.edit().putBoolean(KEY_12, value).commit();
    }

    public boolean getIsOwner() {
        return mSharedPreferences.getBoolean(KEY_13, false);
    }

    public void setOwner(final boolean value) {
        mSharedPreferences.edit().putBoolean(KEY_13, value).commit();
    }

    public String getUserAvatarUrl() {
        return mSharedPreferences.getString(KEY_14, "");
    }

    public void setUserAvatarUrl(final String value) {
        mSharedPreferences.edit().putString(KEY_14, value).commit();
    }

    public String getUserDisplayName() {
        return mSharedPreferences.getString(KEY_15, "");
    }

    public void setUserDisplayName(final String value) {
        mSharedPreferences.edit().putString(KEY_15, Html.fromHtml(value).toString()).commit();
    }

    public boolean isPersonalPortal() {
        final String portal = getPortal();
        if (portal != null) {
            for (String address : PERSONAL_ADDRESSES) {
                if (portal.contains(address)) {
                    return true;
                }
            }
        }
        return false;
    }

    public String getScheme() {
        return mSharedPreferences.getString(KEY_16, Api.SCHEME_HTTPS);
    }

    public void setScheme(final String value) {
        mSharedPreferences.edit().putString(KEY_16, value).commit();
    }

    public boolean isHttpsConnect() {
        return Api.SCHEME_HTTPS.equals(getScheme());
    }

    public String getPortalFullPath() {
        return getScheme() != null && getPortal() != null ? getScheme() + getPortal() : "";
    }

    public String getSocialProvider() {
        return mSharedPreferences.getString(KEY_17, null);
    }

    public void setSocialProvider(final String value) {
        mSharedPreferences.edit().putString(KEY_17, value).commit();
    }

    public String getSocialToken() {
        return mSharedPreferences.getString(KEY_18, null);
    }

    public void setSocialToken(final String value) {
        mSharedPreferences.edit().putString(KEY_18, value).commit();
    }

    public boolean isSocialLogin() {
        return getSocialProvider() != null;
    }

    public String getSelfId() {
        return mSharedPreferences.getString(KEY_19, "");
    }

    public void setSelfId(final String value) {
        mSharedPreferences.edit().putString(KEY_19, value).commit();
    }

    public boolean getOnBoarding() {
        return mSharedPreferences.getBoolean(KEY_20, false);
    }

    public void setOnBoarding(final boolean value) {
        mSharedPreferences.edit().putBoolean(KEY_20, value).commit();
    }

    public boolean getSslState() {
        return mSharedPreferences.getBoolean(KEY_21, true);
    }

    public void setSslState(final boolean value) {
        mSharedPreferences.edit().putBoolean(KEY_21, value).commit();
    }

    public boolean getSslCiphers() {
        return mSharedPreferences.getBoolean(KEY_22, false);
    }

    public void setSslCiphers(final boolean value) {
        mSharedPreferences.edit().putBoolean(KEY_22, value).commit();
    }

    public AppContext getAppContext() {
        return AppContext.getEnum(mSharedPreferences.getInt(KEY_23, AppContext.NONE.getValue()));
    }

    public void setAppContext(final AppContext appContext) {
        mSharedPreferences.edit().putInt(KEY_23, appContext.getValue()).commit();
    }

    public boolean getIsRateOn() {
        return mSharedPreferences.getBoolean(KEY_24, true);
    }

    public void setIsRateOn(final boolean value) {
        mSharedPreferences.edit().putBoolean(KEY_24, value).commit();
    }

    public String getUserFirstName() {
        return mSharedPreferences.getString(KEY_25, "");
    }

    public void setUserFirstName(final String value) {
        mSharedPreferences.edit().putString(KEY_25, Html.fromHtml(value).toString()).commit();
    }

    public String getUserLastName() {
        return mSharedPreferences.getString(KEY_26, "");
    }

    public void setUserLastName(final String value) {
        mSharedPreferences.edit().putString(KEY_26, Html.fromHtml(value).toString()).commit();
    }

    @Nullable
    private String getKey() {
        return getPortal() != null && getLogin() != null ? getPortal() + getLogin() : null;
    }

    public long getUserSession() {
        return mSharedPreferences.getLong(KEY_27, 0L);
    }

    public void setUserSession(final long value) {
        mSharedPreferences.edit().putLong(KEY_27, value).commit();
    }

    public void setUserSession() {
        setUserSession(getUserSession() + 1);
    }

    public void setNoPortal(final boolean isNoPortal) {
        mSharedPreferences.edit().putBoolean(KEY_31, isNoPortal).commit();
    }

    public boolean isNoPortal() {
        return mSharedPreferences.getBoolean(KEY_31, true);
    }

    public void setSecretKey(String secretKey) {
        mSharedPreferences.edit().putString(KEY_30, secretKey).apply();
    }

    public String getSecretKey() {
        return mSharedPreferences.getString(KEY_30, "");
    }

    public void setProjectDisable(boolean value) {
        mSharedPreferences.edit().putBoolean(KEY_28, value).apply();
    }

    public boolean isProjectDisable() {
        return mSharedPreferences.getBoolean(KEY_28, false);
    }

    public void setServerVersion(String value) {
        mSharedPreferences.edit().putString(KEY_29, value).apply();
    }

    public String getServerVersion() {
        return mSharedPreferences.getString(KEY_29, "");
    }

    public void setWifiState(boolean wifiState){
        mSharedPreferences.edit().putBoolean(KEY_WIFI_STATE, wifiState).apply();
    }

    public boolean getUploadWifiState(){
        return mSharedPreferences.getBoolean(KEY_WIFI_STATE, false);
    }

    public void setAnalyticEnable(boolean isEnable) {
        mSharedPreferences.edit().putBoolean(KEY_ANALYTIC, isEnable).apply();
    }

    public boolean isAnalyticEnable() {
        return mSharedPreferences.getBoolean(KEY_ANALYTIC, true);
    }

    public void setShowStorageAccess(boolean isShow) {
        mSharedPreferences.edit().putBoolean(KEY_STORAGE_ACCESS, isShow).apply();
    }

    public boolean isShowStorageAccess() {
        return mSharedPreferences.getBoolean(KEY_STORAGE_ACCESS, true);
    }

}
