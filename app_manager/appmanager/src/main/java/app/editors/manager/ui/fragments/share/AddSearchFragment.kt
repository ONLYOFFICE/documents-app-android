package app.editors.manager.ui.fragments.share

import android.content.Context
import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.SearchView
import app.editors.manager.R
import app.editors.manager.databinding.FragmentListBinding
import app.editors.manager.databinding.FragmentShareAddListSearchBinding
import app.editors.manager.mvp.models.explorer.Item
import app.editors.manager.mvp.models.models.ModelShareStack
import app.editors.manager.mvp.models.ui.GroupUi
import app.editors.manager.mvp.models.ui.UserUi
import app.editors.manager.mvp.presenters.share.AddPresenter
import app.editors.manager.mvp.views.share.AddView
import app.editors.manager.ui.activities.main.MainActivity.Companion.show
import app.editors.manager.ui.activities.main.ShareActivity
import app.editors.manager.ui.adapters.holders.factory.ShareAddHolderFactory
import app.editors.manager.ui.adapters.share.ShareAddAdapter
import app.editors.manager.ui.fragments.base.ListFragment
import app.editors.manager.ui.views.custom.PlaceholderViews
import app.editors.manager.ui.views.custom.SharePanelViews
import butterknife.ButterKnife
import lib.toolkit.base.managers.tools.ResourcesProvider
import lib.toolkit.base.ui.adapters.BaseAdapter
import lib.toolkit.base.ui.adapters.holder.ViewType
import moxy.presenter.InjectPresenter
import javax.inject.Inject

class AddSearchFragment : ListFragment(), AddView, SearchView.OnQueryTextListener,
    BaseAdapter.OnItemClickListener, SharePanelViews.OnEventListener {

    @InjectPresenter
    lateinit var addPresenter: AddPresenter
    
    private var shareActivity: ShareActivity? = null
    private var shareAddAdapter: ShareAddAdapter? = null
    private var sharePanelViews: SharePanelViews? = null
    private var toolbarMenu: Menu? = null
    private var searchItem: MenuItem? = null
    private var searchView: SearchView? = null
    private var viewBinding: FragmentShareAddListSearchBinding? = null
    private var fragmentListBinding: FragmentListBinding? = null

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
        fragmentListBinding = FragmentListBinding
            .bind(viewBinding?.root ?: throw RuntimeException("View binding can not be null"))
            .apply {
                mUnbinder = ButterKnife.bind(root)
                mPlaceholderLayout = placeholderLayout.placeholderLayout
                mListLayout = listLayout
                mRecyclerView = listOfItems
                mSwipeRefresh = listSwipeRefresh
            }
        return viewBinding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init(savedInstanceState)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        sharePanelViews?.unbind()
        viewBinding = null
        fragmentListBinding = null
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
        return false
    }

    override fun onUpdateSearch(users: MutableList<ViewType>?) {
        users?.let { shareAddAdapter?.setItems(it) } ?: run {
            setPlaceholder(true)
        }
    }

    override fun onItemClick(view: View, position: Int) {
        shareAddAdapter?.getItem(position).let { item ->
            when (item) {
                is UserUi -> item.isSelected = !item.isSelected
                is GroupUi -> item.isSelected = !item.isSelected
            }
        }
        shareAddAdapter?.notifyItemChanged(position)
        setCountChecked()
    }

    override fun onPanelAccessClick(accessCode: Int) {
        addPresenter.accessCode = accessCode
    }

    override fun onPanelResetClick() {
        addPresenter.resetChecked()
        sharePanelViews?.setCount(0)
        sharePanelViews?.setAddButtonEnable(false)
        shareAddAdapter?.notifyDataSetChanged()
    }

    override fun onPanelMessageClick(isShow: Boolean) {
        // Stub
    }

    override fun onPanelAddClick() {
        mSwipeRefresh.isRefreshing = true
        addPresenter.shareItem()
    }

    override fun onMessageInput(message: String) {
        addPresenter.setMessage(message)
    }

    override fun onError(message: String?) {
        mSwipeRefresh.isRefreshing = false
        message?.let { showSnackBar(it) }
        if (shareAddAdapter?.itemCount == 0) {
            mPlaceholderViews.setTemplatePlaceholder(PlaceholderViews.Type.CONNECTION)
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
        mSwipeRefresh.isRefreshing = false
        shareAddAdapter?.setMode(BaseAdapter.Mode.COMMON)
        shareAddAdapter?.setItems(list)
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

    private fun init(savedInstanceState: Bundle?) {
        setActionBarTitle(getString(R.string.share_title_search))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)
        shareActivity?.expandAppBar()
        getArgs()
        initViews()
        restoreViews(savedInstanceState)
    }

    private fun getArgs() {
        addPresenter.setItem((arguments?.getSerializable(TAG_ITEM) as Item))
    }

    private fun initViews() {
        sharePanelViews = SharePanelViews(viewBinding?.sharePanelLayout?.root, activity).apply {
            setOnEventListener(this@AddSearchFragment)
            setAccessIcon(addPresenter.accessCode)
        }
        shareAddAdapter = ShareAddAdapter(ShareAddHolderFactory{ view, position ->
            onItemClick(view, position)
        })
        shareAddAdapter?.setMode(BaseAdapter.Mode.COMMON)
        mRecyclerView.adapter = shareAddAdapter
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
        addPresenter.countChecked.let { countChecked ->
            sharePanelViews?.setCount(countChecked)
            sharePanelViews?.setAddButtonEnable(countChecked > 0)
        }
    }

    private fun setPlaceholder(isEmpty: Boolean) {
        mPlaceholderViews.setTemplatePlaceholder(if (isEmpty) PlaceholderViews.Type.NONE else PlaceholderViews.Type.COMMON)
    }

    companion object {
        val TAG = AddSearchFragment::class.java.simpleName
        const val TAG_ITEM = "TAG_ITEM"

        fun newInstance(item: Item?): AddSearchFragment {
            item?.let {
                return AddSearchFragment().apply {
                    arguments = Bundle(1).apply {
                        putSerializable(TAG_ITEM, item)
                    }
                }
            } ?: run {
                throw NullPointerException("Item must not be null!")
            }
        }
    }
}