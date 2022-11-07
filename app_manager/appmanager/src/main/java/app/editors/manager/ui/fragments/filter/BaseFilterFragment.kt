package app.editors.manager.ui.fragments.filter

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.clearFragmentResultListener
import androidx.fragment.app.setFragmentResultListener
import app.editors.manager.R
import app.editors.manager.databinding.FragmentFilterBinding
import app.editors.manager.mvp.presenters.filter.BaseFilterPresenter
import app.editors.manager.mvp.views.filter.FilterView
import app.editors.manager.ui.activities.main.IFilterActivity
import app.editors.manager.ui.dialogs.fragments.FilterDialogFragment.Companion.BUNDLE_KEY_REFRESH
import app.editors.manager.ui.dialogs.fragments.FilterDialogFragment.Companion.REQUEST_KEY_REFRESH
import app.editors.manager.ui.dialogs.fragments.IBaseDialogFragment
import app.editors.manager.ui.fragments.base.BaseAppFragment
import app.editors.manager.ui.views.custom.SingleChoiceChipGroupView
import lib.toolkit.base.managers.utils.FragmentUtils

abstract class BaseFilterFragment : BaseAppFragment(), FilterView {

    companion object {
        val TAG = BaseFilterFragment::class.simpleName
        const val KEY_FOLDER_ID = "key_folder_id"
    }

    private var viewBinding: FragmentFilterBinding? = null
    private var chipGroups: Array<out SingleChoiceChipGroupView>? = null

    protected var activity: IFilterActivity? = null
    protected val dialog: IBaseDialogFragment? get() = getDialogFragment()
    protected val linearLayout: LinearLayout? get() = viewBinding?.linearLayout
    
    abstract val filterPresenter: BaseFilterPresenter

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (!isTablet) {
            activity = try {
                context as IFilterActivity
            } catch (e: ClassCastException) {
                throw RuntimeException(
                    BaseFilterFragment::class.java.simpleName + " - must implement - " +
                            IFilterActivity::class.java.simpleName
                )
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewBinding = FragmentFilterBinding.inflate(inflater)
        return viewBinding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        menu.clear()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewBinding = null
    }

    override fun updateViewState(isChanged: Boolean) {
        if (isTablet) {
            dialog?.setToolbarButtonEnabled(filterPresenter.hasFilter)
            if (isChanged) requireActivity().supportFragmentManager
                .setFragmentResult(REQUEST_KEY_REFRESH, bundleOf(BUNDLE_KEY_REFRESH to true))
        } else {
            activity?.setResetButtonEnabled(filterPresenter.hasFilter)
            if (isChanged) requireActivity().setResult(Activity.RESULT_OK)
        }
    }

    override fun onFilterProgress() {
        viewBinding?.resultText?.isVisible = false
        viewBinding?.resultProgress?.isVisible = true
    }

    override fun onFilterResult(count: Int) {
        viewBinding?.resultProgress?.isVisible = false
        viewBinding?.resultText?.isVisible = true
        viewBinding?.resultText?.text = getString(R.string.filter_show_results, count.toString())
    }

    override fun onError(message: String?) {
        showToast(message ?: getString(R.string.errors_connection_error))
    }

    override fun onFilterReset() {
        chipGroups?.forEach { chipGroup ->
            chipGroup.clearCheck()
        }
    }

    private fun init() {
        initDefault()
        initToolbar()
        initShowButton()
        initViews()
    }

    private fun initDefault() {
        activity?.setResetButtonVisible(true)
        activity?.setResetButtonListener(filterPresenter::reset)
        setHasOptionsMenu(true)
    }

    private fun initToolbar() {
        if (isTablet) {
            dialog?.setToolbarButtonVisible(isVisible = true)
            dialog?.setToolbarNavigationIcon(isClose = true)
            dialog?.setToolbarTitle(getString(R.string.filter_toolbar_title))
            dialog?.setToolbarButtonTitle(getString(R.string.filter_button_reset))
            dialog?.setToolbarButtonClickListener { filterPresenter.reset() }
            dialog?.getMenu()?.findItem(R.id.toolbar_item_search)?.isVisible = false
        } else {
            activity?.setResetButtonEnabled(filterPresenter.hasFilter)
            setActionBarTitle(getString(R.string.filter_toolbar_title))
        }
    }

    private fun initShowButton() {
        viewBinding?.showButton?.setOnClickListener {
            if (isTablet) {
                dialog?.dismiss()
            } else {
                requireActivity().finish()
            }
        }
    }

    protected fun showAuthorFragment(
        fragmentManager: FragmentManager,
        isRoom: Boolean = false,
        isGroups: Boolean = false,
        selectedId: String,
        onResultListener: (Bundle) -> Unit
    ) {
        FragmentUtils.showFragment(
            fragmentManager,
            FilterAuthorFragment.newInstance(selectedId, isGroups, isRoom),
            R.id.frame_container,
            FilterAuthorFragment.TAG
        )
        setFragmentResultListener(FilterAuthorFragment.REQUEST_KEY_AUTHOR) { _, bundle ->
            onResultListener.invoke(bundle)
            clearFragmentResultListener(FilterAuthorFragment.REQUEST_KEY_AUTHOR)
        }
    }

    protected fun addChipGroups(vararg chipGroups: SingleChoiceChipGroupView?) {
        this.chipGroups = chipGroups.filterNotNull().toTypedArray()
        chipGroups.reversed().forEach { chipGroup ->
            linearLayout?.addView(chipGroup, 0)
        }
    }

    abstract fun initViews()
}