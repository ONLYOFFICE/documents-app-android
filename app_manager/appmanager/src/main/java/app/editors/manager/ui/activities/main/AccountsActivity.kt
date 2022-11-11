package app.editors.manager.ui.activities.main

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import app.editors.manager.R
import app.editors.manager.mvp.presenters.main.CloudAccountPresenter
import app.editors.manager.ui.activities.base.BaseAppActivity
import app.editors.manager.ui.fragments.main.CloudAccountFragment
import com.google.android.material.appbar.MaterialToolbar
import lib.toolkit.base.managers.utils.FragmentUtils

class AccountsActivity : BaseAppActivity() {

    companion object {
        val TAG: String = AccountsActivity::class.java.simpleName

        const val RESULT_NO_LOGGED_IN_ACCOUNTS = 1021

        fun show(activity: Activity, isSwitch: Boolean = false) {
            activity.startActivityForResult(
                Intent(activity, AccountsActivity::class.java).apply {
                    putExtra(CloudAccountPresenter.KEY_SWITCH, isSwitch)
                },
                REQUEST_ACTIVITY_ACCOUNTS
            )
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_accounts)
        init(savedInstanceState)
    }

    private fun init(savedInstanceState: Bundle?) {
        initToolbar()
        if (savedInstanceState == null) {
            showFragment()
        }
    }

    private fun initToolbar() {
        val toolbar: MaterialToolbar = findViewById(R.id.appToolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.let {
            it.setDisplayHomeAsUpEnabled(true)
            toolbar.setNavigationOnClickListener { onBackPressed() }
        }
    }

    override fun onBackPressed() {
        if (supportFragmentManager.backStackEntryCount > 0) {
            supportFragmentManager.popBackStack()
        } else {
            super.onBackPressed()
        }
    }

    private fun showFragment() {
        FragmentUtils.showFragment(
            supportFragmentManager,
            CloudAccountFragment.newInstance(intent.extras?.getBoolean(CloudAccountPresenter.KEY_SWITCH) ?: false),
            R.id.frame_container
        )
    }

}