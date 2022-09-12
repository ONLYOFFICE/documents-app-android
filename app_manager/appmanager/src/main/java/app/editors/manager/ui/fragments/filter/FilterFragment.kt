package app.editors.manager.ui.fragments.filter

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.clearFragmentResultListener
import androidx.fragment.app.setFragmentResultListener
import app.editors.manager.R
import app.editors.manager.app.App
import app.editors.manager.app.accountOnline
import app.editors.manager.databinding.FragmentFilterBinding
import app.editors.manager.mvp.models.filter.FilterAuthor
import app.editors.manager.mvp.models.filter.FilterType
import app.editors.manager.mvp.models.filter.isNotEmpty
import app.editors.manager.mvp.presenters.filter.FilterPresenter
import app.editors.manager.mvp.views.filter.FilterView
import app.editors.manager.ui.activities.main.IFilterActivity
import app.editors.manager.ui.dialogs.fragments.FilterDialogFragment.Companion.BUNDLE_KEY_REFRESH
import app.editors.manager.ui.dialogs.fragments.FilterDialogFragment.Companion.REQUEST_KEY_REFRESH
import app.editors.manager.ui.fragments.base.BaseAppFragment
import app.editors.manager.ui.dialogs.fragments.IBaseDialogFragment
import com.google.android.material.chip.Chip
import lib.toolkit.base.managers.utils.FragmentUtils
import moxy.presenter.InjectPresenter
import moxy.presenter.ProvidePresenter

class FilterFragment : BaseAppFragment(), FilterView {

    companion object {
        val TAG = FilterFragment::class.simpleName
        private const val KEY_FOLDER_ID = "key_folder_id"

        fun newInstance(folderId: String?): FilterFragment {
            return FilterFragment().apply {
                arguments = Bundle(1).apply {
                    putString(KEY_FOLDER_ID, folderId)
                }
            }
        }

        fun getFilterType(checkedId: Int): FilterType {
            return FilterType.values().find { it.checkedId == checkedId } ?: FilterType.None
        }
    }

    @InjectPresenter
    lateinit var presenter: FilterPresenter

    @ProvidePresenter
    fun providePresenter(): FilterPresenter {
        return FilterPresenter(arguments?.getString(KEY_FOLDER_ID))
    }

    private var viewBinding: FragmentFilterBinding? = null
    private var activity: IFilterActivity? = null
    private val dialog: IBaseDialogFragment? get() = getDialogFragment()

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (!isTablet) {
            activity = try {
                context as IFilterActivity
            } catch (e: ClassCastException) {
                throw RuntimeException(
                    FilterFragment::class.java.simpleName + " - must implement - " +
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
        setAuthorChips()
        if (isTablet) {
            dialog?.setToolbarButtonEnabled(presenter.hasFilter)
            if (isChanged) requireActivity().supportFragmentManager
                .setFragmentResult(REQUEST_KEY_REFRESH, bundleOf(BUNDLE_KEY_REFRESH to true))
        } else {
            activity?.setResetButtonEnabled(presenter.hasFilter)
            if (isChanged) requireActivity().setResult(Activity.RESULT_OK)
        }
    }

    override fun onFilterReset() {
        viewBinding?.types?.clearCheck()
        viewBinding?.subfolder?.isChecked = false
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

    private fun init() {
        activity?.setResetButtonVisible(true)
        setHasOptionsMenu(true)
        initToolbar()
        initChipGroup()
        initViews()
        setAuthorChips()
        presenter.update(initialCall = true)
    }

    private fun initViews() {
        if (presenter.resultCount >= 0) onFilterResult(presenter.resultCount)
        viewBinding?.let { binding ->
            binding.subfolder.isChecked = presenter.excludeSubfolder
            binding.subfolder.setOnCheckedChangeListener { _, checked ->
                presenter.excludeSubfolder = checked
            }
            binding.showButton.setOnClickListener {
                if (isTablet) {
                    dialog?.dismiss()
                } else {
                    requireActivity().finish()
                }
            }
        }
    }

    private fun initToolbar() {
        if (isTablet) {
            dialog?.setToolbarButtonVisible(isVisible = true)
            dialog?.setToolbarNavigationIcon(isClose = true)
            dialog?.setToolbarTitle(getString(R.string.filter_toolbar_title))
            dialog?.setToolbarButtonTitle(getString(R.string.filter_button_reset))
            dialog?.setToolbarButtonClickListener { presenter.reset() }
            dialog?.getMenu()?.findItem(R.id.toolbar_item_search)?.isVisible = false
        } else {
            activity?.setResetButtonEnabled(presenter.hasFilter)
            setActionBarTitle(getString(R.string.filter_toolbar_title))
        }
    }

    private fun setAuthorChips() {
        viewBinding?.let { binding ->
            with(binding) {
                val author = presenter.filterAuthor

                if (App.getApp().accountOnline?.isPersonal() == true) {
                    authorTitle.isVisible = false
                    authorLayout.isVisible = false
                    presenter.filterAuthor = FilterAuthor()
                    return
                }

                users.setOnClickListener { showAuthorFragment(author.id, false) }
                groups.setOnClickListener { showAuthorFragment(author.id, true) }
                if (author.isNotEmpty()) {
                    users.isChecked = !author.isGroup
                    groups.isChecked = author.isGroup
                    users.isCloseIconVisible = !author.isGroup
                    groups.isCloseIconVisible = author.isGroup
                    if (!author.isGroup) {
                        groups.text = getString(R.string.share_add_common_header_groups)
                        users.text = author.name
                        users.setOnCloseIconClickListener { presenter.clearAuthor() }
                    } else {
                        users.text = getString(R.string.share_add_common_header_users)
                        groups.text = author.name
                        groups.setOnCloseIconClickListener { presenter.clearAuthor() }
                    }
                } else {
                    setAuthorChipsDefault()
                }
            }
        }
    }

    private fun setAuthorChipsDefault() {
        viewBinding?.let { binding ->
            binding.users.isChecked = false
            binding.groups.isChecked = false
            binding.users.isCloseIconVisible = false
            binding.groups.isCloseIconVisible = false
            binding.users.text = getString(R.string.share_add_common_header_users)
            binding.groups.text = getString(R.string.share_add_common_header_groups)
        }
    }

    private fun initChipGroup() {
        viewBinding?.types?.let { typesGroup ->
            typesGroup.findViewById<Chip>(presenter.filterType.checkedId)?.isChecked = true
            typesGroup.setOnCheckedChangeListener { _, checkedId ->
                presenter.filterType = getFilterType(checkedId)
                presenter.update()
            }
        }
    }

    private fun showAuthorFragment(selectedId: String, isGroups: Boolean) {
        FragmentUtils.showFragment(
            parentFragmentManager,
            FilterAuthorFragment.newInstance(selectedId, isGroups),
            R.id.frame_container,
            FilterAuthorFragment.TAG
        )
        setFragmentResultListener(FilterAuthorFragment.REQUEST_KEY_AUTHOR) { _, bundle ->
            presenter.filterAuthor =
                FilterAuthor.toObject(bundle.getString(FilterAuthorFragment.BUNDLE_KEY_AUTHOR))
            presenter.update()
            clearFragmentResultListener(FilterAuthorFragment.REQUEST_KEY_AUTHOR)
        }
    }

    fun resetFilters() {
        presenter.reset()
    }
}