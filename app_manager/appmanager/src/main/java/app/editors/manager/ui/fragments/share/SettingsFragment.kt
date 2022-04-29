package app.editors.manager.ui.fragments.share

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout.OnRefreshListener
import androidx.transition.TransitionManager
import app.documents.core.network.ApiContract
import app.editors.manager.R
import app.editors.manager.databinding.FragmentShareSettingsListBinding
import app.editors.manager.databinding.IncludeButtonPopupBinding
import app.editors.manager.databinding.IncludeShareSettingsHeaderBinding
import app.editors.manager.mvp.models.explorer.CloudFolder
import app.editors.manager.mvp.models.explorer.Item
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
import lib.toolkit.base.managers.utils.StringUtils
import lib.toolkit.base.managers.utils.StringUtils.getExtension
import lib.toolkit.base.managers.utils.StringUtils.getExtensionFromPath
import lib.toolkit.base.ui.adapters.holder.ViewType
import moxy.presenter.InjectPresenter

class SettingsFragment : BaseAppFragment(), SettingsView, OnRefreshListener {

    @InjectPresenter
    lateinit var settingsPresenter: SettingsPresenter

    private var sharePopup: SharePopup? = null
    private var shareActivity: ShareActivity? = null
    private var shareSettingsAdapter: ShareAdapter? = null
    private var placeholderViews: PlaceholderViews? = null

