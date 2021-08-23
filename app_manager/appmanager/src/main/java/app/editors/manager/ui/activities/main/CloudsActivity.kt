package app.editors.manager.ui.activities.main

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.widget.Toolbar
import app.editors.manager.R
import app.editors.manager.ui.activities.base.BaseAppActivity
import app.editors.manager.ui.fragments.main.CloudsFragment
import lib.toolkit.base.managers.utils.FragmentUtils

class CloudsActivity : BaseAppActivity() {

    private var toolbar: Toolbar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.clouds_activity_layout)
        toolbar = findViewById(R.id.appToolbar)
        init()
    }

    private fun init() {
        initToolbar()
        showFragment()
    }

    private fun initToolbar() {
        toolbar?.let { toolbar ->
            setSupportActionBar(toolbar)
            toolbar.setNavigationOnClickListener { onBackPressed() }
            supportActionBar?.apply {
                setDisplayHomeAsUpEnabled(true)
                setTitle(R.string.fragment_clouds_title)
            }
        }
    }

    private fun showFragment() {
        supportFragmentManager.findFragmentByTag(CloudsFragment.TAG)?.let { fragment ->
            FragmentUtils.showFragment(supportFragmentManager, fragment, R.id.frame_container)
        } ?: run {
            FragmentUtils.showFragment(
                supportFragmentManager,
                CloudsFragment.newInstance(true),
                R.id.frame_container
            )
        }
    }

    companion object {
        fun show(context: Context) {
            context.startActivity(Intent(context, CloudsActivity::class.java))
        }
    }
}