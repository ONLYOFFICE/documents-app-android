package app.editors.manager.ui.fragments.main

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.selection.SelectionPredicates
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.selection.StorageStrategy
import app.documents.core.model.cloud.CloudAccount
import app.documents.core.model.cloud.WebdavProvider
import app.documents.core.network.common.utils.DropboxUtils
import app.documents.core.network.common.utils.GoogleDriveUtils
import app.documents.core.network.common.utils.OneDriveUtils
import app.editors.manager.R
import app.editors.manager.databinding.CloudsAccountsLayoutBinding
import app.editors.manager.mvp.presenters.main.CloudAccountPresenter
import app.editors.manager.mvp.presenters.main.CloudAccountState
import app.editors.manager.mvp.views.main.CloudAccountView
import app.editors.manager.ui.activities.login.PortalsActivity
import app.editors.manager.ui.activities.login.SignInActivity
import app.editors.manager.ui.activities.login.WebDavLoginActivity
import app.editors.manager.ui.activities.main.AccountsActivity
import app.editors.manager.ui.activities.main.IMainActivity
import app.editors.manager.ui.activities.main.MainActivity
import app.editors.manager.ui.adapters.AccountDetailsLookup
import app.editors.manager.ui.adapters.AccountKeyProvider
import app.editors.manager.ui.adapters.CloudAccountAdapter
import app.editors.manager.ui.dialogs.AccountContextDialog
import app.editors.manager.ui.dialogs.fragments.IBaseDialogFragment
import app.editors.manager.ui.fragments.base.BaseAppFragment
import app.editors.manager.ui.fragments.base.StorageLoginFragment
import app.editors.manager.ui.fragments.login.EnterprisePortalFragment
import app.editors.manager.ui.popup.CloudAccountPopup
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import lib.toolkit.base.managers.utils.FragmentUtils
import lib.toolkit.base.managers.utils.UiUtils
import lib.toolkit.base.managers.utils.contains
import lib.toolkit.base.ui.activities.base.BaseActivity
import lib.toolkit.base.ui.dialogs.common.CommonDialog
import moxy.presenter.InjectPresenter

