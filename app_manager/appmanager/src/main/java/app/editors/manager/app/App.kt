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
import app.documents.core.di.dagger.CoreComponent
import app.documents.core.di.dagger.DaggerCoreComponent
import app.documents.core.network.login.ILoginServiceProvider
import app.documents.core.network.manager.ManagerService
import app.documents.core.network.room.RoomService
import app.documents.core.network.share.ShareService
import app.documents.core.network.storages.dropbox.api.DropboxProvider
import app.documents.core.network.storages.dropbox.login.DropboxLoginProvider
import app.documents.core.network.storages.googledrive.api.GoogleDriveProvider
import app.documents.core.network.storages.googledrive.login.GoogleDriveLoginProvider
import app.documents.core.network.storages.onedrive.api.OneDriveProvider
import app.documents.core.network.storages.onedrive.login.OneDriveLoginProvider
import app.documents.core.network.webdav.WebDavService
import app.documents.core.providers.CloudFileProvider
import app.documents.core.providers.LocalFileProvider
import app.documents.core.providers.RoomProvider
import app.documents.core.providers.WebDavFileProvider
import app.documents.core.storage.account.CloudAccount
import app.editors.manager.BuildConfig
import app.editors.manager.di.component.AppComponent
import app.editors.manager.di.component.DaggerAppComponent
import app.editors.manager.di.component.DaggerDropboxComponent
import app.editors.manager.di.component.DaggerGoogleDriveComponent
import app.editors.manager.di.component.DaggerOneDriveComponent
import app.editors.manager.di.component.DropboxComponent
import app.editors.manager.di.component.GoogleDriveComponent
import app.editors.manager.di.component.OneDriveComponent
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

    val coreComponent: CoreComponent by lazy  {
        DaggerCoreComponent.builder()
            .context(this)
            .build()
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
            .coreComponent(coreComponent)
            .build()

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

val Context.coreComponent: CoreComponent
    get() = when (this) {
        is App -> this.coreComponent
        else -> this.applicationContext.coreComponent
    }

val Context.loginService: ILoginServiceProvider
    get() = when (this) {
        is App -> this.coreComponent.loginService
        else -> this.applicationContext.loginService
    }

val Context.oneDriveLoginProvider: OneDriveLoginProvider
    get() = when (this) {
        is App -> oneDriveComponent.oneDriveLoginProvider
        else -> applicationContext.oneDriveLoginProvider
    }

val Context.oneDriveProvider: OneDriveProvider
    get() = when (this) {
        is App -> oneDriveComponent.oneDriveProvider
        else -> applicationContext.oneDriveProvider
    }

val Context.dropboxLoginProvider: DropboxLoginProvider
    get() = when (this) {
        is App -> dropboxComponent.dropboxLoginProvider
        else -> applicationContext.dropboxLoginProvider
    }

val Context.dropboxProvider: DropboxProvider
    get() = when (this) {
        is App -> dropboxComponent.dropboxProvider
        else -> applicationContext.dropboxProvider
    }

val Context.googleDriveLoginProvider: GoogleDriveLoginProvider
    get() = when (this) {
        is App -> googleDriveComponent.googleDriveLoginProvider
        else -> applicationContext.googleDriveLoginProvider
    }

val Context.googleDriveProvider: GoogleDriveProvider
    get() = when (this) {
        is App -> googleDriveComponent.googleDriveProvider
        else -> applicationContext.googleDriveProvider
    }

val Context.api: ManagerService
    get() = when (this) {
        is App -> coreComponent.managerService
        else -> applicationContext.api
    }

val Context.roomApi: RoomService
    get() = when (this) {
        is App -> coreComponent.roomService
        else -> applicationContext.roomApi
    }

val Context.webDavApi: WebDavService
    get() = when (this) {
        is App -> coreComponent.webDavService
        else -> applicationContext.webDavApi
    }

val Context.shareApi: ShareService
    get() = when (this) {
        is App -> coreComponent.shareService
        else -> applicationContext.shareApi
    }

val Context.cloudFileProvider: CloudFileProvider
    get() = when (this) {
        is App -> coreComponent.cloudFileProvider
        else -> applicationContext.cloudFileProvider
    }

val Context.localFileProvider: LocalFileProvider
    get() = when (this) {
        is App -> coreComponent.localFileProvider
        else -> applicationContext.localFileProvider
    }

val Context.webDavFileProvider: WebDavFileProvider
    get() = when (this) {
        is App -> coreComponent.webDavFileProvider
        else -> applicationContext.webDavFileProvider
    }

val Context.roomProvider: RoomProvider
    get() = when (this) {
        is App -> coreComponent.roomProvider
        else -> applicationContext.roomProvider
    }