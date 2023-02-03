package app.editors.manager.app

import android.app.ActivityManager
import android.app.Application
import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.os.Process
import android.webkit.WebView
import androidx.appcompat.app.AppCompatDelegate
import androidx.room.InvalidationTracker
import app.documents.core.account.CloudAccount
import app.documents.core.login.ILoginServiceProvider
import app.documents.core.share.ShareService
import app.documents.core.webdav.WebDavApi
import app.editors.manager.BuildConfig
import app.editors.manager.di.component.*
import app.editors.manager.managers.utils.KeyStoreUtils
import app.editors.manager.storages.dropbox.di.component.DaggerDropboxComponent
import app.editors.manager.storages.dropbox.dropbox.api.IDropboxServiceProvider
import app.editors.manager.storages.dropbox.dropbox.login.IDropboxLoginServiceProvider
import app.editors.manager.storages.googledrive.di.component.DaggerGoogleDriveComponent
import app.editors.manager.storages.googledrive.googledrive.api.IGoogleDriveServiceProvider
import app.editors.manager.storages.googledrive.googledrive.login.IGoogleDriveLoginServiceProvider
import app.editors.manager.storages.onedrive.di.component.DaggerOneDriveComponent
import app.editors.manager.storages.onedrive.onedrive.api.IOneDriveServiceProvider
import app.editors.manager.storages.onedrive.onedrive.login.IOneDriveLoginServiceProvider
import com.google.firebase.FirebaseApp
import com.google.firebase.crashlytics.FirebaseCrashlytics
import lib.toolkit.base.managers.tools.ThemePreferencesTools
import lib.toolkit.base.managers.utils.ActivitiesUtils
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

    }

    var isAnalyticEnable = true
        set(value) {
            field = value
            initCrashlytics()
        }

    var isKeyStore: Boolean = true

    private var _appComponent: AppComponent? = null
    val appComponent: AppComponent
        get() = checkNotNull(_appComponent) {
            "App component can't be null"
        }

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
        ThemePreferencesTools(this).also { pref ->
            AppCompatDelegate.setDefaultNightMode(pref.mode)
        }
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
        if (ActivitiesUtils.isPackageExist(this, "com.onlyoffice.projects")) {
            AddAccountHelper(this).copyData()
        }
        isAnalyticEnable = appComponent.preference.isAnalyticEnable
        initCrashlytics()
        KeyStoreUtils.init()
        addDataBaseObserver()
    }

    private fun addDataBaseObserver() {
        appComponent.accountsDataBase.invalidationTracker.addObserver(object :
            InvalidationTracker.Observer(arrayOf(CloudAccount::class.java.simpleName)) {
            override fun onInvalidated(tables: Set<String>) {
                appComponent.preference.dbTimestamp = System.currentTimeMillis()
            }
        })
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
            .context(context = this)
            .build()
    }

    private fun initCrashlytics() {
        FirebaseApp.initializeApp(this)
        if (BuildConfig.DEBUG || !isAnalyticEnable) {
            FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(false)
        }
    }

    fun getApi(): ApiComponent {
        return DaggerApiComponent.builder()
            .appComponent(appComponent)
            .build()
    }

    fun getShareService(): ShareService {
        return DaggerShareComponent.builder().appComponent(appComponent)
            .build()
            .shareService
    }

    fun getWebDavApi(): WebDavApi {
        return DaggerWebDavComponent.builder().appComponent(appComponent)
            .build()
            .webDavApi
    }

    fun getOneDriveComponent(): IOneDriveServiceProvider {
        return DaggerOneDriveComponent.builder().appComponent(appComponent)
            .build()
            .oneDriveServiceProvider
    }

    fun getDropboxComponent(): IDropboxServiceProvider {
        return DaggerDropboxComponent.builder().appComponent(appComponent)
            .build()
            .dropboxServiceProvider
    }

    fun getGoogleDriveComponent(token: String = ""): IGoogleDriveServiceProvider {
        return DaggerGoogleDriveComponent.builder().appComponent(appComponent)
            .token(token)
            .build()
            .googleDriveServiceProvider
    }
}

val Context.accountOnline: CloudAccount?
    get() = when (this) {
        is App -> this.appComponent.accountOnline
        else -> this.applicationContext.appComponent.accountOnline
    }


val Context.appComponent: AppComponent
    get() = when (this) {
        is App -> this.appComponent
        else -> this.applicationContext.appComponent
    }

val Context.loginService: ILoginServiceProvider
    get() = when (this) {
        is App -> this.appComponent.loginService
        else -> this.applicationContext.appComponent.loginService
    }

val Context.oneDriveLoginService: IOneDriveLoginServiceProvider
    get() = when (this) {
        is App -> this.appComponent.oneDriveLoginService
        else -> applicationContext.appComponent.oneDriveLoginService
    }

val Context.dropboxLoginService: IDropboxLoginServiceProvider
    get() = when (this) {
        is App -> this.appComponent.dropboxLoginService
        else -> applicationContext.appComponent.dropboxLoginService
    }

val Context.googleDriveLoginService: IGoogleDriveLoginServiceProvider
    get() = when(this) {
        is App -> this.appComponent.googleDriveLoginService
        else -> applicationContext.appComponent.googleDriveLoginService
    }

fun Context.api(): Api {
    return when (this) {
        is App -> this.getApi().api
        else -> this.applicationContext.api()
    }
}

val Context.roomApi: RoomApi
    get() = when (this) {
        is App -> this.getApi().roomApi
        else -> this.applicationContext.roomApi
    }

fun Context.webDavApi(): WebDavApi {
    return when (this) {
        is App -> this.getWebDavApi()
        else -> this.applicationContext.webDavApi()
    }
}

fun Context.getShareApi(): ShareService {
    return when (this) {
        is App -> this.getShareService()
        else -> this.applicationContext.getShareApi()
    }
}

fun Context.getOneDriveServiceProvider(): IOneDriveServiceProvider {
    return when (this) {
        is App -> this.getOneDriveComponent()
        else -> this.applicationContext.getOneDriveServiceProvider()
    }
}

fun Context.getDropboxServiceProvider(): IDropboxServiceProvider {
    return when(this) {
        is App -> this.getDropboxComponent()
        else -> this.applicationContext.getDropboxServiceProvider()
    }
}
fun Context.getGoogleDriveServiceProvider(token: String = ""): IGoogleDriveServiceProvider {
    return when(this) {
        is App -> this.getGoogleDriveComponent(token)
        else -> this.applicationContext.getGoogleDriveServiceProvider(token)
    }
}