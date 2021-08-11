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

    private var _viewBinding: FragmentShareSettingsListBinding? = null
    private val viewBinding get() = _viewBinding!!

    private var _popupBinding: IncludeButtonPopupBinding? = null
    private val popupBinding get() = _popupBinding!!

    private var _shareSettingsBinding: IncludeShareSettingsHeaderBinding? = null
    private val shareSettingsBinding get() = _shareSettingsBinding!!

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
        if (sharePopup != null && sharePopup!!.isShowing) {
            outState.putBoolean(TAG_SHOW_POPUP, sharePopup!!.isShowing)
            outState.putInt(TAG_POSITION_POPUP, settingsPresenter.sharePosition)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)
        _viewBinding = FragmentShareSettingsListBinding.inflate(inflater, container, false)
        _popupBinding = viewBinding.includeShareSettingsHeader.shareSettingsAccessButtonLayout
        _shareSettingsBinding = viewBinding.includeShareSettingsHeader
        return viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init(savedInstanceState)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (sharePopup != null && sharePopup!!.isShowing) {
            sharePopup?.hide()
        }
        _viewBinding = null
        _popupBinding = null
        _shareSettingsBinding = null
    }

    override fun onBackPressed(): Boolean {
        if (sharePopup != null && sharePopup!!.isShowing) {
            sharePopup?.hide()
            return true
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
        viewBinding.apply {
            shareSettingsAddItem.setOnClickListener { settingsPresenter.addShareItems() }
        }
        shareSettingsBinding.apply {
            shareSettingsAccessButtonLayout.root.setOnClickListener {
                showAccessPopup()
            }
            shareSettingsExternalCopyLink.setOnClickListener {
                copySharedLinkToClipboard(
                    settingsPresenter.externalLink,
                    getString(R.string.share_clipboard_external_copied)
                )
            }
            shareSettingsExternalSendLink.setOnClickListener {
                settingsPresenter.sendLink(
                    settingsPresenter.externalLink
                )
            }
        }
    }

    override fun onError(message: String?) {
        placeholderViews?.setTemplatePlaceholder(PlaceholderViews.Type.CONNECTION)
        viewBinding.shareSettingsListSwipeRefresh.isRefreshing = false
        showSnackBar(checkNotNull(message))
    }

    override fun onUnauthorized(message: String?) {
        requireActivity().finish()
        show(requireContext())
    }

    override fun onGetShare(list: List<ViewType>, accessCode: Int) {
        viewBinding.shareSettingsListSwipeRefresh.isRefreshing = false
        shareSettingsAdapter?.setItems(list)
        setExternalViewState(accessCode, false)
    }

    override fun onGetShareItem(entity: ViewType, position: Int, accessCode: Int) {
        viewBinding.shareSettingsListSwipeRefresh.isRefreshing = false
        shareSettingsAdapter!!.setItem(entity, position)
        setExternalViewState(accessCode, false)
    }

    override fun onRemove(share: ShareUi, sharePosition: Int) {
        viewBinding.shareSettingsListSwipeRefresh.isRefreshing = false
        shareSettingsAdapter?.removeItem(share)
        if (shareSettingsAdapter!!.itemList.size > 1 && shareSettingsAdapter!!.getItem(0) is Header
            && shareSettingsAdapter!!.getItem(1) is Header
        ) {
            shareSettingsAdapter!!.removeHeader(getString(R.string.share_goal_user))
        }
        showSnackBarWithAction(
            getString(R.string.share_snackbar_remove_user),
            getString(R.string.snackbar_undo)
        ) {
            settingsPresenter.setItemAccess(share.access)
            shareSettingsAdapter?.addItem(share)
        }
    }

    override fun onExternalAccess(accessCode: Int, isMessage: Boolean) {
        setExternalViewState(accessCode, isMessage)
    }

    override fun onInternalLink(internalLink: String) {
        copySharedLinkToClipboard(internalLink, getString(R.string.share_clipboard_internal_copied))
    }

    override fun onItemType(isFolder: Boolean) {
        shareSettingsBinding.shareSettingsHeaderLayout.visibility = if (isFolder) View.GONE else View.VISIBLE
    }

    override fun onAddShare(item: Item) {
        showFragment(AddPagerFragment.newInstance(item), AddFragment.TAG, false)
    }

    override fun onPlaceholderState(type: PlaceholderViews.Type) {
        placeholderViews?.setTemplatePlaceholder(type)
    }

    override fun onActionButtonState(isVisible: Boolean) {
        if (isVisible) {
            viewBinding.shareSettingsAddItem.show()
        } else {
            viewBinding.shareSettingsAddItem.hide()
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
            if (context != null) {
                sharePopup = SharePopup(requireContext(), R.layout.popup_share_menu)
                sharePopup?.setContextListener(mListContextListener)
                if (settingsPresenter.item is CloudFolder) {
                    sharePopup?.setIsFolder(true)
                } else {
                    val extension =
                        getExtension(getExtensionFromPath(settingsPresenter.item!!.title))
                    sharePopup?.setIsDoc(extension === StringUtils.Extension.DOC)
                }
                sharePopup?.setFullAccess(true)
                sharePopup?.showDropAt(view, requireActivity())
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
        viewBinding.apply {
            shareSettingsAddItem.hide()
            placeholderViews = PlaceholderViews(viewBinding.placeholderLayout.root)
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
        if (savedInstanceState == null || ModelShareStack.getInstance().isRefresh) {
            getSharedItems()
        } else {
            settingsPresenter.updateActionButtonState()
            settingsPresenter.updateSharedListState()
        }
        if (savedInstanceState != null && savedInstanceState.containsKey(TAG_SHOW_POPUP)
            && savedInstanceState.containsKey(TAG_POSITION_POPUP)
        ) {
            settingsPresenter.setIsPopupShow(true)
        } else {
            settingsPresenter.setIsPopupShow(false)
        }
    }

    private fun getSharedItems() {
        viewBinding.shareSettingsListSwipeRefresh.isRefreshing = true
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
        popupBinding.buttonPopupImage.setImageResource(iconRes)
        if (isMessage) {
            showSnackBar(messageRes)
        }
    }

    override fun onButtonState(isVisible: Boolean) {
        shareSettingsBinding.shareSettingsExternalAccessLayout.let { shareSettingsLayout ->
            TransitionManager.beginDelayedTransition(shareSettingsLayout)
            shareSettingsLayout.visibility = if (isVisible) View.VISIBLE else View.GONE
        }
    }

    override fun onPopupState(state: Boolean) {
        popupBinding.buttonPopupImage.visibility = View.GONE
        popupBinding.buttonPopupArrow.visibility = View.GONE
    }

    override fun onShowPopup(sharePosition: Int) {
        viewBinding.shareMainListOfItems.let { recyclerView: RecyclerView? ->
            recyclerView?.post {
                if (sharePosition != 0) {
                    setPopup(
                        recyclerView.layoutManager?.findViewByPosition(sharePosition)!!
                            .findViewById(R.id.button_popup_arrow)
                    )
                } else {
                    showAccessPopup()
                }
            } ?: setPopup(viewBinding.shareSettingsListContentLayout)
        }
    }

    private fun showAccessPopup() {
        sharePopup = SharePopup(requireContext(), R.layout.popup_share_menu)
        sharePopup?.setContextListener(mExternalContextListener)
        sharePopup?.setExternalLink()
        sharePopup?.setFullAccess(false)
        if (settingsPresenter.item is CloudFolder) {
            sharePopup?.setIsFolder(true)
        } else {
            val extension = getExtension(getExtensionFromPath(settingsPresenter.item!!.title))
            sharePopup?.setIsDoc(extension === StringUtils.Extension.DOC)
        }
        sharePopup?.showDropAt(
            shareSettingsBinding.shareSettingsAccessButtonLayout.root,
            requireActivity()
        )
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
            val settingsFragment = SettingsFragment()
            val bundle = Bundle(1)
            bundle.putSerializable(TAG_ITEM, item)
            settingsFragment.arguments = bundle
            return settingsFragment
        }
    }
}