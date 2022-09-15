package app.editors.manager.ui.activities.login

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.core.view.isVisible
import app.editors.manager.R
import app.editors.manager.databinding.ActivityOnBoardingBinding
import app.editors.manager.ui.activities.base.BaseAppActivity
import app.editors.manager.ui.fragments.login.AuthPagerFragment
import lib.toolkit.base.managers.tools.ResourcesProvider

class AuthAppActivity : BaseAppActivity() {

    companion object {
        val TAG: String = AuthAppActivity::class.java.simpleName

        const val REQUEST_KEY = "ACCOUNT_KEY"
        const val TFA_KEY = "TFA_KEY"

        fun show(activity: Activity, request: String, key: String) {
            activity.startActivity(Intent(activity, AuthAppActivity::class.java).apply {
                putExtra(REQUEST_KEY, request)
                putExtra(TFA_KEY, key)
            })
        }
    }

    private var viewBinding: ActivityOnBoardingBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityOnBoardingBinding.inflate(layoutInflater)
        setContentView(viewBinding?.root)
        init(savedInstanceState)
    }

    override fun onDestroy() {
        super.onDestroy()
        viewBinding = null
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun init(savedInstanceState: Bundle?) {
        if (savedInstanceState == null) {
            showFragment(AuthPagerFragment.newInstance(intent.getStringExtra(REQUEST_KEY), intent.getStringExtra(TFA_KEY)), null)
        }
    }

    fun setActionBar() {
        viewBinding?.appBarToolbar?.isVisible = true
        setSupportActionBar(viewBinding?.appBarToolbar)
        supportActionBar?.let {
            it.title = ""
            it.setDisplayHomeAsUpEnabled(true)
            it.setDisplayShowHomeEnabled(true)
            it.setHomeAsUpIndicator(ResourcesProvider(this)
                .getDrawable(R.drawable.ic_toolbar_close)?.apply {
                    setTint(getColor(lib.toolkit.base.R.color.colorPrimary))
                });
        }
    }
}