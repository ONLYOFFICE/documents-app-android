package app.editors.manager.ui.activities.main

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import app.editors.manager.R
import app.editors.manager.databinding.ActivityFilterBinding
import app.editors.manager.ui.activities.base.BaseAppActivity
import app.editors.manager.ui.fragments.filter.FilterFragment
import lib.toolkit.base.managers.utils.FragmentUtils

interface IFilterActivity {
    fun setResetButtonEnabled(isEnable: Boolean)
    fun setResetButtonVisible(isVisible: Boolean)
}

class FilterActivity : BaseAppActivity(), IFilterActivity {

    companion object {
        const val KEY_ID = "key_id"
        const val REQUEST_ACTIVITY_FILTERS_CHANGED = 1004

        fun getIntent(fragment: Fragment, folderId: String?): Intent {
            return  Intent(fragment.requireContext(), FilterActivity::class.java).apply {
                putExtra(KEY_ID, folderId)
            }
        }

        fun show(fragment: Fragment, folderId: String?) {
            fragment.startActivityForResult(
                Intent(fragment.requireContext(), FilterActivity::class.java)
                    .putExtra(KEY_ID, folderId),
                REQUEST_ACTIVITY_FILTERS_CHANGED
            )
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

    private fun init(savedInstanceState: Bundle?) {
        initToolbar()
        viewBinding?.resetButton?.setOnClickListener {
            (supportFragmentManager
                .findFragmentByTag(FilterFragment.TAG) as? FilterFragment)?.resetFilters()
        }
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
            FilterFragment.newInstance(folderId),
            R.id.frame_container
        )
    }
}