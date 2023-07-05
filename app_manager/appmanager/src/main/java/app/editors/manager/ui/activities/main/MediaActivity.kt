package app.editors.manager.ui.activities.main

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.widget.Toolbar
import androidx.core.view.isInvisible
import app.documents.core.network.manager.models.explorer.Explorer
import app.editors.manager.R
import app.editors.manager.databinding.ActivityMediaBinding
import app.editors.manager.ui.activities.base.BaseAppActivity
import app.editors.manager.ui.fragments.media.MediaPagerFragment
import lib.toolkit.base.managers.utils.getSerializable

class MediaActivity : BaseAppActivity(), View.OnClickListener {

    private var viewBinding: ActivityMediaBinding? = null
    private var shareItem: MenuItem? = null

    private val explorer by lazy { intent.getSerializable(TAG_MEDIA, Explorer::class.java) }
    private val isWebDav by lazy { intent.getBooleanExtra(TAG_WEB_DAV, false) }
    private val clickedPosition by lazy { explorer.files.indexOfFirst { it.isClicked } }

    var shareButtonVisible: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityMediaBinding.inflate(layoutInflater)
        setContentView(viewBinding?.root)
        init(savedInstanceState)
    }

    override fun onDestroy() {
        super.onDestroy()
        viewBinding = null
        killSelf()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_media, menu)
        shareItem = menu?.findItem(R.id.toolbarShare)
        shareVisible = shareButtonVisible
        return super.onCreateOptionsMenu(menu)
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.app_bar_toolbar -> toggleToolbar()
        }
    }

    private fun init(savedInstanceState: Bundle?) {
        initToolbar()
        if (savedInstanceState == null) {
            showFragment(MediaPagerFragment.newInstance(explorer, isWebDav, clickedPosition), null)
        }
    }

    private fun initToolbar() {
        viewBinding?.mediaToolbar?.let { toolbar ->
            setSupportActionBar(toolbar)
            toolbar.setNavigationIcon(lib.toolkit.base.R.drawable.ic_close)
            toolbar.setNavigationIconTint(getColor(lib.toolkit.base.R.color.colorWhite))
            toolbar.setNavigationOnClickListener { finish() }
            toolbar.setOnClickListener(this)
        }
    }

    var shareVisible: Boolean
        get() = shareItem?.isVisible == true
        set(value) {
            shareItem?.isVisible = value
        }

    private val isToolbarVisible: Boolean
        get() = viewBinding?.mediaToolbar?.isInvisible == false

    fun setToolbarTitle(title: String?) {
        supportActionBar?.title = title.orEmpty()
    }

    fun setToolbarSubtitle(subtitle: String) {
        supportActionBar?.subtitle = subtitle
    }

    fun toggleToolbar(): Boolean {
        viewBinding?.mediaToolbar?.isInvisible = isToolbarVisible
        return isToolbarVisible
    }

    fun setOnMenuItemClickListener(listener: Toolbar.OnMenuItemClickListener) {
        viewBinding?.mediaToolbar?.setOnMenuItemClickListener(listener)
    }

    companion object {
        val TAG: String = MediaActivity::class.java.simpleName
        const val TAG_MEDIA = "TAG_MEDIA"
        const val TAG_WEB_DAV = "TAG_WEB_DAV"

        fun getIntent(context: Context, explorer: Explorer, isWebDav: Boolean): Intent {
            return Intent(context, MediaActivity::class.java).apply {
                putExtra(TAG_MEDIA, explorer)
                putExtra(TAG_WEB_DAV, isWebDav)
            }
        }
    }

}