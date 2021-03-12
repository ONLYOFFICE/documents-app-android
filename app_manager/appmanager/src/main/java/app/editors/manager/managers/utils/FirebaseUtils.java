package app.editors.manager.managers.utils;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;

import app.editors.manager.BuildConfig;
import app.editors.manager.R;
import app.editors.manager.app.App;

public class FirebaseUtils {

    public static final class AnalyticsEvents {
        public static final String CREATE_PORTAL = "portal_create";
        public static final String CREATE_ENTITY = "create_entity";
        public static final String CHECK_PORTAL = "check_portal";
        public static final String LOGIN_PORTAL = "portal_login";
        public static final String SWITCH_ACCOUNT = "account_switch";
        public static final String OPEN_PDF = "open_pdf";
        public static final String OPEN_EDITOR = "open_editor";
        public static final String OPEN_MEDIA = "open_media";
        public static final String OPEN_EXTERNAL = "open_external";
        public static final String OPERATION_RESULT = "operation_result";
        public static final String OPERATION_DETAILS = "operation_details";
    }

    public static final class AnalyticsKeys {
        public static final String NONE = "none";
        public static final String SUCCESS = "success";
        public static final String FAILED = "failed";
        public static final String PORTAL = "portal";
        public static final String LOGIN = "email";
        public static final String PROVIDER = "provider";
        public static final String ON_DEVICE = "onDevice";
        public static final String TYPE = "type";
        public static final String FILE_EXT = "fileExt";
        public static final String TYPE_FILE = "file";
        public static final String TYPE_FOLDER = "folder";
    }

    private static final String KEY_BETA = "android_documents_20_beta_enabled";
    private static final String KEY_VERSION = "android_documents_20_beta_version";
    private static final String KEY_RATING = "android_documents_rating";

    private static final long TIME_FETCH = 3600;

    private static FirebaseRemoteConfig sRemoteConfig = null;
    private static FirebaseAnalytics sFirebaseAnalytics = null;

//    public interface OnBeta {
//        void onBetaVersion(boolean isBeta, long version);
//    }

    public interface OnRatingApp {
        void onRatingApp(boolean isRating);
    }

    public static void addCrash(@NonNull final String message) {
        if (App.getApp().isAnalyticEnable()) {
            FirebaseCrashlytics.getInstance().log(message);
        }
    }

    public static void addCrash(@NonNull final Throwable throwable) {
        if (App.getApp().isAnalyticEnable()) {
            FirebaseCrashlytics.getInstance().recordException(throwable);
        }
    }

    private static void initRemoteConfig() {
        if (sRemoteConfig == null && App.getApp().isAnalyticEnable()) {
            sRemoteConfig = FirebaseRemoteConfig.getInstance();
            final FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                    .setFetchTimeoutInSeconds(3600L)
                    .build();
            sRemoteConfig.setConfigSettingsAsync(configSettings);
            sRemoteConfig.setDefaultsAsync(R.xml.remote_config_defaults);
        }
    }

//    //TODO Не испрользовать бета версию
//    public static void checkBetaConfig(@Nullable final OnBeta onBeta) {
//        initRemoteConfig();
//        sRemoteConfig.fetch(BuildConfig.DEBUG ? 0 : TIME_FETCH).addOnCompleteListener(task -> {
//            if (task.isSuccessful()) {
//                sRemoteConfig.activate();
//                final boolean isBeta = sRemoteConfig.getBoolean(KEY_BETA);
//                final long version = sRemoteConfig.getLong(KEY_VERSION);
//                if (onBeta != null) {
//                    onBeta.onBetaVersion(isBeta, version);
//                }
//            }
//        });
//    }

