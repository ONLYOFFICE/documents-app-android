package app.editors.manager.ui.activities.main

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.view.ViewPropertyAnimator
import app.editors.manager.R
import app.editors.manager.databinding.ActivityMediaBinding
import app.editors.manager.mvp.models.explorer.Explorer
import app.editors.manager.ui.activities.base.BaseAppActivity
import app.editors.manager.ui.fragments.media.MediaPagerFragment
import lib.toolkit.base.managers.utils.UiUtils
import lib.toolkit.base.managers.utils.getSerializable

class MediaActivity : BaseAppActivity(), View.OnClickListener {

    private var viewBinding: ActivityMediaBinding? = null
    private var uncaughtExceptionHandler: Thread.UncaughtExceptionHandler? = null
    private var viewPropertyAnimator: ViewPropertyAnimator? = null
    private var backDrawable: Drawable? = null

    private val toolbarRunnableGone = Runnable {
        viewBinding?.appBarToolbar?.visibility = View.GONE
    }

    private val toolbarRunnableVisible = Runnable {
        viewBinding?.appBarToolbar?.visibility = View.VISIBLE
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityMediaBinding.inflate(layoutInflater)
        setContentView(viewBinding?.root)
        init(savedInstanceState)
    }

    override fun onDestroy() {
        super.onDestroy()
        viewBinding?.appBarToolbar?.removeCallbacks(toolbarRunnableGone)
        viewBinding = null
        killSelf()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            com.facebook.common.R.id.home -> {
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.app_bar_toolbar -> showToolbar()
        }
    }

    private fun init(savedInstanceState: Bundle?) {
        initException()
        initToolbar()
        savedInstanceState ?: run {
            val explorer = intent.getSerializable(TAG_MEDIA, Explorer::class.java)
            val isWebDav = intent.getBooleanExtra(TAG_WEB_DAV, false)
            showFragment(MediaPagerFragment.newInstance(explorer, isWebDav), null)
        }
    }

    private fun initException() {
        uncaughtExceptionHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread: Thread, throwable: Throwable? ->
            Log.d(TAG, "ID: " + thread.id + "; NAME: " + thread.name)
            setResult(Activity.RESULT_CANCELED, Intent())
            uncaughtExceptionHandler?.uncaughtException(thread, throwable ?: Throwable())
        }
    }

    private fun initToolbar() {
        backDrawable = UiUtils
            .getFilteredDrawable(this, R.drawable.ic_toolbar_back, lib.toolkit.base.R.color.colorWhite)
        setSupportActionBar(viewBinding?.appBarToolbar)
        setStatusBarColor(lib.toolkit.base.R.color.colorBlack)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(backDrawable)
    }

    private fun resetToolbarView(isVisible: Boolean) {
        viewPropertyAnimator?.cancel()
        viewBinding?.appBarToolbar?.run {
            removeCallbacks(toolbarRunnableGone)
            removeCallbacks(toolbarRunnableVisible)
            visibility = View.VISIBLE
            alpha = if (isVisible) ALPHA_TO else ALPHA_FROM
        }
    }

    fun setToolbarView(view: View?) {
        viewBinding?.appBarToolbarContainer?.run {
            removeAllViews()
            addView(view)
        }
    }

    fun setToolbarState(isAnimating: Boolean) {
        viewBinding?.appBarToolbar?.setOnClickListener(if (isAnimating) this else null)
        resetToolbarView(true)
    }

    fun showToolbar(): Boolean {
        val isVisible = viewBinding?.appBarToolbar?.visibility == View.VISIBLE
        resetToolbarView(isVisible)
        viewPropertyAnimator = viewBinding?.run {
            appBarToolbar.animate()
                .alpha(if (isVisible) ALPHA_FROM else ALPHA_TO)
                .setDuration(ALPHA_DELAY.toLong())
                .withEndAction(if (isVisible) toolbarRunnableGone else toolbarRunnableVisible)
        }?.apply {
            start()
        }
        return isVisible
    }

    val isToolbarVisible: Boolean
        get() = viewBinding?.appBarToolbar?.visibility == View.VISIBLE

    companion object {
        val TAG: String = MediaActivity::class.java.simpleName
        const val TAG_MEDIA = "TAG_MEDIA"
        const val TAG_WEB_DAV = "TAG_WEB_DAV"
        const val ALPHA_DELAY = 300
        const val ALPHA_FROM = 0.0f
        const val ALPHA_TO = 1.0f

        fun getIntent(context: Context, explorer: Explorer, isWebDav: Boolean): Intent {
            return Intent(context, MediaActivity::class.java).apply {
                putExtra(TAG_MEDIA, explorer)
                putExtra(TAG_WEB_DAV, isWebDav)
            }
        }
    }
}