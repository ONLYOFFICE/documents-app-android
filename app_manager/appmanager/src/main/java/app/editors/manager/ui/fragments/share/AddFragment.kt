package app.editors.manager.ui.fragments.share

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import app.editors.manager.app.App
import app.editors.manager.databinding.FragmentListBinding
import app.editors.manager.databinding.FragmentShareAddListBinding
import app.editors.manager.mvp.models.explorer.Item
import app.editors.manager.mvp.models.models.ModelShareStack
import app.editors.manager.mvp.models.ui.GroupUi
import app.editors.manager.mvp.models.ui.UserUi
import app.editors.manager.mvp.presenters.share.AddPresenter
import app.editors.manager.mvp.views.share.AddView
import app.editors.manager.ui.activities.main.MainActivity.Companion.show
import app.editors.manager.ui.adapters.holders.factory.ShareAddHolderFactory
import app.editors.manager.ui.adapters.share.ShareAddAdapter
import app.editors.manager.ui.fragments.base.ListFragment
import app.editors.manager.ui.views.custom.PlaceholderViews
import butterknife.ButterKnife
import lib.toolkit.base.managers.tools.ResourcesProvider
import lib.toolkit.base.ui.adapters.BaseAdapter
import lib.toolkit.base.ui.adapters.holder.ViewType
import moxy.presenter.InjectPresenter
import javax.inject.Inject

class AddFragment : ListFragment(), AddView, BaseAdapter.OnItemClickListener {

    enum class Type {
        USERS, GROUPS
    }

    @InjectPresenter
    lateinit var addPresenter: AddPresenter

    private var shareAddAdapter: ShareAddAdapter? = null
    private var viewBinding: FragmentShareAddListBinding? = null
    private var fragmentListBinding: FragmentListBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewBinding = FragmentShareAddListBinding.inflate(inflater, container, false)
        fragmentListBinding = FragmentListBinding
            .bind(viewBinding?.root ?: throw RuntimeException("View binding can not be null"))
            .apply {
                mUnbinder = ButterKnife.bind(root)
                mPlaceholderLayout = placeholderLayout.root
                mRecyclerView = listOfItems
                mSwipeRefresh = listSwipeRefresh
                mListLayout = root
            }
        return viewBinding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init(savedInstanceState)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewBinding = null
        fragmentListBinding = null
    }

    override fun onRefresh() {
        requestData()
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

    override fun onError(message: String?) {
        mSwipeRefresh.isRefreshing = false
        if (shareAddAdapter?.itemCount == 0) {
            mPlaceholderViews.setTemplatePlaceholder(PlaceholderViews.Type.CONNECTION)
        }
        if (isActivePage) {
            message?.let { showSnackBar(it) }
        }
    }

    override fun onUnauthorized(message: String?) {
        requireActivity().finish()
        show(requireContext())
    }

    override fun onGetUsers(list: List<ViewType>) {
        setPlaceholder(true, list.isNotEmpty())
        mSwipeRefresh.isRefreshing = false
        shareAddAdapter?.setMode(BaseAdapter.Mode.USERS)
        shareAddAdapter?.setItems(list)
    }

    override fun onGetGroups(list: List<ViewType>) {
        setPlaceholder(false, list.isNotEmpty())
        mSwipeRefresh.isRefreshing = false
        shareAddAdapter?.setMode(BaseAdapter.Mode.GROUPS)
        shareAddAdapter?.setItems(list)
    }

    override fun onGetCommon(list: List<ViewType>) {
        // Stub
    }

    override fun onSuccessAdd() {
        ModelShareStack.getInstance().isRefresh = true
        showParentRootFragment()
    }

    override fun onSearchValue(value: String?) {
        // Stub
    }

    override fun onUpdateSearch(users: MutableList<ViewType>?) {
        // Stub
    }

    private fun init(savedInstanceState: Bundle?) {
        getArgs()
        initViews()
        restoreViews(savedInstanceState)
    }

    private fun getArgs() {
        arguments.let { bundle ->
            addPresenter.setItem((bundle?.getSerializable(TAG_ITEM) as Item))
            addPresenter.setType((bundle.getSerializable(TAG_TYPE) as Type))
        }
    }

    private fun initViews() {
        shareAddAdapter = ShareAddAdapter(ShareAddHolderFactory { view, integer ->
            onItemClick(view, integer)
        })
        mRecyclerView.adapter = shareAddAdapter
    }

    private fun restoreViews(savedInstanceState: Bundle?) {
        savedInstanceState?.let {
            addPresenter.updateTypeSharedListState()
        } ?: run {
            requestData()
        }
    }

    private fun requestData() {
        mSwipeRefresh.isRefreshing = true
        addPresenter.shared
    }

    private fun setPlaceholder(isUsers: Boolean, isEmpty: Boolean) {
        if (isUsers) {
            mPlaceholderViews.setTemplatePlaceholder(if (isEmpty) PlaceholderViews.Type.NONE else PlaceholderViews.Type.USERS)
        } else {
            mPlaceholderViews.setTemplatePlaceholder(if (isEmpty) PlaceholderViews.Type.NONE else PlaceholderViews.Type.GROUPS)
        }
    }

    private fun setCountChecked() {
        (parentFragment as AddPagerFragment).setChecked()
    }

    private val isActivePage: Boolean
        get() {
            val fragment = parentFragment
            return if (fragment is AddPagerFragment) {
                fragment.isActivePage(this)
            } else true
        }

    fun updateAdapterState() {
        shareAddAdapter?.notifyDataSetChanged()
    }

    fun addAccess() {
        addPresenter.shareItem()
    }

    fun setMessage(message: String?) {
        addPresenter.setMessage(message)
    }

    companion object {
        val TAG = AddFragment::class.java.simpleName
        const val TAG_ITEM = "TAG_ITEM"
        const val TAG_TYPE = "TAG_TYPE"

        @JvmStatic
        fun newInstance(item: Item?, type: Type?): AddFragment {
            item?.let {
                return AddFragment().apply {
                    arguments = Bundle(2).apply {
                        putSerializable(TAG_ITEM, item)
                        putSerializable(TAG_TYPE, type)
                    }
                }
            } ?: run {
                throw NullPointerException("Item must not be null!")
            }

        }
    }
}