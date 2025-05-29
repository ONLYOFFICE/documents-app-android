package app.editors.manager.ui.activities.main

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.annotation.StringRes
import androidx.core.view.isVisible
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.work.WorkManager
import app.documents.core.model.cloud.PortalProvider
import app.documents.core.model.cloud.WebdavProvider
import app.documents.core.network.manager.models.explorer.CloudFile
import app.editors.manager.R
import app.editors.manager.app.App
import app.editors.manager.app.accountOnline
import app.editors.manager.databinding.ActivityMainBinding
import app.editors.manager.managers.receivers.AppLocaleReceiver
import app.editors.manager.managers.receivers.DownloadReceiver
import app.editors.manager.managers.receivers.RoomDuplicateReceiver
import app.editors.manager.managers.receivers.UploadReceiver
import app.editors.manager.managers.utils.InAppUpdateUtils
import app.editors.manager.mvp.presenters.main.MainActivityPresenter
import app.editors.manager.mvp.presenters.main.MainPagerPresenter.Companion.PERSONAL_DUE_DATE
import app.editors.manager.mvp.views.main.MainActivityView
import app.editors.manager.ui.activities.base.BaseAppActivity
import app.editors.manager.ui.compose.personal.PersonalPortalMigrationFragment
import app.editors.manager.ui.dialogs.fragments.CloudAccountDialogFragment
import app.editors.manager.ui.fragments.main.CloudAccountFragment
import app.editors.manager.ui.fragments.main.DocsOnDeviceFragment
import app.editors.manager.ui.fragments.main.DocsRecentFragment
import app.editors.manager.ui.fragments.main.DocsWebDavFragment
import app.editors.manager.ui.fragments.main.MainPagerFragment
import app.editors.manager.ui.fragments.main.OnlyOfficeCloudFragment
import app.editors.manager.ui.fragments.main.settings.AppSettingsFragment
import app.editors.manager.ui.fragments.storages.DocsDropboxFragment
import app.editors.manager.ui.fragments.storages.DocsGoogleDriveFragment
import app.editors.manager.ui.fragments.storages.DocsOneDriveFragment
import com.google.android.gms.tasks.Task
import com.google.android.play.core.review.ReviewInfo
import com.google.android.play.core.review.ReviewManagerFactory
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import lib.toolkit.base.managers.utils.FragmentUtils
import lib.toolkit.base.managers.utils.LaunchActivityForResult
import lib.toolkit.base.managers.utils.RequestPermission
import lib.toolkit.base.managers.utils.TimeUtils
import lib.toolkit.base.managers.utils.contains
import lib.toolkit.base.ui.dialogs.base.BaseBottomDialog
import lib.toolkit.base.ui.dialogs.common.CommonDialog
import moxy.presenter.InjectPresenter
import java.util.UUID


interface ActionButtonFragment {
    fun showActionDialog()
}

interface IMainActivity {
    fun showNavigationButton(isShow: Boolean)
    fun showActionButton(isShow: Boolean)
    fun showAccount(isShow: Boolean)
    fun setAppBarStates(isVisible: Boolean)
    fun onSwitchAccount()
    fun showOnCloudFragment()
    fun showAccountsActivity()
    fun showWebViewer(file: CloudFile, isEditMode: Boolean = false, callback: (() -> Unit)? = null)
    fun onLogOut()
    fun showPersonalMigrationFragment()
    fun setToolbarInfo(
        title: String?,
        drawable: Int? = null,
        drawablePadding: Int? = null
    )
}

