package app.editors.manager.managers.tools;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Set;
import java.util.TreeSet;

import app.documents.core.network.ApiContract;


public class PreferenceTool {

    public static final String TAG = "PreferenceUtil";
    public static final int RATING_DEFAULT = 0;
    public static final int RATING_THRESHOLD = 4;
    private static final String TAG_SUFFIX_INFO = "info";


    private static Set<String> PERSONAL_ADDRESSES = new TreeSet<String>() {{
        add(ApiContract.PERSONAL_SUBDOMAIN + ".");
    }};


    private final String KEY_1 = "KEY_1";
    private final String KEY_2 = "KEY_2";
    private final String KEY_8 = "KEY_8";
    private final String KEY_9 = "KEY_9";
    private final String KEY_10 = "KEY_10";
    private final String KEY_16 = "KEY_16";
    private final String KEY_17 = "KEY_17";
    private final String KEY_19 = "KEY_19";
    private final String KEY_20 = "KEY_20";
    private final String KEY_24 = "KEY_24";
    private final String KEY_27 = "KEY_27";
    private final String KEY_28 = "KEY_28";
    private final String KEY_29 = "KEY_29";
    private final String KEY_30 = "KEY_30";
    private final String KEY_31 = "KEY_31";
    private final String KEY_32 = "KEY_32";
    private final String KEY_WIFI_STATE = "KEY_WIFI_STATE";
    private final String KEY_ANALYTIC = "KEY_ANALYTIC";
    private final String KEY_STORAGE_ACCESS = "KEY_STORAGE_ACCESS";

    private SharedPreferences mSharedPreferences;

    public PreferenceTool(@NonNull Context context) {
        mSharedPreferences = context.getSharedPreferences(TAG, Context.MODE_PRIVATE);
    }

    public void setDefault() {
        setDefaultPortal();
        setDefaultUser();
    }

    public void setDefaultUser() {
        setLogin(null);
        setPhoneNoise(null);
        setSortBy(ApiContract.Parameters.VAL_SORT_BY_UPDATED);
        setSortOrder(ApiContract.Parameters.VAL_SORT_ORDER_DESC);
        setSocialProvider(null);
        setSelfId("");
        setProjectDisable(false);
        setNoPortal(true);
        setShowStorageAccess(true);
    }

    public void setDefaultPortal() {
        setPortal(null);
        setScheme(ApiContract.SCHEME_HTTPS);
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
    public String getPhoneNoise() {
        return mSharedPreferences.getString(KEY_8, null);
    }

    public void setPhoneNoise(final String value) {
        mSharedPreferences.edit().putString(KEY_8, value).commit();
    }

    @NonNull
    public String getSortBy() {
        return mSharedPreferences.getString(KEY_9, ApiContract.Parameters.VAL_SORT_BY_UPDATED);
    }

    public void setSortBy(final String value) {
        mSharedPreferences.edit().putString(KEY_9, value).commit();
    }

    @NonNull
    public String getSortOrder() {
        return mSharedPreferences.getString(KEY_10, ApiContract.Parameters.VAL_SORT_ORDER_DESC);
    }

    public void setSortOrder(final String value) {
        mSharedPreferences.edit().putString(KEY_10, value).commit();
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
        return mSharedPreferences.getString(KEY_16, ApiContract.SCHEME_HTTPS);
    }

    public void setScheme(final String value) {
        mSharedPreferences.edit().putString(KEY_16, value).commit();
    }

    public String getSocialProvider() {
        return mSharedPreferences.getString(KEY_17, null);
    }

    public void setSocialProvider(final String value) {
        mSharedPreferences.edit().putString(KEY_17, value).commit();
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

    public boolean getIsRateOn() {
        return mSharedPreferences.getBoolean(KEY_24, true);
    }

    public void setIsRateOn(final boolean value) {
        mSharedPreferences.edit().putBoolean(KEY_24, value).commit();
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

    public void setFavoritesEnable(boolean value) {
        mSharedPreferences.edit().putBoolean(KEY_32, value).apply();
    }

    public boolean isFavoritesEnabled() {
        return mSharedPreferences.getBoolean(KEY_32, true);
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
