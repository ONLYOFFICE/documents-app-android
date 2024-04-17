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
        if (intent.getStringExtra(TAG_PROVIDER_KEY) != null) {
            finish()
            return
        }
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
            showFragment(
                SelectFragment.newInstance(
                    isRoomStorage = intent.getBooleanExtra(TAG_IS_ROOM_STORAGE, false),
                    title = intent.getStringExtra(TAG_TITLE),
                    providerKey = intent.getStringExtra(TAG_PROVIDER_KEY),
                    providerId = intent.getIntExtra(TAG_PROVIDER_ID, -1)
                ), null
            )
        }
        setFinishOnTouchOutside(true)
    }

    override fun finishWithResult(folder: CloudFolder?) {
        if (title == null && providerId == -1 && !isRoomStorage) {
            val intent = Intent().apply { putExtra(TAG_RESULT, folder) }
            setResult(Activity.RESULT_OK, intent)
        }
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

    override val isRoomStorage: Boolean
        get() = intent.getBooleanExtra(TAG_IS_ROOM_STORAGE, false)

    override val title: String?
        get() = intent.getStringExtra(TAG_TITLE)

    override val providerId: Int
        get() = intent.getIntExtra(TAG_PROVIDER_ID, -1)

    companion object {

        val TAG = StorageActivity::class.java.simpleName
        const val TAG_RESULT = "TAG_RESULT"
        private const val TAG_SECTION = "TAG_SECTION"
        private const val TAG_IS_ROOM_STORAGE = "TAG_IS_ROOM_STORAGE"
        private const val TAG_TITLE = "TAG_TITLE"
        private const val TAG_PROVIDER_KEY = "TAG_PROVIDER_KEY"
        private const val TAG_PROVIDER_ID = "TAG_PROVIDER_ID"

        @JvmStatic
        fun show(
            fragment: Fragment,
            isMySection: Boolean,
            isRoomStorage: Boolean = false,
            title: String? = null,
            providerKey: String? = null,
            providerId: Int? = null
        ) {
            fragment.startActivityForResult(
                getIntent(fragment.requireContext(), isMySection, isRoomStorage, title, providerKey, providerId),
                REQUEST_ACTIVITY_STORAGE
            )
        }

        fun getIntent(
            context: Context,
            isMySection: Boolean,
            isRoomStorage: Boolean,
            title: String?,
            providerKey: String?,
            providerId: Int?
        ): Intent {
            return Intent(context, StorageActivity::class.java).apply {
                putExtra(TAG_SECTION, isMySection)
                putExtra(TAG_IS_ROOM_STORAGE, isRoomStorage)
                putExtra(TAG_TITLE, title)
                putExtra(TAG_PROVIDER_KEY, providerKey)
                putExtra(TAG_PROVIDER_ID, providerId)
            }
        }
    }
}