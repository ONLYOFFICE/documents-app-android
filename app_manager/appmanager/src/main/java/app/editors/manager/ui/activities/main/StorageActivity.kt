package app.editors.manager.ui.activities.main

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.fragment.app.Fragment
import app.documents.core.network.manager.models.explorer.CloudFolder
import app.editors.manager.databinding.ActivityStorageBinding
import app.editors.manager.ui.activities.base.BaseAppActivity
import app.editors.manager.ui.fragments.storage.SelectFragment
import app.editors.manager.ui.interfaces.WebDavInterface

class StorageActivity : BaseAppActivity(), WebDavInterface {

    private var viewBinding: ActivityStorageBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityStorageBinding.inflate(layoutInflater)
        setContentView(viewBinding?.root)
        init(savedInstanceState)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            if (supportFragmentManager.fragments.size > 1) {
                supportFragmentManager.popBackStack()
            } else {
                onBackPressed()
            }
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        hideKeyboard()
    }

    override fun onDestroy() {
        super.onDestroy()
        viewBinding = null
    }

    private fun init(savedInstanceState: Bundle?) {
        setSupportActionBar(viewBinding?.appBarToolbar)
        savedInstanceState ?: run {
            showFragment(SelectFragment.newInstance(), null)
        }
        setFinishOnTouchOutside(true)
    }

    override fun finishWithResult(folder: CloudFolder?) {
        val intent = Intent().apply { putExtra(TAG_RESULT, folder) }
        setResult(Activity.RESULT_OK, intent)
        finish()
    }

    override val isMySection: Boolean
        get() {
            return if (intent.hasExtra(TAG_SECTION)) {
                intent.getBooleanExtra(TAG_SECTION, false)
            } else {
                throw RuntimeException(
                    StorageActivity::class.java.simpleName
                            + " - must open with extra: " + TAG_SECTION
                )
            }
        }

    override val isTitleRequired: Boolean
        get() = intent.getBooleanExtra(TAG_TITLE_REQUIRED, true)

    companion object {

        val TAG = StorageActivity::class.java.simpleName
        const val TAG_RESULT = "TAG_RESULT"
        private const val TAG_SECTION = "TAG_SECTION"
        private const val TAG_TITLE_REQUIRED = "TAG_TITLE_REQUIRED"

        @JvmStatic
        fun show(fragment: Fragment, isMySection: Boolean, isTitleRequired: Boolean = true) {
            fragment.startActivityForResult(
                getIntent(fragment.requireContext(), isMySection, isTitleRequired),
                REQUEST_ACTIVITY_STORAGE
            )
        }

        fun getIntent(context: Context, isMySection: Boolean, isTitleRequired: Boolean): Intent {
            return Intent(context, StorageActivity::class.java).apply {
                putExtra(TAG_SECTION, isMySection)
                putExtra(TAG_TITLE_REQUIRED, isTitleRequired)
            }
        }
    }
}