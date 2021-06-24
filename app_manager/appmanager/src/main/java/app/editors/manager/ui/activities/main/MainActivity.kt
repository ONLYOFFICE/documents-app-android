package app.editors.manager.ui.activities.main

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import androidx.annotation.StringRes
import androidx.work.WorkManager
import app.documents.core.account.CloudAccount
import app.documents.core.webdav.WebDavApi
import app.editors.manager.R
import app.editors.manager.databinding.ActivityMainBinding
import app.editors.manager.managers.receivers.DownloadReceiver
import app.editors.manager.managers.receivers.UploadReceiver
import app.editors.manager.mvp.presenters.main.MainActivityPresenter
import app.editors.manager.mvp.presenters.main.MainActivityState
import app.editors.manager.mvp.views.main.MainActivityView
import app.editors.manager.ui.activities.base.BaseAppActivity
import app.editors.manager.ui.dialogs.AccountBottomDialog
import app.editors.manager.ui.fragments.main.*
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.tabs.TabLayout
import com.google.android.play.core.review.ReviewInfo
import com.google.android.play.core.review.ReviewManagerFactory
import com.google.android.play.core.tasks.Task
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import lib.toolkit.base.managers.utils.FragmentUtils
import lib.toolkit.base.managers.utils.PermissionUtils
import lib.toolkit.base.managers.utils.UiUtils
import lib.toolkit.base.ui.dialogs.base.BaseBottomDialog
import lib.toolkit.base.ui.dialogs.common.CommonDialog
import lib.toolkit.base.ui.views.animation.collapse
import lib.toolkit.base.ui.views.animation.expand
import moxy.presenter.InjectPresenter
import java.util.*


interface ActionButtonFragment {
    fun showActionDialog()
}

interface IMainActivity {
    fun showNavigationButton(isShow: Boolean)
    fun showActionButton(isShow: Boolean)
    fun showAccount(isShow: Boolean)
    fun getTabLayout(): TabLayout
    fun setAppBarStates(isVisible: Boolean)
    fun getNavigationBottom(): BottomNavigationView
}