    private var viewBinding: FragmentShareSettingsListBinding? = null
    private var headerBinding: IncludeShareSettingsHeaderBinding? = null
    private var popupBinding: IncludeButtonPopupBinding? = null

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
    ): View {
        viewBinding = FragmentShareSettingsListBinding.inflate(inflater, container, false)
        headerBinding = viewBinding?.shareSettingsHeader
        popupBinding = headerBinding?.shareSettingsAccessButtonLayout
        return viewBinding?.root!!
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
        popupBinding = null
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
        if (item.itemId == R.id.menu_share_settings) settingsPresenter.internalLink
        return super.onOptionsItemSelected(item)
    }

    private fun initClickListeners() {
        viewBinding?.shareSettingsAddItem?.setOnClickListener { settingsPresenter.addShareItems() }
        headerBinding?.let { binding ->
            binding.shareSettingsAccessButtonLayout.root.setOnClickListener { showAccessPopup() }
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
            if (isVisible && !settingsPresenter.isPersonalAccount) {
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
            sharePopup = SharePopup(requireContext(), R.layout.popup_share_menu)
            sharePopup?.let { popup ->
                popup.setContextListener(listContextListener)
                if (!isVisitor) {
                    if (settingsPresenter.item is CloudFolder) {
                        popup.setIsFolder()
                    } else {
                        val ext = getExtensionFromPath(checkNotNull(settingsPresenter.item?.title))
                        popup.extension = getExtension(ext).takeIf { it != StringUtils.Extension.FORM }
                            ?: StringUtils.getFormExtension(ext)
                    }
                } else {
                    popup.setIsVisitor()
                }
                popup.showDropAt(view, requireActivity())
            }
        }
    }

    private fun init(savedInstanceState: Bundle?) {
        setActionBarTitle(getString(R.string.share_title_main))
        supportActionBar?.let { actionBar ->
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeButtonEnabled(true)
        }
        shareActivity?.expandAppBar()
        getArgs()
        initViews()
        initClickListeners()
        restoreViews(savedInstanceState)
        ModelShareStack.getInstance().clearModel()
    }

    private fun getArgs() {
        val bundle = arguments
        val item = bundle?.getSerializable(TAG_ITEM) as Item
        settingsPresenter.item = item
    }

    private fun initViews() {
        viewBinding?.let { binding ->
            binding.shareSettingsAddItem.hide()
            placeholderViews = PlaceholderViews(binding.placeholderLayout.root)
            placeholderViews?.setViewForHide(binding.shareMainListOfItems)
            binding.shareSettingsListSwipeRefresh.setOnRefreshListener(this@SettingsFragment)
            binding.shareSettingsListSwipeRefresh.setColorSchemeColors(
                ContextCompat.getColor(requireContext(), lib.toolkit.base.R.color.colorSecondary)
            )
            shareSettingsAdapter = ShareAdapter(ShareHolderFactory { view, position ->
                onItemContextClick(view, position)
            })
            binding.shareMainListOfItems.layoutManager = LinearLayoutManager(context)
            binding.shareMainListOfItems.adapter = shareSettingsAdapter
            ViewCompat.setNestedScrollingEnabled(binding.shareMainListOfItems, false)
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
        settingsPresenter.shared
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
                iconRes = R.drawable.ic_access_full
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
        }
        popupBinding?.buttonPopupImage?.setImageResource(iconRes)
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
        popupBinding?.let { binding ->
            binding.buttonPopupImage.visibility = View.GONE
            binding.buttonPopupArrow.visibility = View.GONE
        }
    }

    override fun onShowPopup(sharePosition: Int, isVisitor: Boolean) {
        viewBinding?.shareMainListOfItems.let { recyclerView: RecyclerView? ->
            recyclerView?.post {
                if (sharePosition != 0) {
                    setPopup(
                        recyclerView.layoutManager?.findViewByPosition(sharePosition)
                            ?.findViewById(R.id.button_popup_arrow), isVisitor)
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
        sharePopup = SharePopup(requireContext(), R.layout.popup_share_menu)
        sharePopup?.let { popup ->
            popup.setContextListener(externalContextListener)
            popup.setExternalLink()
            popup.setFullAccess(false)
            if (settingsPresenter.item is CloudFolder) {
                popup.setIsFolder()
            } else {
                val ext = getExtensionFromPath(checkNotNull(settingsPresenter.item?.title))
                popup.extension = getExtension(ext).takeIf { it != StringUtils.Extension.FORM }
                    ?: StringUtils.getFormExtension(ext)
            }
            popup.showDropAt(
                headerBinding?.shareSettingsAccessButtonLayout?.root!!,
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
                R.id.popup_share_access_full -> settingsPresenter.setItemAccess(ApiContract.ShareCode.READ_WRITE)
                R.id.popup_share_access_review -> settingsPresenter.setItemAccess(ApiContract.ShareCode.REVIEW)
                R.id.popup_share_access_read -> settingsPresenter.setItemAccess(ApiContract.ShareCode.READ)
                R.id.popup_share_access_deny -> settingsPresenter.setItemAccess(ApiContract.ShareCode.RESTRICT)
                R.id.popup_share_access_remove -> settingsPresenter.setItemAccess(ApiContract.ShareCode.NONE)
                R.id.popup_share_access_comment -> settingsPresenter.setItemAccess(ApiContract.ShareCode.COMMENT)
                R.id.popup_share_access_fill_forms -> settingsPresenter.setItemAccess(ApiContract.ShareCode.FILL_FORMS)
            }
        }
    }

    private val externalContextListener = object : SharePopup.PopupContextListener {
        override fun onContextClick(v: View, sharePopup: SharePopup) {
            sharePopup.hide()
            when (v.id) {
                R.id.popup_share_access_full -> settingsPresenter.getExternalLink(ApiContract.ShareType.READ_WRITE)
                R.id.popup_share_access_review -> settingsPresenter.getExternalLink(ApiContract.ShareType.REVIEW)
                R.id.popup_share_access_read -> settingsPresenter.getExternalLink(ApiContract.ShareType.READ)
                R.id.popup_share_access_deny -> settingsPresenter.getExternalLink(ApiContract.ShareType.NONE)
                R.id.popup_share_access_comment -> settingsPresenter.getExternalLink(ApiContract.ShareType.COMMENT)
                R.id.popup_share_access_fill_forms -> settingsPresenter.getExternalLink(ApiContract.ShareType.FILL_FORMS)
            }
        }
    }

    companion object {

        private const val TAG_ITEM = "TAG_ITEM"
        private const val TAG_SHOW_POPUP = "TAG_SHOW_POPUP"
        private const val TAG_POSITION_POPUP = "TAG_POSITION_POPUP"

        fun newInstance(item: Item): SettingsFragment {
            return SettingsFragment().apply {
                arguments = Bundle(1).apply {
                    putSerializable(TAG_ITEM, item)
                }
            }
        }
    }
}