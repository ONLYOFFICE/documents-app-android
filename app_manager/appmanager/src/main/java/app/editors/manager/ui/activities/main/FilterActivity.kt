package app.editors.manager.ui.activities.main

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import app.documents.core.network.ApiContract
import app.editors.manager.R
import app.editors.manager.databinding.ActivityFilterBinding
import app.editors.manager.ui.activities.base.BaseAppActivity
import app.editors.manager.ui.fragments.filter.CloudFilterFragment
import app.editors.manager.ui.fragments.filter.RoomFilterFragment
import lib.toolkit.base.managers.utils.FragmentUtils

interface IFilterActivity {
    fun setResetButtonEnabled(isEnable: Boolean)
    fun setResetButtonVisible(isVisible: Boolean)
    fun setResetButtonListener(onClick: () -> Unit)
}

class FilterActivity : BaseAppActivity(), IFilterActivity {

    companion object {
        const val REQUEST_ACTIVITY_FILTERS_CHANGED = 1004
        private const val KEY_ID = "key_id"
        private const val KEY_SECTION = "key_section"

        fun getIntent(fragment: Fragment, folderId: String?, section: Int): Intent {
            return Intent(fragment.requireContext(), FilterActivity::class.java).apply {
                putExtra(KEY_ID, folderId)
                putExtra(KEY_SECTION, section)
            }
        }
    }

    private var viewBinding: ActivityFilterBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityFilterBinding.inflate(layoutInflater)
        setContentView(viewBinding?.root)
        init(savedInstanceState)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.docs_filter, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onDestroy() {
        super.onDestroy()
        viewBinding = null
    }

    override fun setResetButtonEnabled(isEnable: Boolean) {
        viewBinding?.resetButton?.isEnabled = isEnable
    }

    override fun setResetButtonVisible(isVisible: Boolean) {
        viewBinding?.resetButton?.isVisible = isVisible
    }

    override fun setResetButtonListener(onClick: () -> Unit) {
        viewBinding?.resetButton?.setOnClickListener { onClick() }
    }

    private fun init(savedInstanceState: Bundle?) {
        initToolbar()
        if (savedInstanceState == null) {
            intent.extras?.getString(KEY_ID)?.let { folderId ->
                showFilterFragment(folderId)
            }
        }
    }

    private fun initToolbar() {
        viewBinding?.toolbar?.let { toolbar ->
            setSupportActionBar(toolbar)
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            toolbar.setNavigationOnClickListener { onBackPressed() }
        }
    }

    private fun showFilterFragment(folderId: String) {
        FragmentUtils.showFragment(
            supportFragmentManager,
            getFragmentInstance(folderId),
            R.id.frame_container
        )
    }

    private fun getFragmentInstance(folderId: String): Fragment {
        val section = intent?.extras?.getInt(KEY_SECTION) ?: -1
        return when {
            ApiContract.SectionType.isRoom(section) -> RoomFilterFragment.newInstance(folderId)
            else -> CloudFilterFragment.newInstance(folderId)
        }
    }
}