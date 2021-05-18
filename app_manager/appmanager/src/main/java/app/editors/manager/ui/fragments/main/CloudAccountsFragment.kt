package app.editors.manager.ui.fragments.main

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.*
import android.widget.FrameLayout
import androidx.recyclerview.widget.LinearLayoutManager
import app.documents.core.account.CloudAccount
import app.documents.core.webdav.WebDavApi
import app.editors.manager.R
import app.editors.manager.databinding.CloudsAccountsLayoutBinding
import app.editors.manager.mvp.presenters.main.CloudAccountState
import app.editors.manager.mvp.presenters.main.CloudAccountsPresenter
import app.editors.manager.mvp.views.main.CloudAccountsView
import app.editors.manager.ui.activities.login.PortalsActivity
import app.editors.manager.ui.activities.login.SignInActivity.Companion.showPortalSignIn
import app.editors.manager.ui.activities.login.WebDavLoginActivity
import app.editors.manager.ui.activities.main.CloudsActivity.show
import app.editors.manager.ui.activities.main.IMainActivity
import app.editors.manager.ui.activities.main.MainActivity
import app.editors.manager.ui.activities.main.ProfileActivity
import app.editors.manager.ui.activities.main.SettingsActivity
import app.editors.manager.ui.adapters.CloudAccountsAdapter
import app.editors.manager.ui.dialogs.AccountBottomDialog
import app.editors.manager.ui.dialogs.AccountContextDialog.OnAccountContextClickListener
import app.editors.manager.ui.fragments.base.BaseAppFragment
import app.editors.manager.ui.popup.CloudAccountPopup
import kotlinx.serialization.decodeFromString
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

    private var clickedPosition: Int? = null

    private val accountClickListener: ((account: CloudAccount) -> Unit) = { account ->
        if(!adapter?.isSelectionMode!!) {
            presenter.accountClick(account)
        } else {
            adapter?.itemList?.indexOf(account)?.let { presenter.checkLogin(account, it) }
        }
    }
    private val accountLongClickListener: ((account: CloudAccount) -> Unit) = { account ->
        if(!adapter?.isSelectionMode!!) {
            presenter.accountLongClick(account)
        } else {
            adapter?.itemList?.indexOf(account)?.let { presenter.checkLogin(account, it) }
        }

    }
    private val accountContextClickListener: ((account: CloudAccount, position: Int, view: View) -> Unit) =
        { account, position, view ->
            presenter.contextClick(account, position)
            clickedPosition = position
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

    override fun onBackPressed(): Boolean {
        return presenter.onBackPressed()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when(resultCode) {
            Activity.RESULT_CANCELED -> clickedPosition?.let { removeItem(it) }
            Activity.RESULT_OK -> clickedPosition?.let {
                onUpdateItem(Json.decodeFromString(data?.getStringExtra(ProfileFragment.KEY_ACCOUNT)!!),
                    it
                )
            }
        }
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
//            is CloudAccountState.DefaultState -> {
//
//            }
//            is CloudAccountState.SelectedState -> {
//
//            }
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
        WebDavLoginActivity.show(requireActivity(), account.provider?.let { WebDavApi.Providers.valueOf(it) }, Json.encodeToString(account))
    }

    override fun onShowClouds() {
        activity.getNavigationBottom().selectedItemId = R.id.menu_item_cloud
    }

    override fun onShowBottomDialog(account: CloudAccount?) {
    }

    override fun onShowWaitingDialog() {
        showWaitingDialog(
            getString(R.string.dialogs_wait_title),
            getString(R.string.dialogs_common_cancel_button), AccountBottomDialog.TAG
        )
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
        show(context)
    }

    override fun onSignIn(portal: String?, login: String) {
        hideDialog()
        showPortalSignIn(this, portal, login)
    }

    override fun onEmptyList() {
        activity.getNavigationBottom().selectedItemId = R.id.menu_item_setting
    }

    override fun onSelectionMode() {
        adapter?.isSelectionMode = true
        adapter?.notifyDataSetChanged()
        activity.showNavigationButton(true)
        setMenuState(false)
    }

    override fun onDefaultState() {
        setActionBarTitle(getString(R.string.cloud_accounts_title))
        activity.showNavigationButton(false)
        adapter?.isSelectionMode = false
        adapter?.notifyDataSetChanged()
        setMenuState(true)
    }

    override fun onSelectedItem(position: Int) {
        adapter?.notifyItemChanged(position)
    }

    override fun onActionBarTitle(title: String) {
        setActionBarTitle(title)
    }

    override fun onNotifyItems() {
        adapter?.notifyDataSetChanged()
    }

    override fun onError(message: String?) {
        hideDialog()
        message?.let { showSnackBar(it) }
    }

    override fun onProfileClick(account: CloudAccount?) {
        startActivityForResult(Intent(context, ProfileActivity::class.java).apply {
            putExtra(ProfileFragment.KEY_ACCOUNT, Json.encodeToString(account))
        }, ProfileActivity.REQUEST_PROFILE)
    }

    override fun onLogOutClick() {
        presenter.logout()
    }

    override fun onRemoveClick(account: CloudAccount?) {
        TODO("Not yet implemented")
    }

    override fun onSignInClick() {
        presenter.signIn()
    }
}