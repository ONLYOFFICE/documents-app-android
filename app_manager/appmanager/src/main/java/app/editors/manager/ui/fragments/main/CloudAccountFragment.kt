package app.editors.manager.ui.fragments.main

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.FrameLayout
import androidx.recyclerview.selection.SelectionPredicates
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.selection.StorageStrategy
import app.documents.core.account.CloudAccount
import app.documents.core.network.ApiContract
import app.documents.core.webdav.WebDavApi
import app.editors.manager.R
import app.editors.manager.databinding.CloudsAccountsLayoutBinding
import app.editors.manager.dropbox.ui.fragments.DropboxSignInFragment
import app.editors.manager.googledrive.ui.fragments.GoogleDriveSignInFragment
import app.editors.manager.managers.utils.Constants
import app.editors.manager.mvp.models.account.Storage
import app.editors.manager.mvp.presenters.main.CloudAccountPresenter
import app.editors.manager.mvp.presenters.main.CloudAccountState
import app.editors.manager.mvp.views.main.CloudAccountView
import app.editors.manager.onedrive.managers.utils.OneDriveUtils
import app.editors.manager.onedrive.ui.fragments.OneDriveSignInFragment
import app.editors.manager.onedrive.ui.fragments.OneDriveSignInFragment.Companion.newInstance
import app.editors.manager.ui.activities.login.PortalsActivity
import app.editors.manager.ui.activities.login.SignInActivity
import app.editors.manager.ui.activities.login.WebDavLoginActivity
import app.editors.manager.ui.activities.main.IMainActivity
import app.editors.manager.ui.activities.main.MainActivity
import app.editors.manager.ui.activities.main.ProfileActivity
import app.editors.manager.ui.activities.main.SettingsActivity
import app.editors.manager.ui.adapters.AccountDetailsLookup
import app.editors.manager.ui.adapters.AccountKeyProvider
import app.editors.manager.ui.adapters.CloudAccountAdapter
import app.editors.manager.ui.dialogs.AccountContextDialog
import app.editors.manager.ui.fragments.base.BaseAppFragment
import app.editors.manager.ui.popup.CloudAccountPopup
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import lib.toolkit.base.ui.dialogs.common.CommonDialog
import moxy.presenter.InjectPresenter

