package app.editors.manager.ui.activities.main

import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.result.ActivityResult
import androidx.fragment.app.Fragment
import app.documents.core.network.manager.models.explorer.CloudFile
import app.documents.core.network.manager.models.explorer.Item
import app.editors.manager.databinding.ActivityShareBinding
import app.editors.manager.ui.activities.base.BaseAppActivity
import app.editors.manager.ui.fragments.share.AddPagerFragment
import app.editors.manager.ui.fragments.share.SettingsFragment
import app.editors.manager.ui.fragments.share.UsersFragment
import app.editors.manager.ui.views.animation.HeightValueAnimator
import lib.toolkit.base.managers.utils.getSerializableExt

class ShareActivity : BaseAppActivity() {

    companion object {
        val TAG: String = ShareActivity::class.java.simpleName

        const val TAG_SHARE_ITEM = "TAG_SHARE_ITEM"
        const val TAG_INFO = "TAG_INFO"
        const val TAG_LEAVE = "TAG_LEAVE"
        const val TAG_RESULT = "TAG_RESULT"
        const val TAG_ID = "TAG_ID"
        private const val PIXEL_C = "Pixel C"

        @JvmStatic
        fun show(fragment: Fragment, item: Item?, isInfo: Boolean = true, leave: Boolean = false) {
            fragment.startActivityForResult(Intent(fragment.context, ShareActivity::class.java).apply {
                putExtra(TAG_SHARE_ITEM, item)
                putExtra(TAG_INFO, isInfo)
                putExtra(TAG_LEAVE, leave)
            }, REQUEST_ACTIVITY_SHARE)
        }

        fun launchForResult(
            launcher: ManagedActivityResultLauncher<Intent, ActivityResult>,
            fragment: Fragment,
            item: Item?,
            isInfo: Boolean = true
        ) {
            val intent = Intent(fragment.context, ShareActivity::class.java).apply {
                putExtra(TAG_SHARE_ITEM, item)
                putExtra(TAG_INFO, isInfo)
            }
            launcher.launch(intent)
        }
    }

    private var valueAnimator: HeightValueAnimator? = null
    private var viewBinding: ActivityShareBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityShareBinding.inflate(layoutInflater)
        setContentView(viewBinding?.root)
        setFinishOnTouchOutside(true)
        init(savedInstanceState)
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

    override fun onConfigurationChanged(newConfig: Configuration) {
        if (Build.MODEL == PIXEL_C) {
            super.onConfigurationChanged(newConfig)
        } else {
            recreate()
            super.onConfigurationChanged(newConfig)
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        hideKeyboard()
    }

    override fun onDestroy() {
        super.onDestroy()
        valueAnimator?.clear()
        viewBinding = null

    }

    private fun init(savedInstanceState: Bundle?) {
        valueAnimator = HeightValueAnimator(viewBinding?.appBarToolbar)
        setSupportActionBar(viewBinding?.appBarToolbar)

        if (savedInstanceState == null) {
            when  {
                intent.getStringExtra(TAG_ID)?.isNotEmpty() == true -> {
                    showFragment(AddPagerFragment.newInstance(CloudFile().apply {
                        id = intent.getStringExtra(TAG_ID)!!
                    }),null)
                }
                intent.getBooleanExtra(TAG_INFO, false) -> {
                    showFragment(SettingsFragment.newInstance(intent.getSerializableExt(TAG_SHARE_ITEM)), null)

                }
                intent.getBooleanExtra(TAG_LEAVE, false) -> {
                    showFragment(UsersFragment.newInstance(intent.getSerializableExt(TAG_SHARE_ITEM)), null)
                }
                else ->  {
                    showFragment(AddPagerFragment.newInstance(intent.getSerializableExt(TAG_SHARE_ITEM)), null)
                }
            }
        }
    }

    fun expandAppBar() {
        valueAnimator?.animate(true)
    }

    fun collapseAppBar() {
        valueAnimator?.animate(false)
    }

    fun getTabLayout() = viewBinding?.appBarTabs


}