package app.editors.manager.ui.activities.base

import android.os.Bundle
import android.os.Handler
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import app.editors.manager.R
import app.editors.manager.ui.interfaces.ContextDialogInterface
import lib.toolkit.base.managers.utils.ActivitiesUtils.showBrowser
import lib.toolkit.base.managers.utils.ActivitiesUtils.showEmail
import lib.toolkit.base.managers.utils.FragmentUtils.showFragment
import lib.toolkit.base.managers.utils.FragmentUtils.showSingleFragment
import lib.toolkit.base.managers.utils.UiUtils.getDeviceInfoString
import lib.toolkit.base.ui.activities.base.BaseActivity

abstract class BaseAppActivity : BaseActivity(), FragmentManager.OnBackStackChangedListener,
    ContextDialogInterface {

    private val TAG = javaClass.simpleName
    private var handler: Handler? = null
    private var finishRunnable: Runnable? = null
    private var isFinish = false
    private var isBackStackNotice = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        init()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putBoolean(TAG_FINISH, isFinish)
        super.onSaveInstanceState(outState)
    }

    override fun onContextDialogOpen() {}
    override fun onDestroy() {
        super.onDestroy()
        finishRunnable?.let { runnable ->
            handler?.removeCallbacks(runnable)
        }
        supportFragmentManager.removeOnBackStackChangedListener(this)
    }

    override fun onBackStackChanged() {}

    private fun init() {
        initHandlers()
        initViews()
    }

    private fun initHandlers() {
        isBackStackNotice = false
        handler = Handler()
        finishRunnable = Runnable { isFinish = false }
    }

    private fun startResetFinishTimer() {
        finishRunnable?.let { runnable ->
            handler?.postDelayed(runnable, TIMER_FINISH.toLong())
        }
    }

    private fun initViews() {
        supportFragmentManager.addOnBackStackChangedListener(this)
        isFinish = false
    }

    private fun showEmailClients(to: String?, subject: String?, body: String?) {
        showEmail(this, getString(lib.toolkit.base.R.string.chooser_email_client), to!!, subject!!, body!!)
    }

    /*
     * Fragment operations
     * */
    protected fun showFragment(fragment: Fragment, tag: String?) {
        showFragment(supportFragmentManager, fragment, R.id.frame_container, tag, false)
    }

    protected fun showSingleFragment(fragment: Fragment, backStackTag: String?) {
        showSingleFragment(supportFragmentManager, fragment, R.id.frame_container, backStackTag!!)
    }

    /*
     * Helper methods
     * */
    protected fun showUrlInBrowser(url: String?) {
        showBrowser(this, getString(R.string.chooser_web_browser), url!!)
    }

    protected fun showEmailClientTemplate() {
        showEmailClients(
            getString(R.string.app_support_email), getString(lib.toolkit.base.R.string.about_email_subject),
            getDeviceInfoString(null, true)
        )
    }

    protected fun showEmailClientTemplate(message: String) {
        showEmailClients(
            getString(R.string.app_support_email), getString(lib.toolkit.base.R.string.about_email_subject),
            message + getDeviceInfoString(null, false)
        )
    }

    companion object {
        private const val TAG_FINISH = "TAG_FINISH"
        private const val TIMER_FINISH = 5000
    }
}