    public static void checkRatingConfig(@Nullable final OnRatingApp onRatingApp) {
        if (App.getApp().isAnalyticEnable()) {
            initRemoteConfig();
            sRemoteConfig.fetch(BuildConfig.DEBUG ? 0 : TIME_FETCH).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    sRemoteConfig.activate();
                    final boolean isRatingApp = sRemoteConfig.getBoolean(KEY_RATING);
                    if (onRatingApp != null) {
                        onRatingApp.onRatingApp(isRatingApp);
                    }
                }
            });
        } else {
            sRemoteConfig = null;
        }
    }

    public static void addAnalytics(@NonNull final String event, @NonNull final Bundle bundle) {
        if (App.getApp().isAnalyticEnable()) {
            if (sFirebaseAnalytics == null) {
                sFirebaseAnalytics = FirebaseAnalytics.getInstance(App.getApp());
            }
            sFirebaseAnalytics.logEvent(event, bundle);
        } else {
            sFirebaseAnalytics = null;
        }
    }

    public static void addAnalyticsCreatePortal(@NonNull final String portal, @NonNull final String login) {
        final Bundle bundle = new Bundle();
        bundle.putString(AnalyticsKeys.PORTAL, portal);
        bundle.putString(AnalyticsKeys.LOGIN, login);
        addAnalytics(AnalyticsEvents.CREATE_PORTAL, bundle);
    }

    public static void addAnalyticsCheckPortal(@NonNull final String portal, @NonNull final String result, @Nullable final String error) {
        final Bundle bundle = new Bundle();
        bundle.putString(AnalyticsKeys.PORTAL, portal);
        bundle.putString(AnalyticsEvents.OPERATION_RESULT, result);
        bundle.putString(AnalyticsEvents.OPERATION_DETAILS, error != null ? error : AnalyticsKeys.NONE);
        addAnalytics(AnalyticsEvents.CHECK_PORTAL, bundle);
    }

    public static void addAnalyticsLogin(@NonNull final String portal, @Nullable final String provider) {
        final Bundle bundle = new Bundle();
        bundle.putString(AnalyticsKeys.PORTAL, portal);
        bundle.putString(AnalyticsKeys.PROVIDER, provider != null ? provider : AnalyticsKeys.NONE);
        addAnalytics(AnalyticsEvents.LOGIN_PORTAL, bundle);
    }

    public static void addAnalyticsSwitchAccount(final String portal) {
        final Bundle bundle = new Bundle();
        bundle.putString(AnalyticsKeys.PORTAL, portal);
        addAnalytics(AnalyticsEvents.SWITCH_ACCOUNT, bundle);
    }

    public static void addAnalyticsCreateEntity(@NonNull final String portal, final boolean isFile, @Nullable final String extension) {
        final Bundle bundle = new Bundle();
        bundle.putString(AnalyticsKeys.PORTAL, portal);
        bundle.putString(AnalyticsKeys.ON_DEVICE, "false");
        bundle.putString(AnalyticsKeys.TYPE, isFile ? AnalyticsKeys.TYPE_FILE : AnalyticsKeys.TYPE_FOLDER);
        bundle.putString(AnalyticsKeys.FILE_EXT, extension != null ? extension : AnalyticsKeys.NONE);
        addAnalytics(AnalyticsEvents.CREATE_ENTITY, bundle);
    }

    public static void addAnalyticsOpenEntity(@NonNull final String portal, @NonNull final String extension) {
        final Bundle bundle = new Bundle();
        bundle.putString(AnalyticsKeys.PORTAL, portal);
        bundle.putString(AnalyticsKeys.ON_DEVICE, "false");
        bundle.putString(AnalyticsKeys.FILE_EXT, extension);
        addAnalytics(AnalyticsEvents.OPEN_EDITOR, bundle);
    }

    public static void addAnalyticsOpenExternal(@NonNull final String portal, @NonNull final String extension) {
        final Bundle bundle = new Bundle();
        bundle.putString(AnalyticsKeys.PORTAL, portal);
        bundle.putString(AnalyticsKeys.ON_DEVICE, "false");
        bundle.putString(AnalyticsKeys.FILE_EXT, extension);
        addAnalytics(AnalyticsEvents.OPEN_EXTERNAL, bundle);
    }

}