class CloudAccountFragment : BaseAppFragment(),
    AccountContextDialog.OnAccountContextClickListener, CloudAccountView {

    companion object {
        val TAG: String = CloudAccountFragment::class.java.simpleName

        private const val TAG_REMOVE = "TAG_REMOVE"
        private const val TRACKER_ID = "accounts_id"

        fun newInstance(): CloudAccountFragment {
            return CloudAccountFragment()
        }
    }

    @InjectPresenter
    lateinit var presenter: CloudAccountPresenter

    private lateinit var activity: IMainActivity

    private var viewBinding: CloudsAccountsLayoutBinding? = null
    private var adapter: CloudAccountAdapter? = null

    private var settingItem: MenuItem? = null
    private var selectAllItem: MenuItem? = null
    private var deselectAllItem: MenuItem? = null
    private var deleteItem: MenuItem? = null

    private var selectedTracker: SelectionTracker<String>? = null

    val selectionObserver: SelectionTracker.SelectionObserver<String> = object :
        SelectionTracker.SelectionObserver<String>() {
        override fun onItemStateChanged(key: String, selected: Boolean) {
            super.onItemStateChanged(key, selected)
            if (selectedTracker?.selection?.isEmpty == true) {
                adapter?.selected = false
                onDefaultMode()
            } else {
                adapter?.selected = true
                onSelectionMode(selectedTracker?.selection?.size()?.toString() ?: "0")
            }
        }
    }

    private val accountClickListener: ((position: Int) -> Unit) = { position ->
        if (selectedTracker?.hasSelection()?.not() == true) {
            adapter?.itemList?.get(position)?.let {
                presenter.checkLogin(it)
            }
        }
    }

    private val accountContextClickListener: ((position: Int, view: View) -> Unit) =
        { position, view ->
            adapter?.itemList?.get(position)?.let { account ->
                presenter.contextAccount = account
                showAccountDialog(account, view)
            }
        }

    private val addClickListener: (() -> Unit) = {
        PortalsActivity.showPortals(this)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewBinding = null
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        presenter.getAccounts()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is IMainActivity) {
            activity = context
        } else {
            throw IllegalArgumentException(
                "${CloudAccountFragment::class.java.simpleName} must " +
                        "implement ${IMainActivity::class.java.simpleName}"
            )
        }
    }

    override fun onBackPressed(): Boolean {
        return if (selectedTracker?.clearSelection() == true) {
            onDefaultMode()
            true
        } else {
            super.onBackPressed()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        selectedTracker?.onSaveInstanceState(outState)
        super.onSaveInstanceState(outState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        viewBinding = CloudsAccountsLayoutBinding.inflate(inflater, container, false)
        return viewBinding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)
        initViews()
        initRecyclerView(savedInstanceState)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.cloud_settings_menu, menu)
        menu.apply {
            settingItem = findItem(R.id.settingsItem)
            selectAllItem = findItem(R.id.selectAll)
            deselectAllItem = findItem(R.id.deselect)
            deleteItem = findItem(R.id.deleteSelected)
            selectedTracker?.let {
                setMenuState(!it.hasSelection())
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.settingsItem -> SettingsActivity.show(requireContext())
            R.id.selectAll -> selectedTracker?.setItemsSelected(adapter?.getIds() ?: emptyList(), true)
            R.id.deselect -> selectedTracker?.setItemsSelected(adapter?.getIds() ?: emptyList(), false)
            R.id.deleteSelected -> presenter.deleteSelected(selectedTracker?.selection?.toList())
        }
        return true
    }

    override fun onRender(state: CloudAccountState) {
        when (state) {
            is CloudAccountState.AccountLoadedState -> {
                if (state.account.isEmpty()) {
                    setEmptyState()
                } else {
                    adapter?.selectedTracker = selectedTracker
                    adapter?.setItems(state.account.toMutableList())
                    state.state?.let {
                        selectedTracker?.onRestoreInstanceState(it)
                    }
                }
            }
        }
    }

    private fun setEmptyState() {
        adapter?.setItems(emptyList<CloudAccount>().toMutableList())
    }

    private fun initViews() {
        activity.setAppBarStates(false)
        activity.showNavigationButton(false)
        activity.showActionButton(false)
        setActionBarTitle(getString(R.string.cloud_accounts_title))
    }

    private fun initRecyclerView(savedInstanceState: Bundle?) {
        (viewBinding?.accountsLayout?.layoutParams as FrameLayout.LayoutParams).setMargins(
            resources.getDimensionPixelSize(lib.toolkit.base.R.dimen.screen_left_right_padding),
            0,
            resources.getDimensionPixelSize(lib.toolkit.base.R.dimen.screen_left_right_padding),
            0
        )

        adapter = CloudAccountAdapter(
            accountClickListener,
            accountContextClickListener,
            addClickListener
        )
        viewBinding?.accountsRecyclerView?.adapter = adapter

        selectedTracker = SelectionTracker.Builder(
            TRACKER_ID,
            viewBinding?.accountsRecyclerView!!,
            AccountKeyProvider(viewBinding?.accountsRecyclerView),
            AccountDetailsLookup(viewBinding?.accountsRecyclerView),
            StorageStrategy.createStringStorage()
        ).withSelectionPredicate(SelectionPredicates.createSelectAnything())
            .build()
        selectedTracker?.addObserver(selectionObserver)

        presenter.getAccounts(savedInstanceState)
    }

    private fun onSelectionMode(count: String) {
        activity.showNavigationButton(true)
        setActionBarTitle(count)
        setMenuState(false)
    }

    private fun onDefaultMode() {
        activity.showNavigationButton(false)
        setActionBarTitle(getString(R.string.cloud_accounts_title))
        setMenuState(true)
    }

    override fun onSuccessLogin() {
        hideDialog()
        requireContext().startActivity(Intent(requireContext(), MainActivity::class.java).apply {
//             if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//                 flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_LAUNCH_ADJACENT
//             }
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        })
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

    private fun showAccountDialog(account: CloudAccount, view: View) {
        if (isTablet) {
            CloudAccountPopup(requireActivity()).apply {
                setListener(this@CloudAccountFragment)
                setAccount(account)
            }.showDropAt(view, requireActivity())
        } else {
            AccountContextDialog.newInstance(Json.encodeToString(account), account.token)
                .show(requireFragmentManager(), AccountContextDialog.TAG)
        }
    }

    override fun onCloseCommonDialog() {
        super.onCloseCommonDialog()
        presenter.contextAccount = null
    }

    override fun onProfileClick(account: CloudAccount?) {
        startActivityForResult(Intent(context, ProfileActivity::class.java).apply {
            putExtra(ProfileFragment.KEY_ACCOUNT, Json.encodeToString(account))
        }, ProfileActivity.REQUEST_PROFILE)
    }

    override fun onLogOutClick() {
        presenter.logOut()
    }

    override fun onRemoveClick(account: CloudAccount?) {
        account?.let {
            showQuestionDialog(
                getString(R.string.dialog_remove_account_title),
                getString(R.string.dialog_remove_account_description, account.login, account.portal),
                getString(R.string.dialogs_question_accept_remove),
                getString(R.string.dialogs_common_cancel_button),
                TAG_REMOVE
            )
        }
    }

    override fun onSignInClick() {
        presenter.checkContextLogin()
    }

    override fun onWebDavLogin(account: String, provider: WebDavApi.Providers) {
        WebDavLoginActivity.show(requireActivity(), provider, account)
    }

    override fun onAccountLogin(portal: String, login: String) {
        SignInActivity.showPortalSignIn(this, portal, login)
    }

    override fun onGoogleDriveLogin() {
        val storage = Storage(
            ApiContract.Storage.GOOGLEDRIVE,
            Constants.Google.COM_CLIENT_ID,
            Constants.Google.COM_REDIRECT_URL
        )
        showFragment(GoogleDriveSignInFragment.newInstance(storage), GoogleDriveSignInFragment.TAG, false)
    }

    override fun onDropboxLogin() {
        val storage = Storage(
            ApiContract.Storage.DROPBOX,
            Constants.DropBox.COM_CLIENT_ID,
            Constants.DropBox.COM_REDIRECT_URL
        )
        showFragment(newInstance(storage), DropboxSignInFragment.TAG, false)
    }

    override fun onOneDriveLogin() {
        val storage = Storage(
            OneDriveUtils.ONEDRIVE_STORAGE,
            Constants.OneDrive.COM_CLIENT_ID,
            Constants.OneDrive.COM_REDIRECT_URL
        )
        showFragment(newInstance(storage), OneDriveSignInFragment.TAG, false)
    }

    override fun onAcceptClick(dialogs: CommonDialog.Dialogs?, value: String?, tag: String?) {
        super.onAcceptClick(dialogs, value, tag)
        if (tag == TAG_REMOVE) {
            presenter.deleteAccount()
        }
        hideDialog()
    }

    override fun onError(message: String?) {
        message?.let {
            showSnackBar(message)
        }
    }

    override fun onUnauthorized(message: String?) {
        message?.let {
            showSnackBar(it)
        }
        Log.d(TAG, "onUnauthorized: ")
    }

}
