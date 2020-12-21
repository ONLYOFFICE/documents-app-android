package app.editors.manager.app;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Process;
import android.util.Log;
import android.webkit.WebView;

import androidx.annotation.NonNull;

import com.google.firebase.FirebaseApp;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.twitter.sdk.android.core.DefaultLogger;
import com.twitter.sdk.android.core.Twitter;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterConfig;

import java.util.Locale;

import app.editors.manager.BuildConfig;
import app.editors.manager.di.component.AppComponent;
import app.editors.manager.di.component.DaggerAppComponent;
import app.editors.manager.di.module.AppModule;
import app.editors.manager.managers.utils.Constants;


public class App extends Application {

    public static final String TAG = App.class.getSimpleName();
    private static App sApp;
    private boolean currentDesktopMode = false;
    private static boolean isDesktop = false;
    private boolean isAnalyticEnable = true;

    private AppComponent mAppComponent;

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        sApp = this;
        initDagger();
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        currentDesktopMode = checkDeXEnabled();
        if (isDesktop != currentDesktopMode) {
            isDesktop = currentDesktopMode;
        }
    }

    boolean checkDeXEnabled() {
        boolean enabled;
        Configuration config = getResources().getConfiguration();
        try {
            Class configClass = config.getClass();
            enabled = configClass.getField("SEM_DESKTOP_MODE_ENABLED").getInt(configClass)
                    == configClass.getField("semDesktopModeEnabled").getInt(config);
            return enabled;
        } catch (NoSuchFieldException | IllegalAccessException | IllegalArgumentException ignored) {
        }
        return false;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        init();
    }

    private void init() {

        /*
         Only android >= pie.
         https://bugs.chromium.org/p/chromium/issues/detail?id=558377
         https://stackoverflow.com/questions/51843546/android-pie-9-0-webview-in-multi-process

         For Android Pie is a separate directory for the process with WebView
         */

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {

            if (getProcess().equals("com.onlyoffice.documents:WebViewerActivity")) {
                WebView.setDataDirectorySuffix("cacheWebView");
            }

        }

        initTwitter();

        isAnalyticEnable = mAppComponent.getPreference().isAnalyticEnable();
        initCrashlytics();
    }

    private String getProcess() {
        ActivityManager manager = (ActivityManager) this.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningAppProcessInfo info : manager.getRunningAppProcesses()) {
            if (info.pid == Process.myPid()) {
                return info.processName;
            }
        }
        return "";
    }

    private void initDagger() {
        mAppComponent = DaggerAppComponent.builder()
                .appModule(new AppModule(this))
                .build();
    }

    private void initTwitter() {
        TwitterConfig config = new TwitterConfig.Builder(this)
                .logger(new DefaultLogger(Log.DEBUG))
                .twitterAuthConfig(new TwitterAuthConfig(Constants.TWITTER_CONSUMER_KEY, Constants.TWITTER_CONSUMER_SECRET))
                .debug(true)
                .build();
        Twitter.initialize(config);
    }

    private void initCrashlytics() {
        if (isAnalyticEnable) {
            FirebaseApp.initializeApp(this);
            if (BuildConfig.DEBUG) {
                FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(false);
            }
        } else {
            try {
                FirebaseApp.getInstance().delete();
            } catch (IllegalStateException e) {
                Log.e(TAG, "initCrashlytics: ", e);
            }
        }
    }

    public void setAnalyticEnable(boolean isEnable) {
        isAnalyticEnable = isEnable;
        initCrashlytics();
    }

    public boolean isAnalyticEnable() {
        return isAnalyticEnable;
    }

    public static App getApp() {
        return sApp;
    }

    public static String getLocale() {
        return Locale.getDefault().getLanguage();
    }

    public static Boolean isDesktopMode() {
        return isDesktop;
    }

    public AppComponent getAppComponent() {
        return mAppComponent;
    }
}
