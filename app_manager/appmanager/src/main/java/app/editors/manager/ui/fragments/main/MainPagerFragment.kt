package app.editors.manager.ui.fragments.main

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import app.editors.manager.R
import app.editors.manager.app.App
import app.editors.manager.databinding.FragmentMainPagerBinding
import app.editors.manager.managers.tools.PreferenceTool
import app.editors.manager.mvp.presenters.main.MainPagerPresenter
import app.editors.manager.mvp.presenters.main.MainPagerState
import app.editors.manager.mvp.views.main.MainPagerView
import app.editors.manager.ui.activities.main.ActionButtonFragment
import app.editors.manager.ui.activities.main.IMainActivity
import app.editors.manager.ui.activities.main.MainActivity
import app.editors.manager.ui.fragments.base.BaseAppFragment
import app.editors.manager.ui.views.pager.ViewPagerAdapter
import moxy.presenter.InjectPresenter
import java.util.*

class MainPagerFragment : BaseAppFragment(), ActionButtonFragment, MainPagerView {

    companion object {

        val TAG: String = MainPagerFragment::class.java.simpleName

        private const val TAG_VISIBLE = "TAG_VISIBLE"
        private const val TAG_SCROLL = "TAG_SCROLL"
        private const val OFFSCREEN_COUNT = 5

        const val KEY_FILE_DATA = "KEY_FILE_DATA"

        fun newInstance(fileData: String?): MainPagerFragment {
            return MainPagerFragment().apply {
                arguments = Bundle().apply {
                    putString(KEY_FILE_DATA, fileData)
                }
            }
        }
    }

    @InjectPresenter
    lateinit var presenter: MainPagerPresenter

    private lateinit var adapter: ViewPagerAdapter
    private var activity: IMainActivity? = null
    private var isScroll = true
    private var isVisibleRoot = true

    private var viewBinding: FragmentMainPagerBinding? = null