class MainActivity : BaseAppActivity(), MainActivityView, BottomNavigationView.OnNavigationItemSelectedListener,
    BaseBottomDialog.OnBottomDialogCloseListener, CommonDialog.OnCommonDialogClose, IMainActivity {

    companion object {

        val TAG: String = MainActivity::class.java.simpleName

        private const val ACCOUNT_KEY = "ACCOUNT_KEY"

        @JvmStatic
        fun show(context: Context) {
            context.startActivity(Intent(context, MainActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            })
        }

        @JvmStatic
        fun getIntent(context: Context): Intent {
            return Intent(context, MainActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            }
        }
    }

    @InjectPresenter
    lateinit var presenter: MainActivityPresenter

    private lateinit var viewBinding: ActivityMainBinding

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_READ_STORAGE) {
            if (grantResults.size > 1) {
                if (grantResults[0] == PackageManager.PERMISSION_DENIED && grantResults[1] == PackageManager.PERMISSION_DENIED) {
                    viewBinding.bottomNavigation.selectedItemId = R.id.menu_item_cloud
                } else {
                    showOnDeviceFragment()
                }
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString(ACCOUNT_KEY, Json.encodeToString(viewBinding.appBarToolbar.account))
        super.onSaveInstanceState(outState)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        intent?.action?.let { action ->
            if (action == Intent.ACTION_VIEW) {
                intent.data?.let {
                    presenter.checkFileData(it)
                }
            }
            if (action == DownloadReceiver.DOWNLOAD_ACTION_CANCELED) {
                intent.extras?.let { extras ->
                    WorkManager.getInstance()
                        .cancelWorkById(UUID.fromString(extras.getString(DownloadReceiver.EXTRAS_KEY_ID)))
                }
                return
            }
            if (action == UploadReceiver.UPLOAD_ACTION_CANCELED) {
                intent.extras?.let { extras ->
                    WorkManager.getInstance()
                        .cancelWorkById(UUID.fromString(extras.getString(UploadReceiver.EXTRAS_KEY_ID)))
                }
                return
            }
        }

        var fragment = supportFragmentManager.findFragmentByTag(MainPagerFragment.TAG)
        if (fragment is MainPagerFragment) {
            val fragments = fragment.getChildFragmentManager().fragments
            for (fr in fragments) {
                if (fr is DocsMyFragment) {
                    fr.getArgs(intent)
                }
            }
        }

        fragment = supportFragmentManager.findFragmentByTag(DocsWebDavFragment.TAG)
        if (fragment is DocsWebDavFragment) {
            fragment.getArgs(intent)
        }

        fragment = supportFragmentManager.findFragmentByTag(DocsOnDeviceFragment.TAG)
        if (fragment is DocsOnDeviceFragment) {
            fragment.getArgs(intent)
        }

        fragment = supportFragmentManager.findFragmentByTag(DocsRecentFragment.TAG)
        if (fragment is DocsRecentFragment) {
            fragment.getArgs(intent)
        }

        intent?.apply {
            data = null
            clipData = null
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_CANCELED) {
            when (requestCode) {
                REQUEST_ACTIVITY_WEB_VIEWER -> {
                    presenter.getRemoteConfigRate()
                    if (data != null && data.hasExtra(WebViewerActivity.TAG_VIEWER_FAIL)) {
                        showSnackBar("BAD bad viewer activity... :(")
                    }
                }
                REQUEST_ACTIVITY_PORTAL -> {
                    presenter.init()
                }
            }
            if (data != null && data.extras != null) {
                if (data.extras?.containsKey("fragment_error") == true) {
                    val dialog = getInfoDialog(
                        getString(R.string.app_internal_error),
                        getString(R.string.app_fragment_crash_error),
                        getString(R.string.dialogs_common_ok_button),
                        null
                    )
                    dialog?.show()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)
        init(savedInstanceState)
    }

    private fun init(savedInstanceState: Bundle?) {
        initViews()
        initToolbar()
        setAppBarStates()
        checkState(savedInstanceState)
    }

    private fun initViews() {
        UiUtils.removePaddingFromNavigationItem(viewBinding.bottomNavigation)
        viewBinding.appFloatingActionButton.visibility = View.GONE
        viewBinding.appFloatingActionButton.setOnClickListener { onFloatingButtonClick() }
    }

    private fun initToolbar() {
        setSupportActionBar(viewBinding.appBarToolbar.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(true)
        viewBinding.appBarToolbar.toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
        viewBinding.appBarToolbar.accountListener = {
            AccountBottomDialog.newInstance().show(supportFragmentManager, AccountBottomDialog.TAG)
        }
    }

    private fun checkState(savedInstanceState: Bundle?) {
        savedInstanceState?.let {
            viewBinding.appBarToolbar.bind(Json.decodeFromString(it.getString(ACCOUNT_KEY) ?: ""))
        } ?: run {
            presenter.init()
            viewBinding.bottomNavigation.selectedItemId = R.id.menu_item_cloud
        }
        viewBinding.bottomNavigation.setOnNavigationItemSelectedListener(this)
        presenter.checkOnBoarding()
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
    }

    override fun getTabLayout(): TabLayout {
        return viewBinding.appBarTabs
    }

    override fun onRender(state: MainActivityState) {
        when (state) {
            is MainActivityState.RecentState -> {
                showRecentFragment()
            }
            is MainActivityState.OnDeviceState -> {
                checkPermission()
            }
            is MainActivityState.CloudState -> {
                state.account?.let {
                    showOnCloudFragment(state.account)
                } ?: run {
                    showOnCloudFragment()
                }
                viewBinding.appBarToolbar.bind(state.account)
            }
            is MainActivityState.AccountsState -> {
                if (state.isAccounts) {
                    showAccountFragment(true)
                } else {
                    showAccountFragment(false)
                }
            }
        }
    }

    override fun openFile(account: CloudAccount, fileData: String) {
        showCloudFragment(account = account, fileData = fileData)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        return if (presenter.isDialogOpen) {
            false
        } else {
            presenter.navigationItemClick(item.itemId)
            true
        }
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

    override fun onUnauthorized(message: String?) {
        message?.let { showSnackBar(it) }
        setAppBarStates(false)
        showNavigationButton(false)
        presenter.clear()
        viewBinding.bottomNavigation.selectedItemId = R.id.menu_item_cloud
    }

    override fun onQuestionDialog(title: String, tag: String, accept: String, cancel: String, question: String?) {
        showQuestionDialog(title, tag, accept, cancel, question)
    }

    override fun onShowEditMultilineDialog(title: String, hint: String, accept: String, cancel: String, tag: String) {
        showEditMultilineDialog(title, hint, accept, cancel, tag)
    }

    override fun onShowPlayMarket(releaseId: String) {
        showPlayMarket(releaseId)
    }

    override fun onShowInAppReview(reviewInfo: ReviewInfo) {
        ReviewManagerFactory.create(this).launchReviewFlow(this, reviewInfo)
            .addOnCompleteListener { task: Task<Void?> ->
                if (task.isSuccessful) {
                    Log.d(TAG, "onShowInAppReview: success")
                } else {
                    Log.d(TAG, "onShowInAppReview: error")
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

    override fun getNavigationBottom(): BottomNavigationView = viewBinding.bottomNavigation

    private fun checkPermission() {
        if (PermissionUtils.requestReadWritePermission(this, PERMISSION_READ_STORAGE)) {
            showOnDeviceFragment()
        }
    }

    private fun showOnDeviceFragment() {
        supportFragmentManager.findFragmentByTag(DocsOnDeviceFragment.TAG)?.let {
            FragmentUtils.showFragment(supportFragmentManager, it, R.id.frame_container)
        } ?: run {
            FragmentUtils.showFragment(supportFragmentManager, DocsOnDeviceFragment.newInstance(), R.id.frame_container)
        }
    }

    private fun showRecentFragment() {
        supportFragmentManager.findFragmentByTag(DocsRecentFragment.TAG)?.let {
            FragmentUtils.showFragment(supportFragmentManager, it, R.id.frame_container)
        } ?: run {
            FragmentUtils.showFragment(supportFragmentManager, DocsRecentFragment.newInstance(), R.id.frame_container)
        }
    }

    private fun showOnCloudFragment(account: CloudAccount? = null) {
        account?.let {
            if (it.isWebDav) {
                showWebDavFragment(it)
            } else {
                showCloudFragment(account)
            }
        } ?: run {
            FragmentUtils.showFragment(
                supportFragmentManager,
                OnlyOfficeCloudFragment.newInstance(false),
                R.id.frame_container
            )
        }
    }

    private fun showCloudFragment(account: CloudAccount?, fileData: String? = null) {
        supportFragmentManager.findFragmentByTag(MainPagerFragment.TAG)?.let { fragment ->
            (fragment as MainPagerFragment).let { pagerFragment ->
                fileData?.let {
                    pagerFragment.setFileData(it)
                } ?: run {
                    if (!pagerFragment.isRoot()) {
                        supportFragmentManager.beginTransaction().remove(fragment).commit()
                        FragmentUtils.showFragment(
                            supportFragmentManager,
                            MainPagerFragment.newInstance(Json.encodeToString(account), fileData),
                            R.id.frame_container
                        )
                    }
                }
            }
        } ?: run {
            FragmentUtils.showFragment(
                supportFragmentManager,
                MainPagerFragment.newInstance(Json.encodeToString(account), fileData),
                R.id.frame_container
            )
        }
    }

    private fun showWebDavFragment(account: CloudAccount) {
        supportFragmentManager.findFragmentByTag(DocsWebDavFragment.TAG)?.let {
            FragmentUtils.showFragment(supportFragmentManager, it, R.id.frame_container)
        } ?: run {
            FragmentUtils.showFragment(
                supportFragmentManager,
                DocsWebDavFragment.newInstance(WebDavApi.Providers.valueOf(account.webDavProvider ?: "")),
                R.id.frame_container
            )
        }
    }

    private fun showAccountFragment(isAccounts: Boolean) {
        if (isAccounts) {
            FragmentUtils.showFragment(
                supportFragmentManager,
                CloudAccountFragment.newInstance(),
                R.id.frame_container
            )

        } else {
            FragmentUtils.showFragment(
                supportFragmentManager,
                OnlyOfficeCloudFragment.newInstance(true),
                R.id.frame_container
            )
        }
    }

    override fun setAppBarStates(isVisible: Boolean) {
        setAppBarMode(isVisible)
        showAccount(isVisible)
        showNavigationButton(!isVisible)
        if (isVisible) {
            viewBinding.appBarTabs.expand(50)
        } else {
            viewBinding.appBarTabs.collapse()
        }
    }

}