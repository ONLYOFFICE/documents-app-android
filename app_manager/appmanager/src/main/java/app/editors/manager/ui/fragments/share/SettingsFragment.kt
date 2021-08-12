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
import app.editors.manager.mvp.models.list.Header
import app.editors.manager.mvp.models.models.ModelShareStack
import app.editors.manager.mvp.models.ui.ShareUi
import app.editors.manager.mvp.models.ui.ViewType
import app.editors.manager.mvp.presenters.share.SettingsPresenter
import app.editors.manager.mvp.views.share.SettingsView
import app.editors.manager.ui.activities.main.MainActivity.Companion.show
import app.editors.manager.ui.activities.main.ShareActivity
import app.editors.manager.ui.adapters.share.ShareAdapter
import app.editors.manager.ui.fragments.base.BaseAppFragment
import app.editors.manager.ui.views.custom.PlaceholderViews
import app.editors.manager.ui.views.popup.SharePopup
import lib.toolkit.base.managers.utils.StringUtils
import lib.toolkit.base.managers.utils.StringUtils.getExtension
import lib.toolkit.base.managers.utils.StringUtils.getExtensionFromPath
import moxy.presenter.InjectPresenter

class SettingsFragment : BaseAppFragment(), SettingsView, OnRefreshListener {

    @InjectPresenter
    lateinit var settingsPresenter: SettingsPresenter

