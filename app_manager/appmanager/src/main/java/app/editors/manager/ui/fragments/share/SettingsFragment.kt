package app.editors.manager.ui.fragments.share

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout.OnRefreshListener
import androidx.transition.TransitionManager
import app.documents.core.network.common.contracts.ApiContract
import app.documents.core.network.manager.models.explorer.CloudFolder
import app.documents.core.network.manager.models.explorer.Item
import app.editors.manager.R
import app.editors.manager.databinding.FragmentShareSettingsListBinding
import app.editors.manager.databinding.IncludeShareSettingsHeaderBinding
import app.editors.manager.mvp.models.models.ModelShareStack
import app.editors.manager.mvp.models.ui.ShareHeaderUi
import app.editors.manager.mvp.models.ui.ShareUi
import app.editors.manager.mvp.presenters.share.SettingsPresenter
import app.editors.manager.mvp.views.share.SettingsView
import app.editors.manager.ui.activities.main.MainActivity.Companion.show
import app.editors.manager.ui.activities.main.ShareActivity
import app.editors.manager.ui.adapters.ShareAdapter
import app.editors.manager.ui.adapters.holders.factory.ShareHolderFactory
import app.editors.manager.ui.fragments.base.BaseAppFragment
import app.editors.manager.ui.views.custom.PlaceholderViews
import app.editors.manager.ui.views.popup.SharePopup
import lib.toolkit.base.managers.utils.getSerializableExt
import lib.toolkit.base.ui.adapters.holder.ViewType
import moxy.presenter.InjectPresenter
import moxy.presenter.ProvidePresenter

class SettingsFragment : BaseAppFragment(), SettingsView, OnRefreshListener {

    @InjectPresenter
    lateinit var settingsPresenter: SettingsPresenter

    @ProvidePresenter
    fun provideSettingsPresenter(): SettingsPresenter {
        return SettingsPresenter(arguments?.getSerializableExt(TAG_ITEM, Item::class.java) as Item)
    }

    private var sharePopup: SharePopup? = null
    private var shareActivity: ShareActivity? = null
    private var shareSettingsAdapter: ShareAdapter? = null
    private var placeholderViews: PlaceholderViews? = null

    private var viewBinding: FragmentShareSettingsListBinding? = null
    private var headerBinding: IncludeShareSettingsHeaderBinding? = null

    private val item: Item
        get() = arguments?.getSerializableExt(TAG_ITEM, Item::class.java) as Item

