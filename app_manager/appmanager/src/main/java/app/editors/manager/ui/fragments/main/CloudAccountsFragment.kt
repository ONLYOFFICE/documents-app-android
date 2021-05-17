package app.editors.manager.ui.fragments.main

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.*
import android.widget.FrameLayout
import androidx.recyclerview.widget.LinearLayoutManager
import app.documents.core.account.CloudAccount
import app.editors.manager.R
import app.editors.manager.databinding.CloudsAccountsLayoutBinding
import app.editors.manager.mvp.presenters.main.CloudAccountState
import app.editors.manager.mvp.presenters.main.CloudAccountsPresenter
import app.editors.manager.mvp.views.main.CloudAccountsView
import app.editors.manager.ui.activities.login.PortalsActivity
import app.editors.manager.ui.activities.login.SignInActivity
import app.editors.manager.ui.activities.main.CloudsActivity.show
import app.editors.manager.ui.activities.main.IMainActivity
import app.editors.manager.ui.activities.main.MainActivity
import app.editors.manager.ui.activities.main.ProfileActivity
import app.editors.manager.ui.activities.main.SettingsActivity
import app.editors.manager.ui.adapters.CloudAccountsAdapter
import app.editors.manager.ui.dialogs.AccountContextDialog.OnAccountContextClickListener
import app.editors.manager.ui.fragments.base.BaseAppFragment
import app.editors.manager.ui.popup.CloudAccountPopup
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import lib.toolkit.base.ui.dialogs.common.CommonDialog
import moxy.presenter.InjectPresenter

class CloudAccountsFragment : BaseAppFragment(), CloudAccountsView, OnAccountContextClickListener {

    companion object {
        @JvmField
        val TAG: String = CloudAccountsFragment::class.java.simpleName

        private const val TAG_REMOVE = "TAG_REMOVE"

        fun newInstance(): CloudAccountsFragment {
            return CloudAccountsFragment()
        }
    }

    @InjectPresenter
    lateinit var presenter: CloudAccountsPresenter
    lateinit var activity: IMainActivity

    private var viewBinding: CloudsAccountsLayoutBinding? = null
    private var adapter: CloudAccountsAdapter? = null

    private var settingItem: MenuItem? = null
    private var selectAllItem: MenuItem? = null
    private var deselectAllItem: MenuItem? = null
    private var deleteItem: MenuItem? = null

    private val accountClickListener: ((account: CloudAccount) -> Unit) = { account ->
        presenter.accountClick(account)
    }
    private val accountLongClickListener: ((account: CloudAccount) -> Unit) = { account ->
        presenter.accountLongClick(account)
    }
    private val accountContextClickListener: ((account: CloudAccount, position: Int, view: View) -> Unit) =
        { account, position, view ->
            presenter.contextClick(account, position)
            if (isTablet) {
                getActivity()?.let {
                    CloudAccountPopup(requireContext()).apply {
                        setListener(this@CloudAccountsFragment)
                        setAccount(account)
                    }.showDropAt(view, it)
                }
            } else {
//                AccountContextDialog.newInstance(Json.encodeToString(account)).show(requireFragmentManager(), AccountContextDialog.TAG)
            }
        }
    private val addClickListener: (() -> Unit) = {
        PortalsActivity.showPortals(getActivity())
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is IMainActivity) {
            activity = context
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewBinding = null
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        viewBinding = CloudsAccountsLayoutBinding.inflate(inflater, container, false)
        return viewBinding?.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        initRecyclerView()

        presenter.getAccounts()
    }

    private fun initViews() {
        setHasOptionsMenu(true)
        activity.setAppBarStates(false)
        activity.showNavigationButton(false)
        activity.showActionButton(false)
        setActionBarTitle(getString(R.string.cloud_accounts_title))
    }

