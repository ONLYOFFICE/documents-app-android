package app.editors.manager.ui.fragments.share

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.*
import android.widget.EditText
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.DiffUtil
import app.documents.core.network.manager.models.explorer.CloudFolder
import app.documents.core.network.manager.models.explorer.Item
import app.editors.manager.R
import app.editors.manager.databinding.FragmentShareAddListSearchBinding
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
import lib.toolkit.base.managers.utils.getSerializableExt
import lib.toolkit.base.ui.adapters.BaseAdapter
import lib.toolkit.base.ui.adapters.holder.ViewType
import moxy.presenter.InjectPresenter
import moxy.presenter.ProvidePresenter

class AddSearchFragment : ListFragment(), AddView, SearchView.OnQueryTextListener,
    BaseAdapter.OnItemClickListener, SharePanelViews.OnEventListener {

    @InjectPresenter
    lateinit var addPresenter: AddPresenter

    @ProvidePresenter
    fun providePresenter(): AddPresenter {
        return AddPresenter(inputItem, if (isRoom) AddPresenter.Type.Users else AddPresenter.Type.Common)
    }

    private var shareActivity: ShareActivity? = null
    private var shareAdapter: ShareAdapter? = null
    private var sharePanelViews: SharePanelViews? = null
    private var toolbarMenu: Menu? = null
    private var searchItem: MenuItem? = null
    private var searchView: SearchView? = null
    private val inputItem: Item by lazy { checkNotNull(arguments?.getSerializableExt(AddFragment.TAG_ITEM)) }
    private val isRoom: Boolean by lazy { (inputItem as? CloudFolder)?.isRoom == true }
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
        resetChecked()
        return super.onBackPressed()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.share_add_search, menu)
        toolbarMenu = menu
        searchItem = menu.findItem(R.id.menu_share_add_search)
        searchView = (searchItem?.actionView as SearchView).apply {
            setOnQueryTextListener(this@AddSearchFragment)
            maxWidth = Int.MAX_VALUE
            isIconified = false
            findViewById<EditText>(androidx.appcompat.R.id.search_src_text).hint = null
            findViewById<View>(androidx.appcompat.R.id.search_plate).setBackgroundColor(Color.TRANSPARENT)

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
        addPresenter.fetchSharedList()
    }

    override fun onQueryTextSubmit(query: String) = false

    override fun onQueryTextChange(newText: String): Boolean {
        addPresenter.setSearchValue(newText)
        resetChecked()
        return false
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

    @SuppressLint("NotifyDataSetChanged")
    override fun onPanelResetClick() {
        resetChecked()
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

    override fun onGetUsers(list: MutableList<ViewType>) {
        setPlaceholder(list.isEmpty())
        swipeRefreshLayout?.isRefreshing = false
        shareAdapter?.setMode(BaseAdapter.Mode.USERS)
        updateDiffUtils(list)
    }

    override fun onGetGroups(list: List<ViewType>) {
        // Stub
    }

    override fun onGetCommon(list: List<ViewType>) {
        setPlaceholder(list.isEmpty())
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

    private fun init(savedInstanceState: Bundle?) {
        setActionBarTitle(getString(R.string.share_title_search))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)
        shareActivity?.expandAppBar()
        resetChecked()
        if (savedInstanceState == null) initViews()
    }

    private fun initViews() {
        viewBinding?.let { binding ->
            sharePanelViews = SharePanelViews(binding.sharePanelLayout.root, inputItem).apply {
                setOnEventListener(this@AddSearchFragment)
                setAccessIcon(addPresenter.accessCode)
            }
        }
        shareAdapter = ShareAdapter(ShareHolderFactory { view, position -> onItemClick(view, position) }, false)
        shareAdapter?.setMode(if (isRoom) BaseAdapter.Mode.USERS else BaseAdapter.Mode.COMMON)
        recyclerView?.adapter = shareAdapter
        addPresenter.fetchSharedList()
        setCountChecked()
    }

    private fun setCountChecked() {
        sharePanelViews?.setCount(addPresenter.countChecked)
        sharePanelViews?.setAddButtonEnable(addPresenter.countChecked > 0)
    }

    private fun setPlaceholder(isEmpty: Boolean) {
        placeholderViews?.setTemplatePlaceholder(
            if (isEmpty) {
                if (isRoom) {
                    PlaceholderViews.Type.USERS
                } else {
                    PlaceholderViews.Type.COMMON
                }
            } else {
                PlaceholderViews.Type.NONE
            }
        )
    }

    private fun resetChecked() {
        addPresenter.resetChecked()
        sharePanelViews?.setCount(0)
        sharePanelViews?.setAddButtonEnable(false)
    }

    companion object {
        val TAG: String = AddSearchFragment::class.java.simpleName
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