package app.editors.manager.app

import android.app.ActivityManager
import android.app.Application
import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.os.Process
import android.webkit.WebView
import app.documents.core.di.module.AccountModule
import app.documents.core.di.module.LoginModule
import app.documents.core.di.module.RecentModule
import app.documents.core.di.module.WebDavApiModule
import app.documents.core.share.ShareService
import app.documents.core.webdav.WebDavApi
import app.editors.manager.BuildConfig
import app.editors.manager.di.component.*
import app.editors.manager.di.module.*
import app.editors.manager.onedrive.di.component.DaggerOneDriveComponent
import app.editors.manager.onedrive.di.component.OneDriveComponent
import com.google.firebase.FirebaseApp
import com.google.firebase.crashlytics.FirebaseCrashlytics
import java.util.*

class App : Application() {

    companion object {

        val TAG: String = App::class.java.simpleName

        private lateinit var sApp: App
        private var currentDesktopMode = false
        private var isDesktop = false

        @JvmStatic
        fun getApp(): App {
            return sApp
        }

        @JvmStatic
        fun getLocale(): String {
            return Locale.getDefault().language
        }

        @JvmStatic
        fun isDesktopMode(): Boolean {
            return isDesktop
        }

    }

    var isAnalyticEnable = true
        set(value) {
            field = value
            initCrashlytics()
        }

    private lateinit var _appComponent: AppComponent

    val appComponent: AppComponent
        get() = _appComponent

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        sApp = this
        initDagger()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        currentDesktopMode = checkDeXEnabled()
        if (isDesktop != currentDesktopMode) {
            isDesktop = currentDesktopMode
        }
    }

    fun checkDeXEnabled(): Boolean {
        val enabled: Boolean
        val config: Configuration = resources.configuration
        try {
            val configClass: Class<*> = config.javaClass
            enabled = (configClass.getField("SEM_DESKTOP_MODE_ENABLED").getInt(configClass)
                    == configClass.getField("semDesktopModeEnabled").getInt(config))
            return enabled
        } catch (ignored: NoSuchFieldException) {
        } catch (ignored: IllegalAccessException) {
        } catch (ignored: IllegalArgumentException) {
        }
        return false
    }

    override fun onCreate() {
        super.onCreate()
        init()
    }

    private fun init() {

        /*
         Only android >= pie.
         https://bugs.chromium.org/p/chromium/issues/detail?id=558377
         https://stackoverflow.com/questions/51843546/android-pie-9-0-webview-in-multi-process

         For Android Pie is a separate directory for the process with WebView
         */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            if (getProcess() == "com.onlyoffice.documents:WebViewerActivity") {
                WebView.setDataDirectorySuffix("cacheWebView")
            }
        }
        isAnalyticEnable = _appComponent.preference.isAnalyticEnable
        initCrashlytics()
    }

    private fun getProcess(): String {
        val manager = this.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        for (info in manager.runningAppProcesses) {
            if (info.pid == Process.myPid()) {
                return info.processName
            }
        }
        return ""
    }

    private fun initDagger() {
        _appComponent = DaggerAppComponent.builder()
            .appModule(AppModule(this))
            .accountModule(AccountModule(RoomCallback()))
            .toolModule(ToolModule())
            .recentModule(RecentModule())
            .build()
    }

    private fun initCrashlytics() {
        FirebaseApp.initializeApp(this)
        if (BuildConfig.DEBUG || !isAnalyticEnable) {
            FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(false)
        }
    }

    fun getApi(token: String): Api {
        return DaggerApiComponent.builder()
            .appComponent(_appComponent)
            .apiModule(ApiModule(token))
            .build()
            .getApi()
    }

    val loginComponent: LoginComponent
        get() = DaggerLoginComponent.builder().appComponent(_appComponent)
            .loginModule(LoginModule())
            .build()


    fun getShareService(token: String): ShareService {
        return DaggerShareComponent.builder().appComponent(_appComponent)
            .shareModule(ShareModule(token))
            .build()
            .shareService
    }

    fun getWebDavApi(login: String?, password: String?): WebDavApi {
        return DaggerWebDavComponent.builder().appComponent(_appComponent)
            .webDavApiModule(WebDavApiModule(login, password))
            .build()
            .getWebDavApi()
    }

    fun getOneDriveComponent(token: String): OneDriveComponent {
        return DaggerOneDriveComponent.builder().appComponent(_appComponent)
            .oneDriveModule(OneDriveModule(token))
            .build()
    }
}