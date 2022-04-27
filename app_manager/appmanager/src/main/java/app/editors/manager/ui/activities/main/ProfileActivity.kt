package app.editors.manager.ui.activities.main

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.widget.Toolbar
import app.editors.manager.R
import app.editors.manager.ui.activities.base.BaseAppActivity
import app.editors.manager.ui.fragments.main.ProfileFragment
import lib.toolkit.base.managers.utils.FragmentUtils

class ProfileActivity : BaseAppActivity() {

    companion object {
        val TAG: String = ProfileActivity::class.java.simpleName

        const val REQUEST_PROFILE = 1001
        const val RESULT_LOGIN = 1005

        fun show(activity: Activity, account: String) {
            activity.startActivityForResult(Intent(activity, ProfileActivity::class.java).apply {
                putExtra(ProfileFragment.KEY_ACCOUNT, account)
            }, REQUEST_PROFILE)
        }
    }


    private lateinit var toolbar: Toolbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
        init(savedInstanceState)
    }

    private fun init(savedInstanceState: Bundle?) {
        initToolbar()
        if (savedInstanceState == null) {
            showFragment()
        }
    }

    private fun initToolbar() {
        toolbar = findViewById(R.id.appToolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.let {
            it.setDisplayHomeAsUpEnabled(true)
            it.setTitle(R.string.fragment_profile_title)
            toolbar.setNavigationOnClickListener { onBackPressed() }
        }
    }

    private fun showFragment() {
        val fragment = ProfileFragment.newInstance(intent.getStringExtra(ProfileFragment.KEY_ACCOUNT))
        FragmentUtils.showFragment(supportFragmentManager, fragment, R.id.frame_container)
    }

}