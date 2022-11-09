package app.editors.manager.ui.fragments.share

import android.os.Build
import android.os.Bundle
import android.view.View
import app.editors.manager.R
import app.editors.manager.mvp.models.explorer.Item
import app.editors.manager.mvp.models.models.ModelShareStack
import app.editors.manager.mvp.models.ui.GroupUi
import app.editors.manager.mvp.models.ui.UserUi
import app.editors.manager.mvp.presenters.share.AddPresenter
import app.editors.manager.mvp.views.share.AddView
import app.editors.manager.ui.activities.main.MainActivity.Companion.show
import app.editors.manager.ui.adapters.ShareAdapter
import app.editors.manager.ui.adapters.holders.factory.ShareHolderFactory
import app.editors.manager.ui.fragments.base.ListFragment
import app.editors.manager.ui.views.custom.PlaceholderViews
import lib.toolkit.base.ui.adapters.BaseAdapter
import lib.toolkit.base.ui.adapters.holder.ViewType
import moxy.presenter.InjectPresenter
import moxy.presenter.ProvidePresenter

class AddFragment : ListFragment(), AddView, BaseAdapter.OnItemClickListener {

    enum class Type {
        USERS, GROUPS, NONE
    }

    @InjectPresenter
    lateinit var addPresenter: AddPresenter

    @Suppress("DEPRECATION")
    @ProvidePresenter
    fun providePresenter(): AddPresenter {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            AddPresenter(
                arguments?.getSerializable(TAG_ITEM, Item::class.java) as Item,
                arguments?.getSerializable(TAG_TYPE, Type::class.java) as Type
            )
        } else {
            AddPresenter(
                arguments?.getSerializable(TAG_ITEM) as Item,
                arguments?.getSerializable(TAG_TYPE) as Type
            )
        }
    }

    private var shareAdapter: ShareAdapter? = null
    private var type: Type? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init(savedInstanceState)
    }

    override fun onRefresh() {
        requestData()
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

    override fun onError(message: String?) {
        swipeRefreshLayout?.isRefreshing = false
        if (shareAdapter?.itemCount == 0) {
            placeholderViews?.setTemplatePlaceholder(PlaceholderViews.Type.CONNECTION)
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
        swipeRefreshLayout?.isRefreshing = false
        shareAdapter?.setMode(BaseAdapter.Mode.USERS)
        shareAdapter?.setItems(list)
    }

    override fun onGetGroups(list: List<ViewType>) {
        setPlaceholder(false, list.isNotEmpty())
        swipeRefreshLayout?.isRefreshing = false
        shareAdapter?.setMode(BaseAdapter.Mode.GROUPS)
        shareAdapter?.setItems(list.map {
            it.apply {
                if (it is GroupUi) {
                    when (it.id) {
                        GroupUi.GROUP_ADMIN_ID -> it.name = getString(R.string.share_group_admin)
                        GroupUi.GROUP_EVERYONE_ID -> it.name = getString(R.string.share_group_everyone)
                    }
                }
            }
        })
    }

    override fun onGetCommon(list: List<ViewType>) {
        // Stub
    }

    override fun onSuccessAdd() {
        ModelShareStack.getInstance().isRefresh = true
        if (parentFragmentManager.fragments.size > 1) {
            showParentRootFragment()
        } else {
            requireActivity().finish()
        }
    }

    override fun onSearchValue(value: String?) {
        // Stub
    }

    override fun onUpdateSearch(users: MutableList<ViewType>?) {
        // Stub
    }

    override fun onUpdateAvatar(user: UserUi) {
        shareAdapter?.let { adapter ->
            val position = adapter.updateItem(user)
            adapter.notifyItemChanged(position, ShareAdapter.PAYLOAD_AVATAR)
        }
    }

    override fun onResume() {
        super.onResume()
        updateSelectionMenu()
    }

    private fun init(savedInstanceState: Bundle?) {
        getArgs()
        initViews()
        restoreViews(savedInstanceState)
    }

    private fun getArgs() {
        arguments?.let {
            type = it.getSerializable(TAG_TYPE) as Type
        }
    }

    private fun initViews() {
        shareAdapter = ShareAdapter(ShareHolderFactory { view, position ->
            onItemClick(view, position)
        })
        recyclerView?.adapter = shareAdapter
    }

    private fun restoreViews(savedInstanceState: Bundle?) {
        savedInstanceState?.let {
            addPresenter.updateTypeSharedListState()
        } ?: run {
            requestData()
        }
    }

    private fun requestData() {
        swipeRefreshLayout?.isRefreshing = true
        addPresenter.shared
    }

    private fun setPlaceholder(isUsers: Boolean, isEmpty: Boolean) {
        if (isUsers) {
            placeholderViews?.setTemplatePlaceholder(
                if (isEmpty)
                    PlaceholderViews.Type.NONE else PlaceholderViews.Type.USERS
            )
        } else {
            placeholderViews?.setTemplatePlaceholder(
                if (isEmpty)
                    PlaceholderViews.Type.NONE else PlaceholderViews.Type.GROUPS
            )
        }
    }

    private fun setCountChecked() {
        updateSelectionMenu()
        (parentFragment as AddPagerFragment).setChecked()
    }

    private val isActivePage: Boolean
        get() = parentFragment.let { it is AddPagerFragment && it.isActivePage(this) }

    private fun updateSelectionMenu() {
        (parentFragment as AddPagerFragment).selectionMenu?.let { menu ->
            type?.let { type ->
                menu.findItem(R.id.menu_share_deselect)?.isVisible = addPresenter.isSelected(type)
                menu.findItem(R.id.menu_share_select_all)?.isVisible = !addPresenter.isSelectedAll(type)
            }
        }
    }

    fun setSelectedAll(isSelected: Boolean) {
        shareAdapter?.let { adapter ->
            when (type) {
                Type.USERS -> adapter.setItems(adapter.itemsList
                    .filterIsInstance(UserUi::class.java)
                    .map { it.apply { this.isSelected = isSelected } })
                Type.GROUPS -> adapter.setItems(adapter.itemsList
                    .filterIsInstance(GroupUi::class.java)
                    .map { it.apply { this.isSelected = isSelected } })
                else -> {}
            }
        }
        setCountChecked()
    }

    fun updateAdapterState() {
        shareAdapter?.notifyDataSetChanged()
    }

    fun addAccess() {
        addPresenter.shareItem()
    }

    fun setMessage(message: String?) {
        addPresenter.setMessage(message)
    }

    companion object {
        val TAG: String = AddFragment::class.java.simpleName
        const val TAG_ITEM = "TAG_ITEM"
        const val TAG_TYPE = "TAG_TYPE"

        @JvmStatic
        fun newInstance(item: Item?, type: Type?): AddFragment {
            return AddFragment().apply {
                arguments = Bundle(2).apply {
                    putSerializable(TAG_ITEM, checkNotNull(item))
                    putSerializable(TAG_TYPE, checkNotNull(type))
                }
            }
        }
    }
}
