package app.editors.manager.ui.fragments.main

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import app.editors.manager.R
import app.editors.manager.app.appComponent
import app.editors.manager.databinding.FragmentMainPagerBinding
import app.editors.manager.managers.tools.PreferenceTool
import app.editors.manager.mvp.models.explorer.Explorer
import app.editors.manager.mvp.presenters.main.MainPagerPresenter
import app.editors.manager.mvp.presenters.main.MainPagerState
import app.editors.manager.mvp.views.main.MainPagerView
import app.editors.manager.ui.activities.main.ActionButtonFragment
import app.editors.manager.ui.activities.main.IMainActivity
import app.editors.manager.ui.activities.main.MainActivity
import app.editors.manager.ui.fragments.base.BaseAppFragment
import app.editors.manager.ui.fragments.factory.TabFragmentFactory
import app.editors.manager.ui.views.custom.PlaceholderViews
import app.editors.manager.ui.views.pager.ViewPagerAdapter
import lib.toolkit.base.managers.utils.TabFragmentDictionary
import moxy.presenter.InjectPresenter
import moxy.presenter.ProvidePresenter
import java.util.*

class MainPagerFragment : BaseAppFragment(), ActionButtonFragment, MainPagerView {

    companion object {

        val TAG: String = MainPagerFragment::class.java.simpleName

        private const val TAG_VISIBLE = "TAG_VISIBLE"
        private const val TAG_SCROLL = "TAG_SCROLL"
        private const val OFFSCREEN_COUNT = 5

        const val KEY_FILE_DATA = "KEY_FILE_DATA"
        const val KEY_ACCOUNT = "KEY_ACCOUNT"

        fun newInstance(accountData: String, fileData: String?): MainPagerFragment {
            return MainPagerFragment().apply {
                arguments = Bundle(2).apply {
                    putString(KEY_ACCOUNT, accountData)
                    putString(KEY_FILE_DATA, fileData)
                }
            }
        }
    }

    @InjectPresenter
    lateinit var presenter: MainPagerPresenter
    @ProvidePresenter
    fun providePresenter() = MainPagerPresenter(arguments?.getString(KEY_ACCOUNT))

    private var adapter: ViewPagerAdapter? = null
    private var activity: IMainActivity? = null
    private var isScroll = true
    private var isVisibleRoot = true

    private var viewBinding: FragmentMainPagerBinding? = null
    private var placeholderViews: PlaceholderViews? = null

