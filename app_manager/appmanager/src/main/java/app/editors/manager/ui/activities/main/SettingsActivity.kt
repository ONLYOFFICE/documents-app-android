package app.editors.manager.ui.activities.main

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.widget.Toolbar
import app.editors.manager.R
import app.editors.manager.ui.activities.base.BaseAppActivity
import app.editors.manager.ui.fragments.main.AppSettingsFragment
import lib.toolkit.base.managers.utils.FragmentUtils.showFragment

class SettingsActivity : BaseAppActivity() {

    companion object {
        val TAG: String = SettingsActivity::class.java.simpleName

        @JvmStatic
        fun show(context: Context) {
            context.startActivity(Intent(context, SettingsActivity::class.java))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        initToolbar()
        showFragment(savedInstanceState)
    }

    private fun initToolbar() {
        findViewById<Toolbar>(R.id.app_bar_toolbar)?.let { toolbar ->
            setSupportActionBar(toolbar)
            supportActionBar?.apply {
                setDisplayHomeAsUpEnabled(true)
                setTitle(R.string.settings_item_title)
            }
            toolbar.setNavigationOnClickListener { onBackPressed() }
        }

    }

    private fun showFragment(savedInstanceState: Bundle?) {
        if (savedInstanceState == null) {
            showFragment(supportFragmentManager,  AppSettingsFragment.newInstance(), R.id.frame_container)
        }
    }

}