    private var shareItem: MenuItem? = null
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
        sharePopup?.apply {
            if (isShowing) {
                outState.putBoolean(TAG_SHOW_POPUP, isShowing)
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
        headerBinding = viewBinding?.includeShareSettingsHeader
        popupBinding = headerBinding?.shareSettingsAccessButtonLayout
        return viewBinding?.root!!
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init(savedInstanceState)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        sharePopup?.apply {
            if (isShowing)
                hide()
        }
        shareActivity = null
        viewBinding = null
        popupBinding = null
        headerBinding = null
    }

    override fun onBackPressed(): Boolean {
        sharePopup?.apply {
            if (isShowing) {
                hide()
                return true
            }
        }
        return super.onBackPressed()
    }

    override fun onCreateOptionsMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.share_settings, menu)
        shareItem = menu.findItem(R.id.menu_share_settings)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.menu_share_settings) settingsPresenter.internalLink
        return super.onOptionsItemSelected(item)
    }

    private fun initClickListeners() {
        viewBinding?.shareSettingsAddItem?.setOnClickListener { settingsPresenter.addShareItems() }
        headerBinding?.apply {
            shareSettingsAccessButtonLayout.root.setOnClickListener { showAccessPopup() }
            shareSettingsExternalCopyLink.setOnClickListener {
                copySharedLinkToClipboard(
                    settingsPresenter.externalLink,
                    getString(R.string.share_clipboard_external_copied))
            }
            shareSettingsExternalSendLink.setOnClickListener {
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

    override fun onGetShareItem(entity: ViewType, position: Int, accessCode: Int) {
        viewBinding?.shareSettingsListSwipeRefresh?.isRefreshing = false
        shareSettingsAdapter?.setItem(entity, position)
        setExternalViewState(accessCode, false)
    }

    override fun onRemove(share: ShareUi, sharePosition: Int) {
        viewBinding?.shareSettingsListSwipeRefresh?.isRefreshing = false
        shareSettingsAdapter?.let { adapter ->
            adapter.removeItem(share)
            if (adapter.itemList.size > 1 && adapter.getItem(0) is Header
                && adapter.getItem(1) is Header
            ) {
                adapter.removeHeader(getString(R.string.share_goal_user))
            }
            showSnackBarWithAction(getString(R.string.share_snackbar_remove_user),
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
        placeholderViews?.setTemplatePlaceholder(type)
    }

    override fun onActionButtonState(isVisible: Boolean) {
        viewBinding?.apply {
            if (isVisible) {
                shareSettingsAddItem.show()
            } else {
                shareSettingsAddItem.hide()
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
        setPopup(view)
    }

    private fun setPopup(view: View?) {
        view?.post {
            sharePopup = SharePopup(requireContext(), R.layout.popup_share_menu)
            sharePopup?.let { popup ->
                popup.setContextListener(mListContextListener)
                if (settingsPresenter.item is CloudFolder) {
                    popup.setIsFolder(true)
                } else {
                    val extension = settingsPresenter.item?.let { item ->
                        getExtension(getExtensionFromPath(item.title))
                    }
                    popup.setIsDoc(extension === StringUtils.Extension.DOC)
                }
                popup.setFullAccess(true)
                popup.showDropAt(view, requireActivity())
            }
        }
    }

    private fun init(savedInstanceState: Bundle?) {
        setActionBarTitle(getString(R.string.share_title_main))
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setHomeButtonEnabled(true)
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
        viewBinding?.apply {
            shareSettingsAddItem.hide()
            placeholderViews = PlaceholderViews(placeholderLayout.root)
            placeholderViews?.setViewForHide(shareMainListOfItems)
            shareSettingsListSwipeRefresh.setOnRefreshListener(this@SettingsFragment)
            shareSettingsListSwipeRefresh.setColorSchemeColors(
                ContextCompat.getColor(requireContext(), R.color.colorAccent)
            )
            shareSettingsAdapter = ShareAdapter { view, integer ->
                onItemContextClick(view, integer)
            }
            shareMainListOfItems.layoutManager = LinearLayoutManager(context)
            shareMainListOfItems.adapter = shareSettingsAdapter
            ViewCompat.setNestedScrollingEnabled(shareMainListOfItems, false)
        }
    }

    private fun restoreViews(savedInstanceState: Bundle?) {
        settingsPresenter.updateSharedExternalState(false)
        settingsPresenter.updatePlaceholderState()
        settingsPresenter.updateHeaderState()

        savedInstanceState?.apply {
            if (ModelShareStack.getInstance().isRefresh) {
                getSharedItems()
            } else {
                settingsPresenter.updateActionButtonState()
                settingsPresenter.updateSharedListState()
            }

            if (containsKey(TAG_SHOW_POPUP)
                && containsKey(TAG_POSITION_POPUP)
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


    override fun onShowPopup(sharePosition: Int) {
        viewBinding?.shareMainListOfItems.let { recyclerView: RecyclerView? ->
            recyclerView?.post {
                if (sharePosition != 0) {
                    setPopup(
                        recyclerView.layoutManager?.findViewByPosition(sharePosition)
                            ?.findViewById(R.id.button_popup_arrow)
                    )
                } else {
                    showAccessPopup()
                }
            } ?: setPopup(viewBinding?.shareSettingsListContentLayout)
        }
    }

    private fun showAccessPopup() {
        sharePopup = SharePopup(requireContext(), R.layout.popup_share_menu)
        sharePopup?.apply {
            setContextListener(mExternalContextListener)
            setExternalLink()
            setFullAccess(false)
            if (settingsPresenter.item is CloudFolder) {
                setIsFolder(true)
            } else {
                val extension = getExtension(getExtensionFromPath(settingsPresenter.item?.title!!))
                setIsDoc(extension === StringUtils.Extension.DOC)
            }
            showDropAt(
                headerBinding?.shareSettingsAccessButtonLayout?.root!!,
                requireActivity()
            )
        }
    }

    override fun onSendLink(intent: Intent) {
        startActivity(Intent.createChooser(intent, getString(R.string.operation_share_send_link)))
    }

    private val mListContextListener = SharePopup.PopupContextListener { v, sharePopup ->
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
    private val mExternalContextListener = SharePopup.PopupContextListener { v, sharePopup ->
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

    companion object {

        private const val TAG_ITEM = "TAG_ITEM"
        private const val TAG_SHOW_POPUP = "TAG_SHOW_POPUP"
        private const val TAG_POSITION_POPUP = "TAG_POSITION_POPUP"
        private val TAG = SettingsFragment::class.java.simpleName

        fun newInstance(item: Item): SettingsFragment {
            return SettingsFragment().apply {
                arguments = Bundle(1).apply {
                    putSerializable(TAG_ITEM, item)
                }
            }
        }
    }
}