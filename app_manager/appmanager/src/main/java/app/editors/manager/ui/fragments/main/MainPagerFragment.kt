package app.editors.manager.ui.fragments.main

import android.Manifest
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import app.documents.core.model.cloud.isDocSpace
import app.documents.core.network.common.contracts.ApiContract
import app.documents.core.network.manager.models.explorer.Explorer
import app.editors.manager.BuildConfig
import app.editors.manager.R
import app.editors.manager.app.accountOnline
import app.editors.manager.app.appComponent
import app.editors.manager.databinding.FragmentMainPagerBinding
import app.editors.manager.managers.tools.PreferenceTool
import app.editors.manager.mvp.models.models.OpenDataModel
import app.editors.manager.mvp.presenters.main.MainPagerPresenter
import app.editors.manager.mvp.views.main.MainPagerView
import app.editors.manager.ui.activities.main.ActionButtonFragment
import app.editors.manager.ui.activities.main.IMainActivity
import app.editors.manager.ui.activities.main.MainActivity
import app.editors.manager.ui.fragments.base.BaseAppFragment
import app.editors.manager.ui.views.custom.PlaceholderViews
import app.editors.manager.ui.views.pager.ViewPagerAdapter
import app.editors.manager.ui.views.pager.ViewPagerAdapter.Container
import lib.toolkit.base.managers.utils.RequestPermission
import lib.toolkit.base.managers.utils.UiUtils
import lib.toolkit.base.managers.utils.clearIntent
import moxy.presenter.InjectPresenter

interface IMainPagerFragment {

    fun setPagerPosition(sectionType: Int, onPageChanged: () -> Unit = {})
}

