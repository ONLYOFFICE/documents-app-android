package app.editors.manager.ui.activities.main

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import app.editors.manager.R
import app.editors.manager.databinding.ActivityAboutBinding
import app.editors.manager.ui.activities.base.BaseAppActivity
import app.editors.manager.ui.fragments.main.AboutFragment
import lib.toolkit.base.managers.utils.UiUtils

class AboutActivity : BaseAppActivity() {

    companion object {
        val TAG: String = AboutActivity::class.java.simpleName

        fun show(context: Context) {
            context.startActivity(Intent(context, AboutActivity::class.java))
        }
    }

    private var viewBinding: ActivityAboutBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityAboutBinding.inflate(layoutInflater)
        setContentView(viewBinding?.root)
        init(savedInstanceState)
    }

    override fun onDestroy() {
        super.onDestroy()
        viewBinding = null
    }

    override fun onBackStackChanged() {
        super.onBackStackChanged()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        if (supportFragmentManager.backStackEntryCount > 0) {
            supportActionBar?.setHomeAsUpIndicator(
                UiUtils.getFilteredDrawable(
                    this,
                    R.drawable.ic_toolbar_back,
                    lib.toolkit.base.R.color.colorWhite
                )
            )
        } else {
            supportActionBar?.setHomeAsUpIndicator(
                UiUtils.getFilteredDrawable(
                    this,
                    R.drawable.ic_toolbar_close,
                    lib.toolkit.base.R.color.colorWhite
                )
            )
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun init(savedInstanceState: Bundle?) {
        setSupportActionBar(viewBinding?.appBarToolbar)
        onBackStackChanged()
        if (savedInstanceState == null) {
            showFragment(AboutFragment.newInstance(), null)
        }
    }
}