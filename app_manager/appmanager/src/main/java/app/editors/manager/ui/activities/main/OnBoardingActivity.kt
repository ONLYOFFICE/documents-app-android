package app.editors.manager.ui.activities.main

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import app.editors.manager.app.appComponent
import app.editors.manager.databinding.ActivityOnBoardingBinding
import app.editors.manager.ui.activities.base.BaseAppActivity
import app.editors.manager.ui.activities.main.OnBoardingActivity
import app.editors.manager.ui.fragments.onboarding.OnBoardingPagerFragment

class OnBoardingActivity : BaseAppActivity() {
    private var viewBinding: ActivityOnBoardingBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(lib.toolkit.base.R.style.NoActionBarTheme)
        super.onCreate(savedInstanceState)
        viewBinding = ActivityOnBoardingBinding.inflate(layoutInflater)
        setContentView(viewBinding?.root)
        init(savedInstanceState)
    }

    private fun init(savedInstanceState: Bundle?) {
        if (!appComponent.preference.onBoarding) {
            savedInstanceState ?: run {
                showFragment(OnBoardingPagerFragment.newInstance(), null)
            }
        } else {
            MainActivity.show(this, null, intent.extras)
            finishAndRemoveTask()
        }

    }

    companion object {
        val TAG: String = OnBoardingActivity::class.java.simpleName

        fun show(activity: Activity) {
            activity.startActivityForResult(
                Intent(activity, OnBoardingActivity::class.java),
                REQUEST_ACTIVITY_ONBOARDING
            )
        }
    }
}