    private var preferenceTool: PreferenceTool? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            if (context is IMainActivity) {
                activity = context
            }
            preferenceTool = requireContext().appComponent.preference
        } catch (e: ClassCastException) {
            throw RuntimeException(
                MainPagerFragment::class.java.simpleName + " - must implement - " +
                        MainActivity::class.java.simpleName
            )
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
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
        placeholderViews = PlaceholderViews(viewBinding?.placeholderLayout?.placeholderLayout)
        placeholderViews?.setTemplatePlaceholder(PlaceholderViews.Type.LOAD)
        presenter.getState(requireActivity().intent.data)
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
        return childFragmentManager.findFragmentByTag("android:switcher:"
                + viewBinding?.mainViewPager?.id + ":"
                + viewBinding?.mainViewPager?.currentItem)?.equals(fragment) == true
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
                getVisitorFragments(state.account)
            }
            is MainPagerState.PersonalState -> {
                getPersonalFragments(state.account, state.version)
            }
            is MainPagerState.CloudState -> {
                getCloudFragments(state.account, state.version)
            }
        }
        arguments?.getString(KEY_FILE_DATA)?.let {
            childFragmentManager.fragments.find { it is DocsProjectsFragment }?.let {
                viewBinding?.mainViewPager?.post {
                    adapter?.let {
                        viewBinding?.mainViewPager?.currentItem =
                            it.getByTitle(getString(R.string.main_pager_docs_projects))
                    }
                }
            }
        }
    }

    override fun onRender(stringAccount: String, sections: List<Explorer>?) {
        val fragments = arrayListOf<ViewPagerAdapter.Container>()
        sections?.let {
            for(section in sections) {
                if(TabFragmentDictionary.Recent.contains(section.current?.title)) {
                    continue
                }
                fragments.add(
                    ViewPagerAdapter.Container(
                        section.current?.title?.let { TabFragmentFactory.getSectionFragment(it, stringAccount) },
                        context?.let { section.current?.title?.let { it1 ->
                            TabFragmentFactory(it).getTabTitle(
                                it1
                            )
                        } }
                    )
                )
            }
            if (preferenceTool?.isProjectDisable?.not() == true) {
                fragments.add(
                    ViewPagerAdapter.Container(
                        DocsProjectsFragment.newInstance(stringAccount, arguments?.getString(KEY_FILE_DATA)),
                        getString(R.string.main_pager_docs_projects)
                    )
                )
            }
            val correctOrderTabs = setCorrectOrder(fragments)
            setAdapter(correctOrderTabs)
            arguments?.getString(KEY_FILE_DATA)?.let {
                childFragmentManager.fragments.find { it is DocsProjectsFragment }?.let {
                    viewBinding?.mainViewPager?.post {
                        viewBinding?.mainViewPager?.currentItem =
                            adapter?.getByTitle(getString(R.string.main_pager_docs_projects)) ?: -1
                    }
                }
            }
        }
    }

    private fun setCorrectOrder(tabs: ArrayList<ViewPagerAdapter.Container>): ArrayList<ViewPagerAdapter.Container?> {
        val tabOrder = arrayListOf<ViewPagerAdapter.Container?>(null, null, null, null, null, null)
        for(tab in tabs) {
            when {
                TabFragmentDictionary.My.contains(tab.mTitle) -> tabOrder[0] = tab
                TabFragmentDictionary.Shared.contains(tab.mTitle) -> tabOrder[1] = tab
                TabFragmentDictionary.Favorites.contains(tab.mTitle) -> tabOrder[2] = tab
                TabFragmentDictionary.Common.contains(tab.mTitle) -> tabOrder[3] = tab
                TabFragmentDictionary.Trash.contains(tab.mTitle) -> tabOrder[5] = tab
                else -> tabOrder[4] = tab
            }
        }
        return tabOrder
    }

    override fun onFinishRequest() {
        placeholderViews?.setTemplatePlaceholder(PlaceholderViews.Type.NONE)
    }

    override fun onError(message: String?) {
        message?.let { showSnackBar(it).show() }
    }

    override fun onError(@StringRes res: Int) {
        showSnackBar(getString(res))
        requireActivity().intent.data = null
    }

    private fun getCloudFragments(stringAccount: String, serverVersion: Int) {
        val fragments = arrayListOf<ViewPagerAdapter.Container?>()
        fragments.add(
            ViewPagerAdapter.Container(
                DocsMyFragment.newInstance(stringAccount), getString(
                    R.string
                        .main_pager_docs_my
                )
            )
        )
        fragments.add(
            ViewPagerAdapter.Container(
                DocsShareFragment.newInstance(stringAccount),
                getString(R.string.main_pager_docs_share)
            )
        )
        if (serverVersion >= 11) {
            fragments.add(
                ViewPagerAdapter.Container(
                    DocsFavoritesFragment.newInstance(stringAccount),
                    getString(R.string.main_pager_docs_favorites)
                )
            )
        }
        fragments.add(
            ViewPagerAdapter.Container(
                DocsCommonFragment.newInstance(stringAccount),
                getString(R.string.main_pager_docs_common)
            )
        )
        fragments.add(
            ViewPagerAdapter.Container(
                DocsProjectsFragment.newInstance(stringAccount, arguments?.getString(KEY_FILE_DATA)),
                getString(R.string.main_pager_docs_projects)
            )
        )
        fragments.add(
            ViewPagerAdapter.Container(
                DocsTrashFragment.newInstance(stringAccount),
                getString(R.string.main_pager_docs_trash)
            )
        )
        setAdapter(fragments)
    }

    private fun getVisitorFragments(stringAccount: String) {
        val fragments = arrayListOf<ViewPagerAdapter.Container?>()
        fragments.add(
            ViewPagerAdapter.Container(
                DocsShareFragment.newInstance(stringAccount),
                getString(R.string.main_pager_docs_share)
            )
        )
        fragments.add(
            ViewPagerAdapter.Container(
                DocsCommonFragment.newInstance(stringAccount),
                getString(R.string.main_pager_docs_common)
            )
        )
        if (preferenceTool?.isProjectDisable?.not() == true) {
            fragments.add(
                ViewPagerAdapter.Container(
                    DocsProjectsFragment.newInstance(stringAccount, arguments?.getString(KEY_FILE_DATA)),
                    getString(R.string.main_pager_docs_projects)
                )
            )
        }
        setAdapter(fragments)
    }

    private fun getPersonalFragments(stringAccount: String, serverVersion: Int) {
        val fragments = arrayListOf<ViewPagerAdapter.Container?>()
        fragments.add(
            ViewPagerAdapter.Container(
                DocsMyFragment.newInstance(stringAccount), getString(
                    R.string
                        .main_pager_docs_my
                )
            )
        )
        if (serverVersion >= 11 && preferenceTool?.isFavoritesEnabled == true) {
            fragments.add(
                ViewPagerAdapter.Container(
                    DocsFavoritesFragment.newInstance(stringAccount),
                    getString(R.string.main_pager_docs_favorites)
                )
            )
        }
        fragments.add(
            ViewPagerAdapter.Container(
                DocsTrashFragment.newInstance(stringAccount),
                getString(R.string.main_pager_docs_trash)
            )
        )
        setAdapter(fragments)
    }

    private fun setAdapter(fragments: ArrayList<ViewPagerAdapter.Container?>) {
        adapter = AdapterForPages(childFragmentManager, fragments)
        viewBinding?.mainViewPager?.offscreenPageLimit = OFFSCREEN_COUNT
        viewBinding?.mainViewPager?.adapter = adapter
        adapter?.let {
            viewBinding?.mainViewPager?.addOnPageChangeListener(it)
        }
        activity?.getTabLayout()?.setupWithViewPager(viewBinding?.mainViewPager, true)
    }

    override fun setFileData(fileData: String) {
        childFragmentManager.fragments.find { it is DocsProjectsFragment }?.let { it ->
            adapter?.let {
                viewBinding?.mainViewPager?.currentItem = it.getByTitle(getString(R.string.main_pager_docs_projects))
            }
            (it as DocsProjectsFragment).setFileData(fileData)
            requireActivity().intent.data = null
        }
    }

    fun isRoot(): Boolean {
        return (activeFragment as DocsCloudFragment).isRoot
    }

    val position: Int?
        get() = adapter?.selectedPage

    private val activeFragment: Fragment?
        get() = adapter?.getActiveFragment(viewBinding?.mainViewPager)

    /*
     * Adapter and page change listener
     * */
    private inner class AdapterForPages(
        manager: FragmentManager,
        fragmentList: List<Container?>
    ) : ViewPagerAdapter(manager, fragmentList) {

        override fun onPageSelected(position: Int) {
            super.onPageSelected(position)
            activity?.showActionButton(false)
            (getActiveFragment(viewBinding?.mainViewPager) as DocsCloudFragment).onScrollPage()
        }
    }
}