    private fun initRecyclerView() {
        (viewBinding?.accountsLayout?.layoutParams as FrameLayout.LayoutParams).setMargins(
            resources.getDimensionPixelSize(R.dimen.screen_left_right_padding),
            0,
            resources.getDimensionPixelSize(R.dimen.screen_left_right_padding),
            0
        )
        adapter = CloudAccountsAdapter(
            accountClickListener,
            accountLongClickListener,
            accountContextClickListener,
            addClickListener
        )
        viewBinding?.accountsRecyclerView?.layoutManager = LinearLayoutManager(requireContext())
        viewBinding?.accountsRecyclerView?.adapter = adapter
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.cloud_settings_menu, menu)
        settingItem = menu.findItem(R.id.settingsItem)
        selectAllItem = menu.findItem(R.id.selectAll)
        deselectAllItem = menu.findItem(R.id.deselect)
        deleteItem = menu.findItem(R.id.deleteSelected)
        setMenuState(adapter?.isSelectionMode != true)
    }

    private fun setMenuState(isSelect: Boolean) {
        if (isSelect) {
            settingItem?.isVisible = true
            selectAllItem?.isVisible = false
            deselectAllItem?.isVisible = false
            deleteItem?.isVisible = false
        } else {
            settingItem?.isVisible = false
            selectAllItem?.isVisible = true
            deselectAllItem?.isVisible = true
            deleteItem?.isVisible = true
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.settingsItem -> SettingsActivity.show(requireContext())
            R.id.selectAll -> adapter?.itemList?.let { presenter.selectAll(it) }
            R.id.deselect -> presenter.deselectAll()
            R.id.deleteSelected -> presenter.deleteAll()
        }
        return true
    }

    override fun onRender(state: CloudAccountState) {
        when (state) {
            is CloudAccountState.AccountLoadedState -> {
                adapter?.setItems(state.account.toMutableList())
            }

        }
    }

    override fun onAccountLogin() {
        hideDialog()
        if (context != null) {
            val intent = Intent(context, MainActivity::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_LAUNCH_ADJACENT
            }
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            requireContext().startActivity(intent)
        }
    }

    override fun onAcceptClick(dialogs: CommonDialog.Dialogs?, value: String?, tag: String?) {
        super.onAcceptClick(dialogs, value, tag)
        if (tag != null && tag == TAG_REMOVE) {
            presenter.removeAccount()
            hideDialog()
        }
        activity
    }

    override fun onCancelClick(dialogs: CommonDialog.Dialogs?, tag: String?) {
        super.onCancelClick(dialogs, tag)
        //        mMainActivity.onContextDialogClose();
    }

    override fun onWebDavLogin(account: CloudAccount) {
    }

    override fun onShowClouds() {
    }

    override fun onShowBottomDialog(account: CloudAccount?) {
    }

    override fun onShowWaitingDialog() {
    }

    override fun removeItem(position: Int) {
        adapter?.removeItem(position)
    }

    override fun onUpdateItem(account: CloudAccount, position: Int) {
        adapter?.updateItem(account, position)
    }

    override fun onSuccessLogin() {
        hideDialog()
        requireActivity().finish()
        show(context!!)
    }

    override fun onSignIn(portal: String?, login: String) {
        hideDialog()
        SignInActivity.showPortalSignIn(this, portal, login)
    }

    override fun onEmptyList() {
    }

    override fun onSelectionMode() {
    }

    override fun onDefaultState() {
    }

    override fun onSelectedItem(position: Int) {
    }

    override fun onActionBarTitle(title: String) {
    }

    override fun onNotifyItems() {
    }

    override fun onError(message: String?) {
        hideDialog()
        message?.let { showSnackBar(it) }
    }

    override fun onProfileClick(account: CloudAccount?) {
        ProfileActivity.show(requireActivity(), Json.encodeToString(account))
    }

    override fun onLogOutClick() {
        presenter.logout()
    }

    override fun onRemoveClick(account: CloudAccount?) {
        val account = presenter.contextAccount
        if (account != null) {
            showQuestionDialog(
                getString(R.string.dialog_remove_account_title),
                getString(R.string.dialog_remove_account_description, account.login, account.portal),
                getString(R.string.dialogs_question_accept_remove),
                getString(R.string.dialogs_common_cancel_button),
                TAG_REMOVE
            )
        } else {
            showQuestionDialog(
                getString(R.string.dialog_remove_account_title),
                getString(R.string.dialog_remove_account_description, "", ""),
                getString(R.string.dialogs_question_accept_remove),
                getString(R.string.dialogs_common_cancel_button),
                TAG_REMOVE
            )
        }
    }

    override fun onSignInClick() {
        presenter.signIn()
    }


