package app.editors.manager.ui.fragments.share

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import app.documents.core.network.manager.models.explorer.CloudFolder
import app.documents.core.network.manager.models.explorer.Item
import app.editors.manager.R
import app.editors.manager.databinding.FragmentShareAddPagerBinding
import app.editors.manager.mvp.models.models.ModelShareStack
import app.editors.manager.mvp.presenters.share.AddPresenter
import app.editors.manager.ui.activities.main.ShareActivity
import app.editors.manager.ui.fragments.base.BaseAppFragment
import app.editors.manager.ui.views.animation.HeightValueAnimator
import app.editors.manager.ui.views.custom.SharePanelViews
import app.editors.manager.ui.views.pager.ViewPagerAdapter
import lib.toolkit.base.managers.utils.capitalize

class AddPagerFragment : BaseAppFragment(), SharePanelViews.OnEventListener {

    private var modelShareStack: ModelShareStack? = null
    private var shareActivity: ShareActivity? = null
    private var sharePanelViews: SharePanelViews? = null
    private var heightValueAnimator: HeightValueAnimator? = null
    private var viewPagerAdapter: ViewPagerAdapter? = null
    private val inputItem: Item
        get() = arguments?.getSerializable(TAG_ITEM) as Item
    private val isRoom: Boolean
        get() = inputItem is CloudFolder && (inputItem as CloudFolder).isRoom
    private var viewBinding: FragmentShareAddPagerBinding? = null
    var selectionMenu: Menu? = null


    override fun onAttach(context: Context) {
        super.onAttach(context)
        shareActivity = try {
            context as ShareActivity
        } catch (e: ClassCastException) {
            throw RuntimeException(
                AddPagerFragment::class.java.simpleName + " - must implement - " +
                        ShareActivity::class.java.simpleName
            )
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewBinding = FragmentShareAddPagerBinding.inflate(inflater, container, false)
        return viewBinding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getArgs()
        init()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        showTabLayout(false)
        sharePanelViews?.popupDismiss()
        sharePanelViews?.unbind()
        viewBinding = null
    }

    override fun onBackPressed(): Boolean {
        sharePanelViews?.let {
            if (it.popupDismiss()) return true
            if (it.hideMessageView()) {
                shareActivity?.expandAppBar()
                return true
            }
        }
        return super.onBackPressed()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.share_add, menu)
        selectionMenu = menu.also {
            it.findItem(R.id.menu_share_deselect).isVisible = false
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val fragment = viewPagerAdapter?.getActiveFragment(viewBinding?.shareAddViewPager) as? AddFragment
        when (item.itemId) {
            R.id.menu_share_select_all -> {
                fragment?.setSelectedAll(true)
            }
            R.id.menu_share_deselect -> {
                fragment?.setSelectedAll(false)
            }
            R.id.menu_share_add_search -> {
                showFragment(AddSearchFragment.newInstance(inputItem), AddSearchFragment.TAG, false)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onPanelAccessClick(accessCode: Int) {
        modelShareStack?.accessCode = accessCode
    }

    override fun onPanelResetClick() {
        resetChecked()
        updateAdaptersFragments()
    }

    override fun onPanelMessageClick(isShow: Boolean) {
        if (isShow) {
            shareActivity?.collapseAppBar()
        } else {
            shareActivity?.expandAppBar()
        }
    }

    override fun onPanelAddClick() {
        requestAddFragments()
    }

    override fun onMessageInput(message: String) {
        setMessageAddFragments(message)
    }

    private fun init() {
        setActionBarTitle(getString(R.string.share_title_add))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)
        shareActivity?.expandAppBar()
        shareActivity?.let {
            heightValueAnimator = HeightValueAnimator(it.getTabLayout(), ANIMATION_DURATION)
        }
        showTabLayout(!isRoom)
        initViews()
    }

    private fun showTabLayout(isShow: Boolean) {
        if (isTablet) {
            shareActivity?.getTabLayout()?.visibility =
                if (isShow) View.VISIBLE else View.GONE
        } else {
            heightValueAnimator?.animate(isShow)
        }
    }

    private fun getArgs() {
        modelShareStack = ModelShareStack.getInstance()
    }

    private fun initViews() {
        viewPagerAdapter = ViewPagerAdapter(childFragmentManager, fragments)
        viewBinding?.let { binding ->
            binding.shareAddViewPager.addOnPageChangeListener(checkNotNull(viewPagerAdapter))
            binding.shareAddViewPager.adapter = viewPagerAdapter
            shareActivity?.getTabLayout()?.setupWithViewPager(binding.shareAddViewPager, true)
            sharePanelViews = SharePanelViews(binding.sharePanelLayout.root, inputItem).apply {
                setOnEventListener(this@AddPagerFragment)
                setAccessIcon(modelShareStack?.accessCode!!)
            }
        }
        setChecked()
    }

    private fun resetChecked() {
        modelShareStack?.resetChecked()
        sharePanelViews?.setCount(0)
        for (fragment in childFragmentManager.fragments) {
            (fragment as AddFragment).setSelectedAll(false)
        }
    }

    private fun updateAdaptersFragments() {
        for (fragment in childFragmentManager.fragments) {
            (fragment as AddFragment).updateAdapterState()
        }
    }

    private fun requestAddFragments() {
        for (fragment in childFragmentManager.fragments) {
            (fragment as AddFragment).addAccess()
            return
        }
    }

    private fun setMessageAddFragments(message: String) {
        for (fragment in childFragmentManager.fragments) {
            (fragment as AddFragment).setMessage(message)
        }
    }

    private val fragments: List<ViewPagerAdapter.Container>
        get() {
            if (inputItem is CloudFolder && (inputItem as CloudFolder).isRoom) {
                return listOf(
                    ViewPagerAdapter.Container(
                        AddFragment.newInstance(inputItem, AddPresenter.Type.Users),
                        getString(R.string.share_tab_users).capitalize()
                    )
                )
            } else {
                return listOf(
                    ViewPagerAdapter.Container(
                        AddFragment.newInstance(inputItem, AddPresenter.Type.Users),
                        getString(R.string.share_tab_users).capitalize()
                    ),

                    ViewPagerAdapter.Container(
                        AddFragment.newInstance(inputItem, AddPresenter.Type.Groups),
                        getString(R.string.share_tab_groups).capitalize()
                    )
                )
            }
        }

    fun setChecked() {
        modelShareStack?.countChecked?.let { countChecked ->
            sharePanelViews?.setCount(countChecked)
            sharePanelViews?.setAddButtonEnable(countChecked > 0)
        }
    }

    fun isActivePage(fragment: Fragment?): Boolean {
        return viewPagerAdapter?.isActiveFragment(viewBinding?.shareAddViewPager, fragment) == true
    }

    companion object {
        val TAG: String = AddPagerFragment::class.java.simpleName
        const val TAG_ITEM = "TAG_ITEM"
        const val ANIMATION_DURATION = 200

        fun newInstance(item: Item?): AddPagerFragment {
            return AddPagerFragment().apply {
                arguments = Bundle(1).apply {
                    putSerializable(TAG_ITEM, checkNotNull(item))
                }
            }
        }
    }
}