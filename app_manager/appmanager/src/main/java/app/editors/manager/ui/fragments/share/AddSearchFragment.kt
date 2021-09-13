package app.editors.manager.ui.fragments.share

import android.content.Context
import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.DiffUtil
import app.editors.manager.R
import app.editors.manager.databinding.FragmentShareAddListSearchBinding
import app.editors.manager.mvp.models.explorer.Item
import app.editors.manager.mvp.models.models.ModelShareStack
import app.editors.manager.mvp.models.ui.GroupUi
import app.editors.manager.mvp.models.ui.UserUi
import app.editors.manager.mvp.presenters.share.AddPresenter
import app.editors.manager.mvp.views.share.AddView
import app.editors.manager.ui.activities.main.MainActivity.Companion.show
import app.editors.manager.ui.activities.main.ShareActivity
import app.editors.manager.ui.adapters.ShareAdapter
import app.editors.manager.ui.adapters.diffutilscallback.ShareSearchDiffUtilsCallback
import app.editors.manager.ui.adapters.holders.factory.ShareHolderFactory
import app.editors.manager.ui.fragments.base.ListFragment
import app.editors.manager.ui.views.custom.PlaceholderViews
import app.editors.manager.ui.views.custom.SharePanelViews
import lib.toolkit.base.ui.adapters.BaseAdapter
import lib.toolkit.base.ui.adapters.holder.ViewType
import moxy.presenter.InjectPresenter