//    @JvmField
//    @BindView(R.id.accountsLayout)
//    var mAccountsLayout: FrameLayout? = null
//
//    @JvmField
//    @BindView(R.id.accountsRecyclerView)
//    var mAccountsRecyclerView: RecyclerView? = null
//    private var mUnbinder: Unbinder? = null
//
//    @JvmField
//    @InjectPresenter
//    var mCloudAccountsPresenter: CloudAccountsPresenter? = null
//    private var mMainActivity: IMainActivity? = null
//    private var mAdapter: CloudAccountsAdapter? = null
//    private var mSettingItem: MenuItem? = null
//    private var mSelectAll: MenuItem? = null
//    private var mDeselect: MenuItem? = null
//    private var mDeleteAll: MenuItem? = null
//    private var mPopup: CloudAccountPopup? = null
//    private var mDialog: AccountContextDialog? = null
//    override fun onAttach(context: Context) {
//        super.onAttach(context)
//        mMainActivity = if (context is IMainActivity) {
//            context
//        } else {
//            throw RuntimeException(
//                CloudAccountsFragment::class.java.simpleName + " - must implement - " +
//                        MainActivity::class.java.simpleName
//            )
//        }
//    }
//
//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//        if (resultCode == Activity.RESULT_CANCELED && requestCode == SignInActivity.REQUEST_SIGN_IN) {
//            mCloudAccountsPresenter!!.restoreAccount()
//        }
//    }
//
//    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
//        val view = inflater.inflate(R.layout.clouds_accounts_layout, container, false)
//        mUnbinder = ButterKnife.bind(this, view)
//        return view
//    }
//
//    override fun onBackPressed(): Boolean {
//        return if (mPopup != null && mPopup!!.isVisible) {
//            mPopup!!.hide()
//            true
//        } else {
//            mCloudAccountsPresenter!!.onBackPressed()
//        }
//    }
//
//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//        setHasOptionsMenu(true)
//        setPadding()
//        initViews(savedInstanceState)
//        initRecyclerView()
//        mCloudAccountsPresenter!!.getAccounts()
//    }
//
//    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
//        super.onCreateOptionsMenu(menu, inflater)
//        inflater.inflate(R.menu.cloud_settings_menu, menu)
//        mSettingItem = menu.findItem(R.id.settingsItem)
//        mSelectAll = menu.findItem(R.id.selectAll)
//        mDeselect = menu.findItem(R.id.deselect)
//        mDeleteAll = menu.findItem(R.id.deleteSelected)
//        setMenuState(!mAdapter!!.isSelectionMode)
//    }
//
//    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//        when (item.itemId) {
//            R.id.settingsItem -> SettingsActivity.show(requireContext())
//            R.id.selectAll -> mCloudAccountsPresenter!!.selectAll(mAdapter!!.itemList)
//            R.id.deselect -> mCloudAccountsPresenter!!.deselectAll()
//            R.id.deleteSelected -> mCloudAccountsPresenter!!.deleteAll()
//        }
//        return true
//    }
//
//    override fun onDestroyView() {
//        super.onDestroyView()
//        if (mDialog != null) {
//            mDialog!!.dismiss()
//        }
//        if (mPopup != null && mPopup!!.isVisible) {
//            mPopup!!.hide()
//        }
//        if (mUnbinder != null) {
//            mUnbinder!!.unbind()
//        }
//        if (mAdapter != null) {
//            mAdapter!!.setOnAccountClick(null)
//            mAdapter!!.setOnAccountContextClick(null)
//            mAdapter!!.setOnAccountLongClick(null)
//            mAdapter!!.setOnAddAccountClick(null)
//        }
//    }
//
//
//    fun onWebDavLogin(account: AccountsSqlData) {
//        WebDavLoginActivity.show(activity, WebDavApi.Providers.valueOf(account.webDavProvider), account)
//    }
//
//    override fun onShowClouds() {
//        mMainActivity!!.getNavigationBottom().selectedItemId = R.id.menu_item_cloud
//    }
//
//
//    override fun onShowWaitingDialog() {
//        showWaitingDialog(
//            getString(R.string.dialogs_wait_title),
//            getString(R.string.dialogs_common_cancel_button), TAG
//        )
//    }
//
//    override fun removeItem(position: Int) {
//        mAdapter!!.removeItem(position)
//    }
//
//    fun onUpdateItem(account: AccountsSqlData) {
//        mAdapter!!.updateItem(account)
//    }
//
//
//    override fun onSignIn(portal: String, login: String) {
//        hideDialog()
//        //        mMainActivity.onContextDialogClose();
//        showPortalSignIn(this, portal, login)
//    }
//
//    override fun onEmptyList() {
//        mMainActivity!!.getNavigationBottom().selectedItemId = R.id.menu_item_setting
//    }
//
//    fun onSetAccounts(accounts: List<AccountsSqlData?>?) {
//        mAdapter!!.setItems(accounts)
//    }
//
//
//    override fun onProfileClick(account: AccountsSqlData) {
//        ProfileActivity.show(requireActivity(), account)
//    }
//
//    override fun onLogOutClick() {
//        mCloudAccountsPresenter!!.logout()
//    }
//
//
//    override fun onSignInClick() {
//        mCloudAccountsPresenter!!.signIn()
//    }
//
//    override fun onSelectionMode() {
//        mAdapter!!.isSelectionMode = true
//        mAdapter!!.notifyDataSetChanged()
//        mMainActivity!!.showNavigationButton(true)
//        setMenuState(false)
//    }
//
//    override fun onDefaultState() {
//        setActionBarTitle(getString(R.string.cloud_accounts_title))
//        mAdapter!!.isSelectionMode = false
//        mAdapter!!.notifyDataSetChanged()
//        mMainActivity!!.showNavigationButton(false)
//        setMenuState(true)
//    }
//
//    override fun onSelectedItem(position: Int) {
//        mAdapter!!.notifyItemChanged(position)
//    }
//
//    override fun onActionBarTitle(title: String) {
//        if (title == "0") {
//            mDeleteAll!!.isEnabled = false
//        } else {
//            mDeleteAll!!.isEnabled = true
//        }
//        setActionBarTitle(title)
//    }
//
//    override fun onNotifyItems() {
//        mAdapter!!.notifyDataSetChanged()
//    }
//
//    private fun setMenuState(isDefault: Boolean) {
//        if (mMenu != null) {
//            if (isDefault) {
//                mSettingItem!!.isVisible = true
//                mSelectAll!!.isVisible = false
//                mDeselect!!.isVisible = false
//                mDeleteAll!!.isVisible = false
//            } else {
//                mSettingItem!!.isVisible = false
//                mSelectAll!!.isVisible = true
//                mDeselect!!.isVisible = true
//                mDeleteAll!!.isVisible = true
//            }
//        }
//    }
//
//    private fun setPadding() {
//        val params = mAccountsLayout!!.layoutParams as FrameLayout.LayoutParams
//        params.setMargins(
//            resources.getDimensionPixelSize(R.dimen.screen_left_right_padding),
//            0,
//            resources.getDimensionPixelSize(R.dimen.screen_left_right_padding),
//            0
//        )
//    }
//
//    private fun initViews(state: Bundle?) {
//        if (state == null) {
//            setActionBarTitle(getString(R.string.cloud_accounts_title))
//        }
//        mMainActivity!!.showActionButton(false)
//        mMainActivity!!.setAppBarStates(false)
//        mMainActivity!!.showAccount(false)
//        mMainActivity!!.showNavigationButton(false)
//    }
}