class MainActivity : BaseAppActivity(), MainActivityView, BaseBottomDialog.OnBottomDialogCloseListener,
    CommonDialog.OnCommonDialogClose, IMainActivity, View.OnClickListener {

    companion object {

        val TAG: String = MainActivity::class.java.simpleName

        private const val ACCOUNT_KEY = "ACCOUNT_KEY"
        private const val FRAGMENT_KEY = "FRAGMENT_KEY"
        private const val URL_KEY = "url"

        fun show(context: Context) {
            context.startActivity(Intent(context, MainActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            })
        }
    }

    @InjectPresenter
    lateinit var presenter: MainActivityPresenter

    private lateinit var viewBinding: ActivityMainBinding

    private val navigationListener: (item: MenuItem) -> Boolean = { item ->
        if (presenter.isDialogOpen) {
            false
        } else {
            navigate(item.itemId)
            true
        }
    }

    private val toolbarElevation by lazy { resources.getDimension(lib.toolkit.base.R.dimen.default_elevation_height) }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString(ACCOUNT_KEY, Json.encodeToString(viewBinding.appBarToolbar.account))
        outState.putInt(FRAGMENT_KEY, viewBinding.bottomNavigation.selectedItemId)
        super.onSaveInstanceState(outState)
    }

    @SuppressLint("MissingSuperCall")
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        intent.action?.let { action ->
            if (action == Intent.ACTION_VIEW) {
                intent.data?.let {
                    val fragment = supportFragmentManager.findFragmentByTag(MainPagerFragment.TAG)
                    if (fragment is MainPagerFragment && fragment.isVisible) {
                        setIntent(intent)
                        fragment.checkBundle()
                    } else {
                        setIntent(intent)
                        openFile()
                    }
                }
            }

            intent.extras?.let extras@{ extras ->
                val key = when (action) {
                    DownloadReceiver.DOWNLOAD_ACTION_CANCELED -> DownloadReceiver.EXTRAS_KEY_ID
                    UploadReceiver.UPLOAD_ACTION_CANCELED -> UploadReceiver.EXTRAS_KEY_ID
                    RoomDuplicateReceiver.ACTION_HIDE -> RoomDuplicateReceiver.KEY_NOTIFICATION_HIDE
                    else -> return@extras
                }
                WorkManager.getInstance(this).cancelWorkById(UUID.fromString(extras.getString(key)))
                return
            }
        }

        if (isNotification()) {
            intent.extras?.getString(URL_KEY)?.let {
                showBrowser(it)
            }
            return
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        InAppUpdateUtils.handleActivityResult(requestCode, resultCode, this)
        if (resultCode == RESULT_CANCELED) {
            when (requestCode) {
                REQUEST_ACTIVITY_PORTAL -> {
                    presenter.init(true)
                }
            }
            if (data != null && data.extras != null) {
                if (data.extras?.contains("fragment_error") == true) {
                    val dialog = getInfoDialog(
                        getString(R.string.app_internal_error),
                        getString(R.string.app_fragment_crash_error),
                        getString(R.string.dialogs_common_ok_button),
                        null
                    )
                    dialog?.show(supportFragmentManager)
                }
            }
        } else if (resultCode == AccountsActivity.RESULT_NO_LOGGED_IN_ACCOUNTS) {
            onLogOut()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        presenter.checkOnBoardingShowed()
        viewBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)
        init(savedInstanceState)
    }

    override fun onStart() {
        if (App.getApp().needPasscodeToUnlock) PasscodeActivity.show(this)
        super.onStart()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            unregisterReceiver(AppLocaleReceiver)
        }
    }

    private fun init(savedInstanceState: Bundle?) {
        initViews()
        initToolbar()
        registerAppLocaleBroadcastReceiver()

        if (isNotification()) {
            intent.extras?.getString(URL_KEY)?.let {
                showBrowser(it)
            }
        }

        App.getApp().appComponent.recentDataSource.getRecentListFlow().flowWithLifecycle(lifecycle)
            .onEach { viewBinding.bottomNavigation.menu.getItem(0).isEnabled = it.isNotEmpty() }
            .launchIn(lifecycleScope)

        checkState(savedInstanceState)

        lifecycleScope.launch {
            delay(3000)
            InAppUpdateUtils.checkForUpdate(this@MainActivity)
        }

    }

    private fun initViews() {
        viewBinding.appFloatingActionButton.visibility = View.GONE
        viewBinding.appFloatingActionButton.setOnClickListener { onFloatingButtonClick() }
        viewBinding.bottomNavigation.setOnItemSelectedListener(navigationListener)
    }

    private fun initToolbar() {
        setSupportActionBar(viewBinding.appBarToolbar.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(true)
        viewBinding.appBarToolbar.toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
        viewBinding.appBarToolbar.accountListener = {
            showAccountsActivity()
        }
    }

    private fun checkState(savedInstanceState: Bundle?) {
        presenter.init()

        if (savedInstanceState != null) {
            viewBinding.bottomNavigation.selectedItemId = savedInstanceState.getInt(FRAGMENT_KEY)
            viewBinding.appBarToolbar.bind()
            return
        }

        if (intent?.extras?.contains("create_type") == true) {
            viewBinding.bottomNavigation.selectedItemId = R.id.menu_item_on_device
        } else {
            accountOnline?.let {
                viewBinding.appBarToolbar.bind()
                viewBinding.bottomNavigation.selectedItemId = R.id.menu_item_cloud
            } ?: run {
                viewBinding.bottomNavigation.selectedItemId = R.id.menu_item_on_device
            }
        }

        checkNotification()
    }

    private fun checkNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            RequestPermission(activityResultRegistry, {
                // TODO set notification flag
            }, Manifest.permission.POST_NOTIFICATIONS).request()
        }
    }

    private fun setAppBarStates() {
        setAppBarMode(supportFragmentManager.backStackEntryCount <= 0)
    }

    private fun setAppBarMode(isScroll: Boolean) {
        if (isTablet) {
            setAppBarFix(viewBinding.appBarToolbar)
        } else {
            if (isScroll) {
                setAppBarScroll(viewBinding.appBarToolbar)
            } else {
                setAppBarFix(viewBinding.appBarToolbar)
            }
        }
    }

    private fun registerAppLocaleBroadcastReceiver() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(AppLocaleReceiver, IntentFilter(Intent.ACTION_LOCALE_CHANGED))
        }
    }

    private fun onFloatingButtonClick() {
        supportFragmentManager.fragments.forEach {
            if (it is ActionButtonFragment && it.isVisible) {
                it.showActionDialog()
                presenter.isDialogOpen = true
            }
        }
    }

    override fun showNavigationButton(isShow: Boolean) {
        supportActionBar?.setDisplayHomeAsUpEnabled(isShow)
    }

    override fun showAccount(isShow: Boolean) {
        //        presenter.isDialogOpen = true
        viewBinding.appBarToolbar.showAccount(isShow)
        viewBinding.appBarLayout.elevation = if (isShow) 0f else toolbarElevation
    }

    override fun onLocaleConfirmation() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            AppLocaleConfirmationActivity.show(this)
        }
    }

    fun openFile() {
        viewBinding.bottomNavigation.setOnItemSelectedListener(null)
        viewBinding.bottomNavigation.selectedItemId = R.id.menu_item_cloud
        showOnCloudFragment()
        viewBinding.bottomNavigation.setOnItemSelectedListener(navigationListener)
    }

    override fun showActionButton(isShow: Boolean) {
        viewBinding.appFloatingActionButton.visibility = if (isShow) View.VISIBLE else View.GONE
    }

    override fun onAcceptClick(dialogs: CommonDialog.Dialogs?, value: String?, tag: String?) {
        super.onAcceptClick(dialogs, value, tag)
        presenter.onAcceptClick(value, tag)
    }

    override fun onCancelClick(dialogs: CommonDialog.Dialogs?, tag: String?) {
        super.onCancelClick(dialogs, tag)
        presenter.onCancelClick(tag)
    }

    override fun onContextDialogOpen() {
        presenter.isDialogOpen = true
    }

    override fun onBottomDialogClose() {
        presenter.isDialogOpen = false
    }

    override fun onCommonClose() {
        presenter.isDialogOpen = false
    }

    override fun onDialogClose() {
        presenter.isDialogOpen = false
        hideDialog()
    }

    override fun onCloseActionDialog() {
        presenter.isDialogOpen = false
    }

    override fun onError(message: String?) {
        hideDialog()
        message?.let {
            if (message == getString(R.string.errors_client_host_not_found)) {
                onUnauthorized(message)
            } else {
                showSnackBar(message)
            }
        }
    }

    override fun showWebViewer(file: CloudFile, isEditMode: Boolean, callback: (() -> Unit)?) {
        LaunchActivityForResult(
            activityResultRegistry = activityResultRegistry,
            callback = { result ->
                presenter.getRemoteConfigRate()
                if (result.data != null && result.data?.hasExtra(WebViewerActivity.TAG_VIEWER_FAIL) == true) {
                    showSnackBar(getString(R.string.errors_web_viewr))
                }
                callback?.invoke()
            },
            intent = WebViewerActivity.getActivityIntent(this, file, isEditMode)
        ).show()
    }

    override fun onClick(view: View?) {
        showAccountsActivity()
    }

    override fun onUnauthorized(message: String?) {
        message?.let { showSnackBar(it) }
        setAppBarStates(false)
        showNavigationButton(false)
        presenter.clear()
        FragmentUtils.showFragment(
            supportFragmentManager,
            OnlyOfficeCloudFragment.newInstance(false),
            R.id.frame_container
        )
    }

    override fun onQuestionDialog(title: String, tag: String, accept: String, cancel: String, question: String?) {
        viewBinding.root.postDelayed({
            showQuestionDialog(title, tag, accept, cancel, question)
        }, 150)
    }

    override fun onShowEditMultilineDialog(title: String, hint: String, accept: String, cancel: String, tag: String) {
        viewBinding.root.postDelayed({
            showEditMultilineDialog(title, hint, accept, cancel, tag)
        }, 150)
    }

    override fun onShowPlayMarket(releaseId: String) {
        showPlayMarket(releaseId)
    }

    override fun onShowInAppReview(reviewInfo: ReviewInfo) {
        ReviewManagerFactory.create(this).launchReviewFlow(this, reviewInfo)
            .addOnCompleteListener { task: Task<Void?> ->
                if (task.isSuccessful) {
                    presenter.onRateOff()
                } else {
                    presenter.onRateOff()
                }
            }
    }

    override fun onShowApp(releaseId: String) {
        showApp(releaseId)
    }

    override fun onShowEmailClientTemplate(value: String) {
        showEmailClientTemplate(value)
    }

    override fun onShowOnBoarding() {
        OnBoardingActivity.show(this)
    }

    override fun onRemotePlayMarket(
        @StringRes title: Int,
        @StringRes info: Int,
        @StringRes accept: Int,
        @StringRes cancel: Int
    ) {
        showQuestionDialog(
            getString(title),
            MainActivityPresenter.TAG_DIALOG_REMOTE_PLAY_MARKET,
            getString(accept),
            getString(cancel),
            getString(info)
        )
    }

    override fun onRemoteApp(
        @StringRes title: Int,
        @StringRes info: Int,
        @StringRes accept: Int,
        @StringRes cancel: Int
    ) {
        showQuestionDialog(
            getString(title),
            MainActivityPresenter.TAG_DIALOG_REMOTE_APP,
            getString(accept),
            getString(cancel),
            getString(info)
        )
    }

    override fun onRatingApp() {
        showQuestionDialog(
            getString(R.string.dialogs_question_rate_first_info), MainActivityPresenter.TAG_DIALOG_RATE_FIRST,
            getString(R.string.dialogs_question_accept_yes),
            getString(R.string.dialogs_question_accept_not_really), null
        )
    }

    override fun onSwitchAccount() {
        FragmentUtils.showFragment(
            supportFragmentManager,
            CloudAccountFragment.newInstance(),
            R.id.frame_container
        )
        viewBinding.bottomNavigation.selectedItemId = R.id.menu_item_settings
    }

    private fun showOnDeviceFragment() {
        if (supportFragmentManager.findFragmentByTag(DocsOnDeviceFragment.TAG) != null) {
            return
        }
        hidePagerFragment()
        val fragment = DocsOnDeviceFragment.newInstance()
        supportFragmentManager.beginTransaction().apply {
            add(R.id.frame_container, fragment, DocsOnDeviceFragment.TAG)
            show(fragment)
        }.commit()
    }

    private fun showRecentFragment() {
        if (supportFragmentManager.findFragmentByTag(DocsRecentFragment.TAG) != null) {
            return
        }
        hidePagerFragment()
        val fragment = DocsRecentFragment.newInstance()
        supportFragmentManager.beginTransaction().apply {
            add(R.id.frame_container, fragment, DocsRecentFragment.TAG)
            show(fragment)
        }.commit()
    }


    private fun showSettingsFragment() {
        if (supportFragmentManager.findFragmentByTag(AppSettingsFragment.TAG) != null) {
            return
        }
        hidePagerFragment()
        val fragment = AppSettingsFragment.newInstance()
        supportFragmentManager.beginTransaction().apply {
            add(R.id.frame_container, fragment, AppSettingsFragment.TAG)
            show(fragment)
        }.commit()
    }

    override fun showOnCloudFragment() {
        if (accountOnline == null) {
            FragmentUtils.showFragment(
                supportFragmentManager,
                OnlyOfficeCloudFragment.newInstance(false),
                R.id.frame_container
            )
            return
        } else {
            val fragment = when (accountOnline?.portal?.provider) {
                PortalProvider.Dropbox -> DocsDropboxFragment.newInstance()
                PortalProvider.GoogleDrive -> DocsGoogleDriveFragment.newInstance()
                PortalProvider.Onedrive -> DocsOneDriveFragment.newInstance()
                is PortalProvider.Webdav -> DocsWebDavFragment.newInstance(WebdavProvider.valueOf(accountOnline?.portal?.provider!!))
                is PortalProvider.Cloud -> {
                    showActionButton(false)
                    setAppBarStates(true)
                    showMainPagerFragment()
                    return
                }

                else -> OnlyOfficeCloudFragment.newInstance(false)
            }
            FragmentUtils.showFragment(
                supportFragmentManager,
                fragment,
                R.id.frame_container
            )
        }
    }

    private fun showMainPagerFragment() {
        supportFragmentManager.beginTransaction().apply {
            supportFragmentManager.fragments.forEach {
                if (it !is MainPagerFragment) {
                    remove(it)
                }
            }
        }.commit()
        val fragment = supportFragmentManager.findFragmentByTag(MainPagerFragment.TAG)
        if (fragment != null) {
            supportFragmentManager.beginTransaction().setCustomAnimations(
                lib.toolkit.base.R.anim.fragment_fade_in,
                lib.toolkit.base.R.anim.fragment_fade_out
            ).show(fragment).commit()
            (fragment as MainPagerFragment).onResume()
            return
        }

        supportFragmentManager.beginTransaction().apply {
            val mainPagerFragment = MainPagerFragment.newInstance()
            add(R.id.frame_container, mainPagerFragment, MainPagerFragment.TAG)
            show(mainPagerFragment)
        }.commit()
    }

    override fun onLogOut() {
        showOnCloudFragment()
        setAppBarStates(false)
        showNavigationButton(false)
    }

    override fun showAccountsActivity() {
        if (!isTablet) {
            AccountsActivity.show(this)
        } else {
            CloudAccountDialogFragment.newInstance()
                .show(supportFragmentManager, CloudAccountDialogFragment.TAG)
        }
    }

    override fun setAppBarStates(isVisible: Boolean) {
        setToolbarInfo(null)
        setAppBarMode(isVisible)
        showAccount(isVisible)
        showNavigationButton(!isVisible)
    }

    override fun showPersonalMigrationFragment() {
        if (!TimeUtils.isDateAfter(PERSONAL_DUE_DATE)) {
            PersonalPortalMigrationFragment.newInstance().show(supportFragmentManager, "")
        }
    }

    override fun setToolbarInfo(
        title: String?,
        drawable: Int?,
        drawablePadding: Int?
    ) {
        viewBinding.infoLayout.root.isVisible = title != null
        viewBinding.infoLayout.infoText.text = title
        viewBinding.infoLayout.infoText.setCompoundDrawablesRelativeWithIntrinsicBounds(drawable ?: 0, 0, 0, 0)
        viewBinding.infoLayout.infoText.compoundDrawablePadding = drawablePadding ?: 4
    }

    private fun isNotification(): Boolean =
        intent?.categories?.contains(Intent.CATEGORY_LAUNCHER) == true && intent.extras?.contains(URL_KEY) == true

    private fun navigate(itemId: Int) {
        when (itemId) {
            R.id.menu_item_recent -> showRecentFragment()
            R.id.menu_item_on_device -> showOnDeviceFragment()
            R.id.menu_item_settings -> showSettingsFragment()
            R.id.menu_item_cloud -> {
                showOnCloudFragment()
            }
        }
    }

    private fun hidePagerFragment() {
        supportFragmentManager.beginTransaction().apply {
            supportFragmentManager.fragments.forEach {
                if (it !is MainPagerFragment) {
                    remove(it)
                } else {
                    hide(it)
                }
            }
        }.commit()
    }

}