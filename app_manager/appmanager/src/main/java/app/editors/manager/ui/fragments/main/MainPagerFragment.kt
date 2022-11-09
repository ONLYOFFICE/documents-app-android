package app.editors.manager.ui.fragments.main

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import app.documents.core.network.ApiContract
import app.editors.manager.R
import app.editors.manager.app.appComponent
import app.editors.manager.databinding.FragmentMainPagerBinding
import app.editors.manager.managers.tools.PreferenceTool
import app.editors.manager.mvp.models.explorer.Explorer
import app.editors.manager.mvp.presenters.main.MainPagerPresenter
import app.editors.manager.mvp.views.main.MainPagerView
import app.editors.manager.ui.activities.main.ActionButtonFragment
import app.editors.manager.ui.activities.main.IMainActivity
import app.editors.manager.ui.activities.main.MainActivity
import app.editors.manager.ui.fragments.base.BaseAppFragment
import app.editors.manager.ui.views.custom.PlaceholderViews
import app.editors.manager.ui.views.pager.ViewPagerAdapter
import moxy.presenter.InjectPresenter
import moxy.presenter.ProvidePresenter

class MainPagerFragment : BaseAppFragment(), ActionButtonFragment, MainPagerView, View.OnClickListener {

    companion object {

        val TAG: String = MainPagerFragment::class.java.simpleName

        private const val TAG_SELECTED_PAGE = "TAG_SELECTED_PAGE"
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
    private var selectedPage = 0

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
        outState.putInt(TAG_SELECTED_PAGE, selectedPage)
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
        checkBundle()
//        presenter.getState(requireActivity().intent.data)
    }

    @Suppress("JSON_FORMAT_REDUNDANT")
    private fun checkBundle() {
        val bundle = requireActivity().intent.extras
        var data = requireActivity().intent.data
        if (bundle != null && bundle.containsKey("data")) {
            val model = bundle.getString("data")
            data = Uri.parse("oodocuments://openfile?data=${model}&push=true")
        }
        presenter.getState(data)
    }

    private fun restoreStates(savedInstanceState: Bundle?) {
        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(TAG_VISIBLE)) {
                isVisibleRoot = savedInstanceState.getBoolean(TAG_VISIBLE)
            }
            if (savedInstanceState.containsKey(TAG_SCROLL)) {
                isScroll = savedInstanceState.getBoolean(TAG_SCROLL)
            }
            if (savedInstanceState.containsKey(TAG_SELECTED_PAGE)) {
                selectedPage = savedInstanceState.getInt(TAG_SELECTED_PAGE)
            }
        }
    }

    fun isActivePage(fragment: Fragment?): Boolean {
        return adapter?.isActiveFragment(viewBinding?.mainViewPager, fragment) == true
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
        (activeFragment as? DocsBaseFragment)?.showActionDialog()
    }

    override fun onRender(stringAccount: String, sections: List<Explorer>?) {
        val fragments = arrayListOf<ViewPagerAdapter.Container>()
        sections?.let {
            for (section in sections) {
                when (section.current.rootFolderType) {
                    ApiContract.SectionType.CLOUD_PRIVATE_ROOM, ApiContract.SectionType.CLOUD_RECENT -> continue
                    else -> {
                        fragments.add(
                            ViewPagerAdapter.Container(
                                section.current?.rootFolderType?.let { folderType ->
                                    when (folderType) {
                                        ApiContract.SectionType.CLOUD_TRASH, ApiContract.SectionType.CLOUD_ARCHIVE_ROOM -> {
                                            DocsTrashFragment.newInstance(stringAccount, folderType, section.current?.id ?: "")
                                        }
                                        ApiContract.SectionType.CLOUD_VIRTUAL_ROOM -> {
                                            DocsRoomFragment.newInstance(stringAccount, folderType, section.current?.id ?: "")
                                        }
                                        else -> {
                                            DocsCloudFragment.newInstance(stringAccount, folderType, section.current?.id ?: "")
                                        }
                                    }
                                },
                                getTabTitle(section.current?.rootFolderType ?: -1)
                            )
                        )
                    }
                }
            }
            setAdapter(fragments)
        }
    }

    override fun onFinishRequest() {
        placeholderViews?.setTemplatePlaceholder(PlaceholderViews.Type.NONE)
    }

    override fun onError(message: String?) {
        message?.let { showSnackBar(it) }
        (requireActivity() as? MainActivity)?.onUnauthorized(message)
    }

    override fun onError(@StringRes res: Int) {
        showSnackBar(getString(res))
        requireActivity().intent.data = null
    }


    private fun setAdapter(fragments: List<ViewPagerAdapter.Container>) {
        adapter = AdapterForPages(childFragmentManager, fragments)
        viewBinding?.mainViewPager?.offscreenPageLimit = OFFSCREEN_COUNT
        viewBinding?.mainViewPager?.adapter = adapter
        adapter?.let {
            viewBinding?.mainViewPager?.addOnPageChangeListener(it)
        }
        activity?.getTabLayout()?.setupWithViewPager(viewBinding?.mainViewPager, true)
        adapter?.selectedPage = selectedPage
    }

    override fun setFileData(fileData: String) {
        viewBinding?.root?.postDelayed({
            childFragmentManager.fragments.find { it is DocsCloudFragment }?.let { fragment ->
                if (fragment.isAdded) {
                    (fragment as DocsCloudFragment).setFileData(fileData)
                    requireActivity().intent.data = null
                }
            }
        }, 1000)
    }

    override fun onOpenProjectFileError(@StringRes res: Int) {
        showSnackBarWithAction(res, R.string.switch_account_open_project_file, this)
    }

    fun isRoot(): Boolean {
        return if (activeFragment != null && activeFragment is DocsCloudFragment) {
            (activeFragment as DocsCloudFragment).isRoot
        } else {
            false
        }
    }

    val position: Int?
        get() = adapter?.selectedPage

    private val activeFragment: Fragment?
        get() = adapter?.getActiveFragment(viewBinding?.mainViewPager)

    override fun onClick(view: View?) {
        activity?.onSwitchAccount()
    }

    private fun getTabTitle(tab: Int): String =
        when (tab) {
            ApiContract.SectionType.CLOUD_COMMON -> requireContext().getString(R.string.main_pager_docs_common)
            ApiContract.SectionType.CLOUD_USER -> requireContext().getString(R.string.main_pager_docs_my)
            ApiContract.SectionType.CLOUD_FAVORITES -> requireContext().getString(R.string.main_pager_docs_favorites)
            ApiContract.SectionType.CLOUD_SHARE -> requireContext().getString(R.string.main_pager_docs_share)
            ApiContract.SectionType.CLOUD_TRASH -> requireContext().getString(R.string.main_pager_docs_trash)
            ApiContract.SectionType.CLOUD_PROJECTS -> requireContext().getString(R.string.main_pager_docs_projects)
            ApiContract.SectionType.CLOUD_VIRTUAL_ROOM -> requireContext().getString(R.string.main_pager_docs_virtual_room)
            ApiContract.SectionType.CLOUD_ARCHIVE_ROOM -> requireContext().getString(R.string.main_pager_docs_archive_room)
            else -> ""
        }

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
            this@MainPagerFragment.selectedPage = mSelectedPage
            (getActiveFragment(viewBinding?.mainViewPager) as DocsCloudFragment).onScrollPage()
        }
    }
}