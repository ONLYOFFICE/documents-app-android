package app.editors.manager.ui.activities.main

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.fragment.app.Fragment
import app.editors.manager.R
import app.editors.manager.databinding.ActivityStorageBinding
import app.editors.manager.mvp.models.explorer.CloudFolder
import app.editors.manager.ui.activities.base.BaseAppActivity
import app.editors.manager.ui.fragments.storage.SelectFragment

class StorageActivity : BaseAppActivity() {

    private var viewBinding: ActivityStorageBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityStorageBinding.inflate(layoutInflater)
        setContentView(viewBinding?.root)
        init(savedInstanceState)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.home -> {
                onBackPressed()
                return true
            }
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

    fun finishWithResult(folder: CloudFolder?) {
        val intent = Intent().apply { putExtra(TAG_RESULT, folder) }
        setResult(Activity.RESULT_OK, intent)
        finish()
    }

    val isMySection: Boolean
        get() {
            return if (intent.hasExtra(TAG_SECTION)) {
                intent.getBooleanExtra(TAG_SECTION, false)
            } else {
                throw RuntimeException(StorageActivity::class.java.simpleName
                        + " - must open with extra: " + TAG_SECTION)
            }
        }

    companion object {
        val TAG = StorageActivity::class.java.simpleName
        const val TAG_SECTION = "TAG_SECTION"
        const val TAG_RESULT = "TAG_RESULT"

        @JvmStatic
        fun show(fragment: Fragment, isMySection: Boolean) {
            val intent = Intent(fragment.context, StorageActivity::class.java).apply {
                putExtra(TAG_SECTION, isMySection)
            }
            fragment.startActivityForResult(intent, REQUEST_ACTIVITY_STORAGE)
        }
    }
}