package app.editors.manager.app

import android.app.ActivityManager
import android.app.Application
import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.os.Process
import android.webkit.WebView
import androidx.appcompat.app.AppCompatDelegate
import app.documents.core.login.LoginComponent
import app.documents.core.model.cloud.CloudAccount
import app.documents.core.model.cloud.CloudPortal
import app.documents.core.network.manager.ManagerService
import app.documents.core.network.room.RoomService
import app.documents.core.network.share.ShareService
import app.documents.core.network.storages.dropbox.api.DropboxProvider
import app.documents.core.network.storages.googledrive.api.GoogleDriveProvider
import app.documents.core.network.storages.onedrive.api.OneDriveProvider
import app.documents.core.network.webdav.WebDavService
import app.documents.core.providers.CloudFileProvider
import app.documents.core.providers.LocalFileProvider
import app.documents.core.providers.RoomProvider
import app.documents.core.providers.WebDavFileProvider
import app.editors.manager.BuildConfig
import app.editors.manager.di.component.AppComponent
import app.editors.manager.di.component.DaggerAppComponent
import app.editors.manager.di.component.DaggerDropboxComponent
import app.editors.manager.di.component.DaggerGoogleDriveComponent
import app.editors.manager.di.component.DaggerOneDriveComponent
import app.editors.manager.di.component.DropboxComponent
import app.editors.manager.di.component.GoogleDriveComponent
import app.editors.manager.di.component.OneDriveComponent
import app.editors.manager.managers.utils.GoogleUtils
import app.editors.manager.managers.utils.KeyStoreUtils
import com.google.firebase.FirebaseApp
import com.google.firebase.crashlytics.FirebaseCrashlytics
import lib.toolkit.base.managers.tools.ThemePreferencesTools
import lib.toolkit.base.managers.utils.ActivitiesUtils
import java.util.Locale

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

    var needPasscodeToUnlock: Boolean = false

    var showPersonalPortalMigration: Boolean = true

    var isAnalyticEnable = true
        set(value) {
            field = value
            initCrashlytics()
        }

    private var _appComponent: AppComponent? = null
    val appComponent: AppComponent
        get() = checkNotNull(_appComponent) {
            "App component can't be null"
        }

    private var _dropboxComponent: DropboxComponent? = null
    val dropboxComponent: DropboxComponent
        get() = checkNotNull(_dropboxComponent) {
            "Dropbox component can't be null"
        }

    private var _googleDriveComponent: GoogleDriveComponent? = null
    val googleDriveComponent: GoogleDriveComponent
        get() = checkNotNull(_googleDriveComponent) {
            "GoogleDrive component can't be null"
        }

    private var _oneDriveComponent: OneDriveComponent? = null
    val oneDriveComponent: OneDriveComponent
        get() = checkNotNull(_oneDriveComponent) {
            "OneDrive component can't be null"
        }

    private var _loginComponent: LoginComponent? = null
    val loginComponent: LoginComponent
        get() = checkNotNull(_loginComponent) {
            "LoginComponent component can't be null"
        }

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        sApp = this
        initDagger(base)
        accountsMigrate()
        needPasscodeToUnlock = appComponent.preference.passcodeLock.enabled
    }

    private fun accountsMigrate() {
        refreshLoginComponent(null)
        if (ActivitiesUtils.isPackageExist(this, "com.onlyoffice.projects")) {
            appComponent.accountHelper.copyData()
        }
        appComponent.migrationHelper.migrate()
        _loginComponent = null
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        currentDesktopMode = checkDeXEnabled()
        if (isDesktop != currentDesktopMode) {
            isDesktop = currentDesktopMode
        }
    }

    fun refreshDropboxInstance() {
        _dropboxComponent = DaggerDropboxComponent
            .builder()
            .appComponent(appComponent)
            .build()
    }

    fun refreshGoogleDriveInstance() {
        _googleDriveComponent = DaggerGoogleDriveComponent
            .builder()
            .appComponent(appComponent)
            .build()
    }

    fun refreshOneDriveInstance() {
        _oneDriveComponent = DaggerOneDriveComponent
            .builder()
            .appComponent(appComponent)
            .build()
    }

    fun refreshLoginComponent(portal: CloudPortal?) {
        _loginComponent = appComponent
            .loginComponent()
            .create(portal, GoogleUtils.isGooglePlayServicesAvailable(this))
    }

    fun refreshAppComponent(context: Context) {
        _appComponent = DaggerAppComponent.builder()
            .context(context)
            .build()
    }

    private fun checkDeXEnabled(): Boolean {
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
        isAnalyticEnable = appComponent.preference.isAnalyticEnable
        initCrashlytics()
        KeyStoreUtils.init()
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

    private fun initDagger(context: Context) {
        refreshAppComponent(context)
        refreshDropboxInstance()
        refreshGoogleDriveInstance()
        refreshOneDriveInstance()
    }

    private fun initCrashlytics() {
        FirebaseApp.initializeApp(this)
        if (BuildConfig.DEBUG || !isAnalyticEnable) {
            FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(false)
        }
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

val Context.oneDriveProvider: OneDriveProvider
    get() = when (this) {
        is App -> oneDriveComponent.oneDriveProvider
        else -> applicationContext.oneDriveProvider
    }

val Context.dropboxProvider: DropboxProvider
    get() = when (this) {
        is App -> dropboxComponent.dropboxProvider
        else -> applicationContext.dropboxProvider
    }

val Context.googleDriveProvider: GoogleDriveProvider
    get() = when (this) {
        is App -> googleDriveComponent.googleDriveProvider
        else -> applicationContext.googleDriveProvider
    }

val Context.api: ManagerService
    get() = when (this) {
        is App -> appComponent.managerService
        else -> applicationContext.api
    }

val Context.roomApi: RoomService
    get() = when (this) {
        is App -> appComponent.roomService
        else -> applicationContext.roomApi
    }

val Context.webDavApi: WebDavService
    get() = when (this) {
        is App -> appComponent.webDavService
        else -> applicationContext.webDavApi
    }

val Context.shareApi: ShareService
    get() = when (this) {
        is App -> appComponent.shareService
        else -> applicationContext.shareApi
    }

val Context.cloudFileProvider: CloudFileProvider
    get() = when (this) {
        is App -> appComponent.cloudFileProvider
        else -> applicationContext.cloudFileProvider
    }

val Context.localFileProvider: LocalFileProvider
    get() = when (this) {
        is App -> appComponent.localFileProvider
        else -> applicationContext.localFileProvider
    }

val Context.webDavFileProvider: WebDavFileProvider
    get() = when (this) {
        is App -> appComponent.webDavFileProvider
        else -> applicationContext.webDavFileProvider
    }

val Context.roomProvider: RoomProvider
    get() = when (this) {
        is App -> appComponent.roomProvider
        else -> applicationContext.roomProvider
    }