    override fun onAttach(context: Context) {
        super.onAttach(context)
        shareActivity = try {
            context as ShareActivity
        } catch (e: ClassCastException) {
            throw RuntimeException(
                SettingsFragment::class.java.simpleName + " - must implement - " +
                        ShareActivity::class.java.simpleName
            )
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        sharePopup?.let {
            if (it.isShowing) {
                outState.putBoolean(TAG_SHOW_POPUP, it.isShowing)
                outState.putInt(TAG_POSITION_POPUP, settingsPresenter.sharePosition)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewBinding = FragmentShareSettingsListBinding.inflate(inflater, container, false)
        headerBinding = viewBinding?.shareSettingsHeader
        return viewBinding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init(savedInstanceState)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        sharePopup?.let {
            if (it.isShowing)
                it.hide()
        }
        shareActivity = null
        viewBinding = null
        headerBinding = null
    }

    override fun onBackPressed(): Boolean {
        sharePopup?.let {
            if (it.isShowing) {
                it.hide()
                return true
            }
        }
        return super.onBackPressed()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.share_settings, menu)
        menu.findItem(R.id.menu_share_settings).isVisible = !settingsPresenter.isPersonalAccount
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.menu_share_settings) settingsPresenter.getInternalLink()
        return super.onOptionsItemSelected(item)
    }

    private fun initClickListeners() {
        viewBinding?.shareSettingsAddItem?.setOnClickListener { settingsPresenter.addShareItems() }
        headerBinding?.let { binding ->
            binding.shareSettingsAccessButtonLayout.setOnClickListener { showAccessPopup() }
            binding.shareSettingsExternalCopyLink.setOnClickListener {
                copySharedLinkToClipboard(
                    settingsPresenter.externalLink,
                    getString(R.string.share_clipboard_external_copied)
                )
            }
            binding.shareSettingsExternalSendLink.setOnClickListener {
                settingsPresenter.sendLink(settingsPresenter.externalLink)
            }
        }
    }

    override fun onError(message: String?) {
        placeholderViews?.setTemplatePlaceholder(PlaceholderViews.Type.CONNECTION)
        viewBinding?.shareSettingsListSwipeRefresh?.isRefreshing = false
        message?.let { text -> showSnackBar(text) }
    }

    override fun onUnauthorized(message: String?) {
        requireActivity().finish()
        show(requireContext())
    }

    override fun onGetShare(list: List<ViewType>, accessCode: Int) {
        viewBinding?.shareSettingsListSwipeRefresh?.isRefreshing = false
        shareSettingsAdapter?.setItems(list)
        setExternalViewState(accessCode, false)
    }

    override fun onGetShareItem(entity: ViewType, sharePosition: Int, accessCode: Int) {
        viewBinding?.shareSettingsListSwipeRefresh?.isRefreshing = false
        shareSettingsAdapter?.setItem(entity, sharePosition)
        setExternalViewState(accessCode, false)
    }

    override fun onRemove(share: ShareUi, sharePosition: Int) {
        viewBinding?.shareSettingsListSwipeRefresh?.isRefreshing = false
        shareSettingsAdapter?.let { adapter ->
            val previousItem = adapter.getItem(sharePosition - 1)
            val nextItem = adapter.getItem(sharePosition + 1)

            adapter.removeItem(sharePosition)

            if (previousItem is ShareHeaderUi && (nextItem == null || nextItem is ShareHeaderUi)) {
                adapter.removeHeader(previousItem)
            }

            showSnackBarWithAction(
                getString(R.string.share_snackbar_remove_user),
                getString(R.string.snackbar_undo)
            ) {
                settingsPresenter.setItemAccess(share.access)
                adapter.addItem(share)
            }
        }
    }

    override fun onExternalAccess(accessCode: Int, isMessage: Boolean) {
        setExternalViewState(accessCode, isMessage)
    }

    override fun onInternalLink(internalLink: String) {
        copySharedLinkToClipboard(internalLink, getString(R.string.share_clipboard_internal_copied))
    }

    override fun onItemType(isFolder: Boolean) {
        headerBinding?.shareSettingsHeaderLayout?.visibility =
            if (isFolder) View.GONE else View.VISIBLE
    }

    override fun onAddShare(item: Item) {
        showFragment(AddPagerFragment.newInstance(item), AddFragment.TAG, false)
    }

    override fun onPlaceholderState(type: PlaceholderViews.Type) {
        if (!settingsPresenter.isPersonalAccount) {
            placeholderViews?.setTemplatePlaceholder(type)
        }
    }

    override fun onActionButtonState(isVisible: Boolean) {
        viewBinding?.let { binding ->
            if (isVisible) {
                binding.shareSettingsAddItem.show()
            } else {
                binding.shareSettingsAddItem.hide()
            }
        }
    }

    override fun onResultState(isShared: Boolean) {
        val intent = Intent()
        intent.putExtra(ShareActivity.TAG_RESULT, isShared)
        requireActivity().setResult(Activity.RESULT_OK, intent)
    }

    override fun onRefresh() {
        getSharedItems()
    }

    private fun onItemContextClick(view: View?, position: Int) {
        val share = shareSettingsAdapter?.getItem(position) as ShareUi
        if (share.isLocked) {
            return
        }
        settingsPresenter.setShared(share, position)
        setPopup(view, share.isGuest)
    }

    private fun setPopup(view: View?, isVisitor: Boolean) {
        view?.post {
            sharePopup = SharePopup(
                context = requireContext(),
                layoutId = R.layout.popup_share_menu
            ).apply {
                setContextListener(listContextListener)
                if (isVisitor) {
                    this.isVisitor = true
                }
                setFullAccess(true)
                setItem(item)
                showDropAt(view, requireActivity())
            }
        }
    }

    private fun init(savedInstanceState: Bundle?) {
        if (item is CloudFolder && (item as CloudFolder).isRoom) {
            setActionBarTitle(item.title)
        } else {
            setActionBarTitle(getString(R.string.share_title_main))
        }

        supportActionBar?.let { actionBar ->
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeButtonEnabled(true)
        }
        shareActivity?.expandAppBar()
        initViews()
        initClickListeners()
        restoreViews(savedInstanceState)
        ModelShareStack.getInstance().clearModel()
    }

    private fun initViews() {
        viewBinding?.let { binding ->
            binding.shareSettingsAddItem.hide()
            placeholderViews = PlaceholderViews(binding.placeholderLayout.root)
            placeholderViews?.setViewForHide(binding.shareMainListOfItems)
            shareSettingsAdapter = ShareAdapter(ShareHolderFactory(::onItemContextClick))
            binding.shareMainListOfItems.layoutManager = LinearLayoutManager(context)
            binding.shareMainListOfItems.adapter = shareSettingsAdapter

            with(binding.shareSettingsListSwipeRefresh) {
                setOnRefreshListener(this@SettingsFragment)
                setColorSchemeResources(lib.toolkit.base.R.color.colorSecondary)
                setProgressBackgroundColorSchemeResource(lib.toolkit.base.R.color.colorSurface)
                binding.shareMainListOfItems.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                        val topRowVerticalPosition =
                            if (recyclerView.childCount == 0) 0 else recyclerView.getChildAt(0).top
                        binding.shareSettingsListSwipeRefresh.isEnabled = topRowVerticalPosition >= 0
                    }
                })
            }
        }
    }

    private fun restoreViews(savedInstanceState: Bundle?) {
        settingsPresenter.updateSharedExternalState(false)
        settingsPresenter.updatePlaceholderState()
        settingsPresenter.updateHeaderState()

        savedInstanceState?.let { savedState ->
            if (ModelShareStack.getInstance().isRefresh) {
                getSharedItems()
            } else {
                settingsPresenter.updateActionButtonState()
                settingsPresenter.updateSharedListState()
            }

            if (savedState.containsKey(TAG_SHOW_POPUP)
                && savedState.containsKey(TAG_POSITION_POPUP)
            ) {
                settingsPresenter.setIsPopupShow(true)
            } else {
                settingsPresenter.setIsPopupShow(false)
            }
        } ?: run { getSharedItems() }
    }

    private fun getSharedItems() {
        viewBinding?.shareSettingsListSwipeRefresh?.isRefreshing = true
        settingsPresenter.getShared()
    }

    private fun setExternalViewState(accessCode: Int, isMessage: Boolean) {
        @StringRes var messageRes = R.string.share_access_denied
        @DrawableRes var iconRes = R.drawable.ic_access_deny
        when (accessCode) {
            ApiContract.ShareCode.NONE, ApiContract.ShareCode.RESTRICT -> {
                iconRes = R.drawable.ic_access_deny
                messageRes = R.string.share_access_denied
                onButtonState(false)
            }
            ApiContract.ShareCode.REVIEW -> {
                iconRes = R.drawable.ic_access_review
                messageRes = R.string.share_access_success
                onButtonState(true)
            }
            ApiContract.ShareCode.READ -> {
                iconRes = R.drawable.ic_access_read
                messageRes = R.string.share_access_success
                onButtonState(true)
            }
            ApiContract.ShareCode.READ_WRITE -> {
                iconRes = if (item is CloudFolder && (item as CloudFolder).isRoom) {
                    R.drawable.ic_drawer_menu_my_docs
                } else {
                    R.drawable.ic_access_full
                }
                messageRes = R.string.share_access_success
                onButtonState(true)
            }
            ApiContract.ShareCode.COMMENT -> {
                iconRes = R.drawable.ic_access_comment
                messageRes = R.string.share_access_success
                onButtonState(true)
            }
            ApiContract.ShareCode.FILL_FORMS -> {
                iconRes = R.drawable.ic_access_fill_form
                messageRes = R.string.share_access_success
                onButtonState(true)
            }
            ApiContract.ShareCode.CUSTOM_FILTER -> {
                iconRes = R.drawable.ic_access_custom_filter
                messageRes = R.string.share_access_success
                onButtonState(true)
            }
        }
        headerBinding?.shareSettingsAccessButtonLayout?.setIconResource(iconRes)
        if (isMessage) {
            showSnackBar(messageRes)
        }
    }

    override fun onButtonState(isVisible: Boolean) {
        headerBinding?.shareSettingsExternalAccessLayout?.let { settingsAccessLayout ->
            TransitionManager.beginDelayedTransition(settingsAccessLayout)
            settingsAccessLayout.visibility = if (isVisible) View.VISIBLE else View.GONE
        }
    }

    override fun onPopupState(state: Boolean) {
        headerBinding?.shareSettingsAccessButtonLayout?.isVisible = false
    }

    override fun onShowPopup(sharePosition: Int, isVisitor: Boolean) {
        viewBinding?.shareMainListOfItems.let { recyclerView: RecyclerView? ->
            recyclerView?.post {
                if (sharePosition != 0) {
//                 TODO   setPopup(recyclerView.layoutManager?.findViewByPosition(sharePosition)?.findViewById(R.id.button_popup_arrow), isVisitor)
                } else {
                    showAccessPopup()
                }
            } ?: setPopup(viewBinding?.shareSettingsListContentLayout, isVisitor)
        }
    }

    override fun onUpdateAvatar(share: ShareUi) {
        shareSettingsAdapter?.let { adapter ->
            val position = adapter.updateItem(share)
            adapter.notifyItemChanged(position, ShareAdapter.PAYLOAD_AVATAR)
        }
    }

    private fun showAccessPopup() {
        sharePopup = SharePopup(
            context = requireContext(),
            layoutId = R.layout.popup_share_menu
        ).apply {
            setContextListener(externalContextListener)
            setExternalLink()
            setFullAccess(false)
            setItem(item)
            showDropAt(
                checkNotNull(headerBinding?.shareSettingsAccessButtonLayout),
                requireActivity()
            )
        }
    }

    override fun onSendLink(intent: Intent) {
        startActivity(Intent.createChooser(intent, getString(R.string.operation_share_send_link)))
    }

    private val listContextListener = object : SharePopup.PopupContextListener {
        override fun onContextClick(v: View, sharePopup: SharePopup) {
             sharePopup.hide()
            when (v.id) {
                R.id.fullAccessItem -> settingsPresenter.setItemAccess(ApiContract.ShareCode.READ_WRITE)
                R.id.powerUserItem -> settingsPresenter.setItemAccess(ApiContract.ShareCode.POWER_USER)
                R.id.reviewItem -> settingsPresenter.setItemAccess(ApiContract.ShareCode.REVIEW)
                R.id.viewItem -> settingsPresenter.setItemAccess(ApiContract.ShareCode.READ)
                R.id.editorItem -> settingsPresenter.setItemAccess(ApiContract.ShareCode.EDITOR)
                R.id.denyItem -> settingsPresenter.setItemAccess(ApiContract.ShareCode.RESTRICT)
                R.id.deleteItem -> settingsPresenter.setItemAccess(ApiContract.ShareCode.NONE)
                R.id.commentItem -> settingsPresenter.setItemAccess(ApiContract.ShareCode.COMMENT)
                R.id.fillFormItem -> settingsPresenter.setItemAccess(ApiContract.ShareCode.FILL_FORMS)
                R.id.customFilterItem -> settingsPresenter.setItemAccess(ApiContract.ShareCode.CUSTOM_FILTER)
            }
        }
    }

    private val externalContextListener = object : SharePopup.PopupContextListener {
        override fun onContextClick(v: View, sharePopup: SharePopup) {
            sharePopup.hide()
            when (v.id) {
                R.id.fullAccessItem -> settingsPresenter.getExternalLink(ApiContract.ShareType.READ_WRITE)
                R.id.reviewItem -> settingsPresenter.getExternalLink(ApiContract.ShareType.REVIEW)
                R.id.viewItem -> settingsPresenter.getExternalLink(ApiContract.ShareType.READ)
                R.id.denyItem -> settingsPresenter.getExternalLink(ApiContract.ShareType.NONE)
                R.id.commentItem -> settingsPresenter.getExternalLink(ApiContract.ShareType.COMMENT)
                R.id.fillFormItem -> settingsPresenter.getExternalLink(ApiContract.ShareType.FILL_FORMS)
                R.id.customFilterItem -> settingsPresenter.getExternalLink(ApiContract.ShareType.CUSTOM_FILTER)
            }
        }
    }

    companion object {

        private const val TAG_ITEM = "TAG_ITEM"
        private const val TAG_SHOW_POPUP = "TAG_SHOW_POPUP"
        private const val TAG_POSITION_POPUP = "TAG_POSITION_POPUP"

        fun newInstance(item: Item?): SettingsFragment {
            return SettingsFragment().apply {
                arguments = Bundle(1).apply {
                    putSerializable(TAG_ITEM, item)
                }
            }
        }
    }
}