    var preferenceTool: PreferenceTool? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            if (context is IMainActivity) {
                activity = context
            }
            preferenceTool = App.getApp().appComponent.preference
        } catch (e: ClassCastException) {
            throw RuntimeException(
                MainPagerFragment::class.java.simpleName + " - must implement - " +
                        MainActivity::class.java.simpleName
            )
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        viewBinding = FragmentMainPagerBinding.inflate(inflater, container, false)
        return viewBinding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init(savedInstanceState)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putBoolean(TAG_VISIBLE, isVisibleRoot)
        outState.putBoolean(TAG_SCROLL, isScroll)
        super.onSaveInstanceState(outState)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        activity?.getTabLayout()?.setupWithViewPager(null)
        activity = null
        viewBinding = null
    }

    private fun init(savedInstanceState: Bundle?) {
        restoreStates(savedInstanceState)
        presenter.getState()
    }

    private fun restoreStates(savedInstanceState: Bundle?) {
        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(TAG_VISIBLE)) {
                isVisibleRoot = savedInstanceState.getBoolean(TAG_VISIBLE)
            }
            if (savedInstanceState.containsKey(TAG_SCROLL)) {
                isScroll = savedInstanceState.getBoolean(TAG_SCROLL)
            }
        }
    }

    fun isActivePage(fragment: Fragment?): Boolean {
        return adapter.isActiveFragment(viewBinding?.mainViewPager, fragment)
    }

    fun setScrollViewPager(isScroll: Boolean) {
        this.isScroll = isScroll
        viewBinding?.mainViewPager?.isPaging = this.isScroll
    }

    fun setToolbarState(isRoot: Boolean) {
        isVisibleRoot = isRoot
        activity?.setAppBarStates(isVisibleRoot)
    }

    fun setExpandToolbar() {
//        mMainActivity.expandToolBar();
    }

    fun setVisibilityActionButton(isShow: Boolean) {
        activity?.showActionButton(isShow)
    }


    fun setAccountEnable(isEnable: Boolean) {
        activity?.showAccount(isEnable)
    }

    fun setVisibleTabs(isVisible: Boolean) {
        activity?.setAppBarStates(isVisible)
    }

    override fun showActionDialog() {
        (activeFragment as DocsBaseFragment).showActionDialog()
    }

    override fun onRender(state: MainPagerState) {
        when (state) {
            is MainPagerState.VisitorState -> {
                getVisitorFragments()
            }
            is MainPagerState.PersonalState -> {
                getPersonalFragments(state.version)
            }
            is MainPagerState.CloudState -> {
                getCloudFragments(state.version)
            }
        }
        arguments?.getString(KEY_FILE_DATA)?.let {
            childFragmentManager.fragments.find { it is DocsProjectsFragment }?.let {
                viewBinding?.mainViewPager?.post {
                    viewBinding?.mainViewPager?.currentItem = adapter.getByTitle(getString(R.string.main_pager_docs_projects))
                }
            }
        }
    }

    private fun getCloudFragments(serverVersion: Int) {
        val fragments = arrayListOf<ViewPagerAdapter.Container>()
        fragments.add(ViewPagerAdapter.Container(DocsMyFragment.newInstance(), getString(R.string.main_pager_docs_my)))
        fragments.add(
            ViewPagerAdapter.Container(
                DocsShareFragment.newInstance(),
                getString(R.string.main_pager_docs_share)
            )
        )
        if (serverVersion >= 11 && preferenceTool?.isFavoritesEnabled == true) {
            fragments.add(
                ViewPagerAdapter.Container(
                    DocsFavoritesFragment.newInstance(),
                    getString(R.string.main_pager_docs_favorites)
                )
            )
        }
        fragments.add(
            ViewPagerAdapter.Container(
                DocsCommonFragment.newInstance(),
                getString(R.string.main_pager_docs_common)
            )
        )
        fragments.add(
            ViewPagerAdapter.Container(
                DocsProjectsFragment.newInstance(arguments?.getString(KEY_FILE_DATA)),
                getString(R.string.main_pager_docs_projects)
            )
        )
        fragments.add(
            ViewPagerAdapter.Container(
                DocsTrashFragment.newInstance(),
                getString(R.string.main_pager_docs_trash)
            )
        )
        setAdapter(fragments)
    }

    private fun getVisitorFragments() {
        val fragments = arrayListOf<ViewPagerAdapter.Container>()
        fragments.add(
            ViewPagerAdapter.Container(
                DocsShareFragment.newInstance(),
                getString(R.string.main_pager_docs_share)
            )
        )
        fragments.add(
            ViewPagerAdapter.Container(
                DocsCommonFragment.newInstance(),
                getString(R.string.main_pager_docs_common)
            )
        )
        fragments.add(
            ViewPagerAdapter.Container(
                DocsProjectsFragment.newInstance(arguments?.getString(KEY_FILE_DATA)),
                getString(R.string.main_pager_docs_projects)
            )
        )
        setAdapter(fragments)
    }

    private fun getPersonalFragments(serverVersion: Int) {
        val fragments = arrayListOf<ViewPagerAdapter.Container>()
        fragments.add(ViewPagerAdapter.Container(DocsMyFragment.newInstance(), getString(R.string.main_pager_docs_my)))
        if (serverVersion >= 11 && preferenceTool?.isFavoritesEnabled == true) {
            fragments.add(
                ViewPagerAdapter.Container(
                    DocsFavoritesFragment.newInstance(),
                    getString(R.string.main_pager_docs_favorites)
                )
            )
        }
        fragments.add(
            ViewPagerAdapter.Container(
                DocsTrashFragment.newInstance(),
                getString(R.string.main_pager_docs_trash)
            )
        )
        setAdapter(fragments)
    }

    private fun setAdapter(fragments: ArrayList<ViewPagerAdapter.Container>) {
        adapter = AdapterForPages(childFragmentManager, fragments)
        viewBinding?.mainViewPager?.offscreenPageLimit = OFFSCREEN_COUNT
        viewBinding?.mainViewPager?.adapter = adapter
        viewBinding?.mainViewPager?.addOnPageChangeListener(adapter)
        activity?.getTabLayout()?.setupWithViewPager(viewBinding?.mainViewPager, true)
    }

    fun setFileData(fileData: String) {
        childFragmentManager.fragments.find { it is DocsProjectsFragment }?.let {
            (it as DocsProjectsFragment).setFileData(fileData)
            viewBinding?.mainViewPager?.setCurrentItem(adapter.getByTitle(getString(R.string.main_pager_docs_projects)))
        }
    }

    val position: Int
        get() = adapter.selectedPage

    private val activeFragment: Fragment?
        get() = adapter.getActiveFragment(viewBinding?.mainViewPager)

    /*
     * Adapter and page change listener
     * */
    private inner class AdapterForPages(
        manager: FragmentManager,
        fragmentList: List<Container?>
    ) : ViewPagerAdapter(manager, fragmentList) {

        override fun onPageSelected(position: Int) {
            super.onPageSelected(position)
            activity?.showActionButton(true)
            (getActiveFragment(viewBinding?.mainViewPager) as DocsCloudFragment).onScrollPage()
        }
    }
}