class AddSearchFragment : ListFragment(), AddView, SearchView.OnQueryTextListener,
    BaseAdapter.OnItemClickListener, SharePanelViews.OnEventListener {

    @InjectPresenter
    lateinit var addPresenter: AddPresenter
    
    private var shareActivity: ShareActivity? = null
    private var shareAdapter: ShareAdapter? = null
    private var sharePanelViews: SharePanelViews? = null
    private var toolbarMenu: Menu? = null
    private var searchItem: MenuItem? = null
    private var searchView: SearchView? = null
    private var viewBinding: FragmentShareAddListSearchBinding? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        shareActivity = try {
            context as ShareActivity
        } catch (e: ClassCastException) {
            throw RuntimeException(
                ShareActivity::class.java.simpleName + " - must implement - " +
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
        viewBinding = FragmentShareAddListSearchBinding.inflate(inflater, container, false)
        fragmentListBinding = viewBinding?.fragmentShareAddList?.fragmentList
        return viewBinding?.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init(savedInstanceState)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        sharePanelViews?.popupDismiss()
        sharePanelViews?.unbind()
        viewBinding = null
        shareActivity = null
    }

    override fun onBackPressed(): Boolean {
        sharePanelViews?.let {
            if (it.popupDismiss() || it.hideMessageView()) {
                return true
            }
        }
        if (searchView?.query?.isNotEmpty()!!) {
            searchView?.setQuery("", true)
            return true
        }
        return super.onBackPressed()
    }

    override fun onCreateOptionsMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.share_add_search, menu)
        toolbarMenu = menu
        searchItem = menu.findItem(R.id.menu_share_add_search)
        searchView = (searchItem?.actionView as SearchView).apply {
            setOnQueryTextListener(this@AddSearchFragment)
            maxWidth = Int.MAX_VALUE
            isIconified = false
            addPresenter.updateSearchState()

            // Action on close search
            setOnCloseListener {
                if (searchView?.query?.isNotEmpty()!!) {
                    searchView?.setQuery("", true)
                    return@setOnCloseListener true
                }
                requireActivity().onBackPressed()
                false
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        item.isChecked = item.itemId == R.id.toolbar_item_search
        return super.onOptionsItemSelected(item)
    }

    override fun onRefresh() {
        searchView?.setQuery("", false)
        addPresenter.getCommons()
    }

    override fun onQueryTextSubmit(query: String) = false

    override fun onQueryTextChange(newText: String): Boolean {
        addPresenter.setSearchValue(newText)
        addPresenter.resetChecked()
        sharePanelViews?.setCount(0)
        sharePanelViews?.setAddButtonEnable(false)
        return false
    }

    override fun onUpdateSearch(list: MutableList<ViewType>?) {
        if (list == null) {
            setPlaceholder(true)
        }
    }

    override fun onItemClick(view: View, position: Int) {
        shareAdapter?.getItem(position).let { item ->
            when (item) {
                is UserUi -> item.isSelected = !item.isSelected
                is GroupUi -> item.isSelected = !item.isSelected
            }
        }
        shareAdapter?.notifyItemChanged(position)
        setCountChecked()
    }

    override fun onPanelAccessClick(accessCode: Int) {
        addPresenter.accessCode = accessCode
    }

    override fun onPanelResetClick() {
        addPresenter.resetChecked()
        sharePanelViews?.setCount(0)
        sharePanelViews?.setAddButtonEnable(false)
        shareAdapter?.notifyDataSetChanged()
    }

    override fun onPanelMessageClick(isShow: Boolean) {
        // Stub
    }

    override fun onPanelAddClick() {
        swipeRefreshLayout?.isRefreshing = true
        addPresenter.shareItem()
    }

    override fun onMessageInput(message: String) {
        addPresenter.setMessage(message)
    }

    override fun onError(message: String?) {
        swipeRefreshLayout?.isRefreshing = false
        message?.let { showSnackBar(it) }
        if (shareAdapter?.itemCount == 0) {
            placeholderViews?.setTemplatePlaceholder(PlaceholderViews.Type.CONNECTION)
        }
    }

    override fun onUnauthorized(message: String?) {
        requireActivity().finish()
        show(requireContext())
    }

    override fun onGetUsers(list: MutableList<ViewType>?) {
        // Stub
    }

    override fun onGetGroups(list: List<ViewType>) {
        // Stub
    }

    override fun onGetCommon(list: List<ViewType>) {
        setPlaceholder(list.isNotEmpty())
        swipeRefreshLayout?.isRefreshing = false
        shareAdapter?.setMode(BaseAdapter.Mode.COMMON)
        updateDiffUtils(list)
    }

    override fun onSuccessAdd() {
        ModelShareStack.getInstance().isRefresh = true
        showRootFragment()
    }

    override fun onSearchValue(value: String?) {
        value?.let {
            searchView?.setQuery(value, false)
        }
    }

    private fun updateDiffUtils(list: List<ViewType>) {
        val diffUtils = ShareSearchDiffUtilsCallback(list, shareAdapter?.itemsList!!)
        val result = DiffUtil.calculateDiff(diffUtils)
        shareAdapter?.set(list, result)
    }

    override fun onUpdateAvatar(user: UserUi) {
        shareAdapter?.let { adapter ->
            val position = adapter.updateItem(user)
            adapter.notifyItemChanged(position, ShareAdapter.PAYLOAD_AVATAR)
        }
    }

    private fun init(savedInstanceState: Bundle?) {
        setActionBarTitle(getString(R.string.share_title_search))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)
        shareActivity?.expandAppBar()
        getArgs()
        restoreViews(savedInstanceState)
        initViews()
    }

    private fun getArgs() {
        addPresenter.setItem((arguments?.getSerializable(TAG_ITEM) as Item))
    }

    private fun initViews() {
        viewBinding?.let { binding ->
            sharePanelViews = SharePanelViews(binding.sharePanelLayout.root, shareActivity!!).apply {
                    setOnEventListener(this@AddSearchFragment)
                    setAccessIcon(addPresenter.accessCode)
                }
//            binding.fragmentShareAddList.fragmentList.let { fragmentList ->
//                recyclerView = fragmentList.listOfItems
//                placeholderViews = PlaceholderViews(fragmentList.placeholderLayout.root)
//                swipeRefreshLayout = fragmentList.listSwipeRefresh
//                placeholderViews?.setViewForHide(fragmentList.listOfItems)
//            }
        }
        shareAdapter = ShareAdapter(ShareHolderFactory { view, position ->
            onItemClick(view, position)
        })
        shareAdapter?.setMode(BaseAdapter.Mode.COMMON)
        recyclerView?.adapter = shareAdapter
        setCountChecked()
    }

    private fun restoreViews(savedInstanceState: Bundle?) {
        savedInstanceState?.let {
            addPresenter.updateCommonSharedListState()
        } ?: run {
            addPresenter.getCommons()
        }
    }

    private fun setCountChecked() {
        sharePanelViews?.setCount(addPresenter.countChecked)
        sharePanelViews?.setAddButtonEnable(addPresenter.countChecked > 0)
    }

    private fun setPlaceholder(isEmpty: Boolean) {
        placeholderViews?.setTemplatePlaceholder(if (isEmpty)
            PlaceholderViews.Type.NONE else PlaceholderViews.Type.COMMON)
    }

    companion object {
        val TAG = AddSearchFragment::class.java.simpleName
        const val TAG_ITEM = "TAG_ITEM"

        fun newInstance(item: Item?): AddSearchFragment {
            return AddSearchFragment().apply {
                arguments = Bundle(1).apply {
                    putSerializable(TAG_ITEM, checkNotNull(item))
                }
            }
        }
    }
}