class MainPagerFragment : BaseAppFragment(), ActionButtonFragment, MainPagerView, View.OnClickListener,
    IMainPagerFragment {

    companion object {

        val TAG: String = MainPagerFragment::class.java.simpleName

        private const val TAG_SELECTED_PAGE = "TAG_SELECTED_PAGE"
        private const val TAG_VISIBLE = "TAG_VISIBLE"
        private const val TAG_SCROLL = "TAG_SCROLL"
        private const val TAG_TITLES = "TAG_TITLES"
        private const val TAG_TYPE = "TAG_TYPE"
        private const val OFFSCREEN_COUNT = 5

        fun newInstance(): MainPagerFragment {
            return MainPagerFragment()
        }
    }

    @InjectPresenter
    lateinit var presenter: MainPagerPresenter

    private var adapter: AdapterForPages? = null
    private var activity: IMainActivity? = null
    private var isScroll = true
    private var isVisibleRoot = false
    private var selectedPage = 0

    private var viewBinding: FragmentMainPagerBinding? = null
    private var placeholderViews: PlaceholderViews? = null

    private var preferenceTool: PreferenceTool? = null

    private var tabTile: ArrayList<String>? = null
    private var type: ArrayList<Int>? = null

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
        checkNotificationPermission()
    }

    private var isFirstResume = true

    override fun onResume() {
        super.onResume()
        if (isFirstResume) {
            isFirstResume = false
        } else {
            viewBinding?.mainViewPager?.post {
//                activity?.showAccount(true)
                activeFragment?.onResume()
            }
        }
    }

    private fun checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            RequestPermission(requireActivity().activityResultRegistry, {
                // TODO set notification flag
            }, Manifest.permission.POST_NOTIFICATIONS).request()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putBoolean(TAG_VISIBLE, isVisibleRoot)
        outState.putBoolean(TAG_SCROLL, isScroll)
        outState.putInt(TAG_SELECTED_PAGE, selectedPage)
        outState.putStringArrayList(TAG_TITLES, tabTile)
        outState.putIntegerArrayList(TAG_TYPE, type)
        super.onSaveInstanceState(outState)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        activity = null
        viewBinding = null
    }

    private fun init(savedInstanceState: Bundle?) {
        if (savedInstanceState != null) {
            restoreStates(savedInstanceState)
        } else {
            placeholderViews = PlaceholderViews(viewBinding?.placeholderLayout?.root)
            placeholderViews?.setTemplatePlaceholder(PlaceholderViews.Type.LOAD)
            checkBundle()
        }
    }

    @Suppress("JSON_FORMAT_REDUNDANT")
    private fun checkBundle() {
        val bundle = requireActivity().intent?.extras
        var data = requireActivity().intent?.data
        if (bundle != null && bundle.containsKey("data")) {
            val model = bundle.getString("data")
            data = Uri.parse("${BuildConfig.PUSH_SCHEME}://openfile?data=${model}&push=true")
        }
        presenter.getState(data)
    }

    private fun restoreStates(savedInstanceState: Bundle) {
        if (savedInstanceState.containsKey(TAG_VISIBLE)) {
            isVisibleRoot = savedInstanceState.getBoolean(TAG_VISIBLE)
        }
        if (savedInstanceState.containsKey(TAG_SCROLL)) {
            isScroll = savedInstanceState.getBoolean(TAG_SCROLL)
        }
        if (savedInstanceState.containsKey(TAG_SELECTED_PAGE)) {
            selectedPage = savedInstanceState.getInt(TAG_SELECTED_PAGE)
        }

        if (savedInstanceState.containsKey(TAG_TITLES) && savedInstanceState.containsKey(TAG_TYPE)) {
            tabTile = savedInstanceState.getStringArrayList(TAG_TITLES)
            type = savedInstanceState.getIntegerArrayList(TAG_TYPE)

            val fragments: MutableList<MainPagerContainer> = mutableListOf()
            childFragmentManager.fragments.forEachIndexed { index, fragment ->
                if (fragment is DocsBaseFragment) {
                    fragments.add(MainPagerContainer(fragment, tabTile?.get(index) ?: "", type?.get(index) ?: 0))
                }
            }
            setAdapter(fragments, true)
        } else {
            checkBundle()
            return
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
        if (!isVisible) return
        isVisibleRoot = isRoot
        activity?.setAppBarStates(isVisibleRoot)
        viewBinding?.mainViewPager?.post {
            viewBinding?.appBarTabs?.isVisible = isVisibleRoot
        }
    }

    fun setExpandToolbar() {
        //        mMainActivity.expandToolBar();
    }

    fun setVisibilityActionButton(isShow: Boolean) {
        if (!isVisible) return
        activity?.showActionButton(isShow)
    }


    fun setAccountEnable(isEnable: Boolean) {
        if (!isVisible) return
        activity?.showAccount(isEnable)
    }

    fun setVisibleTabs(isVisible: Boolean) {
        activity?.setAppBarStates(isVisible)
    }

    override fun showActionDialog() {
        (activeFragment as? DocsBaseFragment)?.showActionDialog()
    }

    override fun onRender(sections: List<Explorer>?) {
        type = arrayListOf()
        tabTile = arrayListOf()
        sections?.let {
            val fragments = sections.mapNotNull { section ->
                type?.add(section.current.rootFolderType)
                when (val folderType = section.current.rootFolderType) {
                    ApiContract.SectionType.CLOUD_PRIVATE_ROOM, ApiContract.SectionType.CLOUD_RECENT -> null
                    else -> {
                        tabTile?.add(getTabTitle(folderType))
                        MainPagerContainer(
                            fragment = when (folderType) {
                                ApiContract.SectionType.CLOUD_TRASH,
                                ApiContract.SectionType.CLOUD_ARCHIVE_ROOM -> {
                                    DocsTrashFragment.newInstance(folderType, section.current.id)
                                }

                                ApiContract.SectionType.CLOUD_VIRTUAL_ROOM -> {
                                    DocsRoomFragment.newInstance(folderType, section.current.id)
                                }

                                else -> {
                                    DocsCloudFragment.newInstance(folderType, section.current.id)
                                }
                            },
                            title = getTabTitle(folderType),
                            sectionType = folderType
                        )
                    }
                }
            }
            setAdapter(fragments)
        }
    }

    override fun onFinishRequest() {
        if (placeholderViews?.type == PlaceholderViews.Type.PAYMENT_REQUIRED) {
            activity?.showActionButton(false)
        } else {
            placeholderViews?.setTemplatePlaceholder(PlaceholderViews.Type.NONE)
        }
    }

    override fun onError(message: String?) {
        if (message == getString(R.string.errors_client_payment_required)) {
            activity?.showActionButton(false)
            placeholderViews?.setTemplatePlaceholder(PlaceholderViews.Type.PAYMENT_REQUIRED) {
                context?.accountOnline?.portal?.urlWithScheme?.let(::showUrlInBrowser)
            }
        } else {
            message?.let { showSnackBar(it) }
            (requireActivity() as? MainActivity)?.onUnauthorized(message)
        }
    }

    override fun onError(@StringRes res: Int) {
        showSnackBar(getString(res))
        requireActivity().intent.data = null
    }

    private fun setAdapter(fragments: List<MainPagerContainer>, isRestore: Boolean = false) {
        if (fragments.isEmpty()) {
            viewBinding?.appBarTabs?.isVisible = false
            return
        }

        adapter = AdapterForPages(childFragmentManager, fragments)
        viewBinding?.mainViewPager?.offscreenPageLimit = OFFSCREEN_COUNT
        viewBinding?.mainViewPager?.adapter = adapter
        adapter?.let {
            viewBinding?.mainViewPager?.addOnPageChangeListener(it)
        }
        viewBinding?.appBarTabs?.setupWithViewPager(viewBinding?.mainViewPager, true)
        setToolbarState(true)

        if (isRestore) {
            viewBinding?.mainViewPager?.currentItem = selectedPage
        } else {
            if (context?.accountOnline.isDocSpace) {
                viewBinding?.mainViewPager?.post {
                    viewBinding?.mainViewPager?.currentItem =
                        fragments.indexOf(fragments.find { it.mFragment is DocsRoomFragment })
                }
            }
        }
    }

    override fun setFileData(fileData: String) {
        viewBinding?.root?.postDelayed({
            childFragmentManager.fragments.find { it is DocsRoomFragment || it is DocsCloudFragment }?.let { fragment ->
                if (fragment.isAdded) {
                    (fragment as DocsCloudFragment).setFileData(fileData)
                    requireActivity().intent.clearIntent()
                }
            }
        }, 500)
    }

    override fun onSwitchAccount(data: OpenDataModel, isToken: Boolean) {
        UiUtils.showQuestionDialog(
            context = requireContext(),
            title = getString(R.string.switch_account_title),
            description = getString(R.string.switch_account_description, data.portal),
            acceptListener = {
                activity?.showAccountsActivity(isToken)
            },
            cancelListener = {
                presenter.onRemoveFileData()
            },
            acceptTitle = getString(R.string.switch_account_open_project_file)
        )
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
        get() = runCatching { adapter?.getActiveFragment(viewBinding?.mainViewPager) }.getOrNull()

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

    override fun setPagerPosition(
        sectionType: Int,
        onPageChanged: () -> Unit
    ) {
        adapter?.let { adapter ->
            val index = adapter.fragmentList.indexOfFirst { it.sectionType == sectionType }
            if (index > -1) {
                viewBinding?.mainViewPager?.let { pager ->
                    pager.currentItem = index
                    pager.postDelayed(onPageChanged, 500)
                }
            }
        }
    }

    /*
     * Adapter and page change listener
     * */
    private data class MainPagerContainer(
        val fragment: Fragment,
        val title: String,
        val sectionType: Int
    ) : Container(fragment, title)

    private inner class AdapterForPages(
        manager: FragmentManager,
        val fragmentList: List<MainPagerContainer>
    ) : ViewPagerAdapter(manager, fragmentList) {

        override fun onPageSelected(position: Int) {
            super.onPageSelected(position)
            activity?.showActionButton(false)
            this@MainPagerFragment.selectedPage = selectedPage
            (getActiveFragment(viewBinding?.mainViewPager) as DocsCloudFragment).onScrollPage()
        }
    }
}