class CloudAccountFragment : BaseAppFragment(),
    AccountContextDialog.OnAccountContextClickListener,
    BaseActivity.OnBackPressFragment, CloudAccountView {

    companion object {
        val TAG: String = CloudAccountFragment::class.java.simpleName

        private const val TRACKER_ID = "accounts_id"
        private const val WAITING_DIALOG_TAG = "WAITING_DIALOG_TAG"

        const val REQUEST_PROFILE = "request_profile"
        const val RESULT_SIGN_IN = "result_sign_in"
        const val RESULT_LOG_OUT = "result_log_out"


        fun newInstance(): CloudAccountFragment {
            return CloudAccountFragment()
        }
    }

    @InjectPresenter
    lateinit var presenter: CloudAccountPresenter

    private var viewBinding: CloudsAccountsLayoutBinding? = null
    private var adapter: CloudAccountAdapter? = null
    private var mainActivity: IMainActivity? = null

    private var selectAllItem: MenuItem? = null
    private var deselectAllItem: MenuItem? = null
    private var deleteItem: MenuItem? = null

    private var selectedTracker: SelectionTracker<String>? = null

    private val accountDialogFragment: IBaseDialogFragment? get() = getDialogFragment()

    private val selectionObserver: SelectionTracker.SelectionObserver<String> = object :
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

    override fun onAttach(context: Context) {
        if (isTablet) {
            mainActivity = try {
                context as IMainActivity
            } catch (e: ClassCastException) {
                throw RuntimeException(
                    "If device is tablet " + this::class.java.simpleName
                            + " - must implement - " + IMainActivity::class.java.simpleName
                )
            }
        }
        super.onAttach(context)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewBinding = null
    }

    override fun onBackPressed(): Boolean {
        return if (selectedTracker?.clearSelection() == true) {
            onDefaultMode()
            true
        } else {
            false
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
        initRecyclerView(savedInstanceState)
        parentFragmentManager.setFragmentResultListener(
            REQUEST_PROFILE,
            requireActivity()
        ) { _, bundle ->
            when {
                bundle.contains(RESULT_LOG_OUT) -> bundle.getString(RESULT_LOG_OUT)?.let(presenter::logOut)
                bundle.contains(RESULT_SIGN_IN) -> bundle.getString(RESULT_SIGN_IN)?.let(presenter::checkLogin)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (isTablet) {
            accountDialogFragment?.setToolbarTitle(getString(R.string.cloud_accounts_title))
            accountDialogFragment?.setToolbarNavigationIcon(isClose = true)
        } else {
            setActionBarTitle(getString(R.string.cloud_accounts_title))
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.cloud_settings_menu, menu)
        if (!isTablet) {
            menu.apply {
                selectAllItem = findItem(R.id.selectAll)
                deselectAllItem = findItem(R.id.deselect)
                deleteItem = findItem(R.id.deleteSelected)
                selectedTracker?.let {
                    setMenuState(!it.hasSelection())
                }
            }
        } else {
            setTabletToolbar()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.selectAll -> selectedTracker?.setItemsSelected(adapter?.getIds().orEmpty(), true)
            R.id.deselect -> selectedTracker?.setItemsSelected(adapter?.getIds().orEmpty(), false)
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
                    adapter?.setItems(state.account.toMutableList(), presenter.getOnlineAccountId())
                    state.state?.let {
                        selectedTracker?.onRestoreInstanceState(it)
                    }
                }
                if (presenter.getOnlineAccountId().isEmpty()) {
                    if (isTablet) {
                        mainActivity?.onLogOut()
                    } else {
                        requireActivity().setResult(AccountsActivity.RESULT_NO_LOGGED_IN_ACCOUNTS)
                    }
                }
            }
        }
    }

    override fun onSuccessLogin() {
        hideDialog()
        MainActivity.show(requireContext())
    }

    override fun onWaiting() {
        showWaitingDialog(
            title = getString(R.string.dialogs_wait_title),
            cancelButton = getString(R.string.dialogs_common_cancel_button),
            tag = WAITING_DIALOG_TAG
        )
    }

    override fun onHideDialog() {
        hideDialog(forceHide = true)
    }

    override fun onCancelClick(dialogs: CommonDialog.Dialogs?, tag: String?) {
        super.onCancelClick(dialogs, tag)
        if (tag == WAITING_DIALOG_TAG) {
            presenter.cancelRequest()
        }
    }

    override fun onProfileClick() {
        presenter.contextAccount?.let { account ->
            FragmentUtils.showFragment(
                parentFragmentManager,
                ProfileFragment.newInstance(
                    account = Json.encodeToString(account),
                    isOnline = presenter.getOnlineAccountId() == account.id
                ),
                R.id.frame_container,
                ProfileFragment.TAG
            )
        }
    }

    override fun onRemoveClick() {
        presenter.contextAccount?.let { account ->
            UiUtils.showQuestionDialog(
                context = requireContext(),
                title = getString(R.string.dialog_remove_account_title) + "?",
                description = getString(R.string.dialog_remove_account_description, account.login, account.portal.url),
                acceptTitle = getString(R.string.dialogs_question_accept_remove),
                acceptListener = { presenter.deleteAccount(account.id) }
            )
        }
    }

    override fun onWebDavLogin(account: String, provider: WebdavProvider) {
        WebDavLoginActivity.show(requireActivity(), provider, account)
    }

    override fun onAccountLogin(portal: String?, login: String?) {
        if (login == null) {
            showFragment(EnterprisePortalFragment.newInstance(portal), null , false)
        } else {
            SignInActivity.showPortalSignIn(this, portal, login)

        }
    }

    override fun onGoogleDriveLogin() {
        showFragment(
            StorageLoginFragment.newInstance(GoogleDriveUtils.storage),
            StorageLoginFragment.TAG,
            false
        )
    }

    override fun onDropboxLogin() {
        showFragment(
            StorageLoginFragment.newInstance(DropboxUtils.storage),
            StorageLoginFragment.TAG,
            false
        )
    }

    override fun onOneDriveLogin() {
        showFragment(
            StorageLoginFragment.newInstance(OneDriveUtils.storage),
            StorageLoginFragment.TAG,
            false
        )
    }

    override fun onError(message: String?) {
        message?.let(::showSnackBar)
    }

    private fun setMenuState(isSelect: Boolean) {
        if (isSelect) {
            selectAllItem?.isVisible = false
            deselectAllItem?.isVisible = false
            deleteItem?.isVisible = false
        } else {
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
            }.show(view)
        } else {
            AccountContextDialog.newInstance().show(parentFragmentManager, AccountContextDialog.TAG)
        }
    }

    private fun setEmptyState() {
        adapter?.setItems(emptyList<CloudAccount>().toMutableList(), "")
    }

    private fun initRecyclerView(savedInstanceState: Bundle?) {
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

    private fun setTabletToolbar() {
        accountDialogFragment?.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.selectAll -> selectedTracker?.setItemsSelected(adapter?.getIds().orEmpty(), true)
                R.id.deselect -> selectedTracker?.setItemsSelected(adapter?.getIds().orEmpty(), false)
                R.id.deleteSelected -> presenter.deleteSelected(selectedTracker?.selection?.toList())
            }
            return@setOnMenuItemClickListener true
        }
        accountDialogFragment?.getMenu()?.apply {
            selectAllItem = findItem(R.id.selectAll)
            deselectAllItem = findItem(R.id.deselect)
            deleteItem = findItem(R.id.deleteSelected)
            selectedTracker?.let {
                setMenuState(!it.hasSelection())
            }
        }
    }

    private fun onSelectionMode(count: String) {
        setMenuState(false)
        if (isTablet) {
            accountDialogFragment?.setToolbarTitle(count)
            accountDialogFragment?.setToolbarNavigationIcon(false)
        } else {
            setActionBarTitle(count)
        }
    }

    fun onDefaultMode() {
        setMenuState(true)
        if (isTablet) {
            accountDialogFragment?.setToolbarTitle(getString(R.string.cloud_accounts_title))
            accountDialogFragment?.setToolbarNavigationIcon(true)
            selectedTracker?.clearSelection()
        } else {
            setActionBarTitle(getString(R.string.cloud_accounts_title))
        }
    }
}
