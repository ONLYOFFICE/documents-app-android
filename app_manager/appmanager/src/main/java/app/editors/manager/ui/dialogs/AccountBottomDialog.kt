package app.editors.manager.ui.dialogs

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import app.documents.core.account.CloudAccount
import app.documents.core.webdav.WebDavApi
import app.editors.manager.R
import app.editors.manager.mvp.presenters.login.AccountsPresenter
import app.editors.manager.mvp.views.login.AccountsView
import app.editors.manager.ui.activities.login.PortalsActivity
import app.editors.manager.ui.activities.login.SignInActivity
import app.editors.manager.ui.activities.login.WebDavLoginActivity
import app.editors.manager.ui.activities.main.MainActivity
import app.editors.manager.ui.adapters.BottomAccountAdapter
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import lib.toolkit.base.ui.adapters.BaseAdapter
import lib.toolkit.base.ui.dialogs.base.BaseBottomDialog
import moxy.presenter.InjectPresenter

class AccountBottomDialog : BaseBottomDialog(), BaseAdapter.OnItemClickListener, AccountsView {

    companion object {
        val TAG: String = AccountBottomDialog::class.java.simpleName

        fun newInstance(): AccountBottomDialog {
            return AccountBottomDialog()
        }
    }

    @InjectPresenter
    lateinit var presenter: AccountsPresenter

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: BottomAccountAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_FRAME, R.style.ContextMenuDialog)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        adapter.setOnAddAccountClick(null)
    }

    @SuppressLint("InflateParams")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.bottom_fragment_accounts, null).apply {
            recyclerView = findViewById(R.id.accountsRecyclerView)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
    }

    private fun init() {
        initPortalsAccounts()
    }

    private fun initPortalsAccounts() {
        adapter = BottomAccountAdapter()
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter
        recyclerView.isNestedScrollingEnabled = true
        adapter.setOnItemClickListener(this)
        adapter.setOnAddAccountClick { PortalsActivity.showPortals(activity) }
        presenter.accounts
    }

    override fun onItemClick(view: View, position: Int) {
        presenter.setAccountClicked(adapter.getItem(position), position)
        presenter.loginAccount()
    }

    override fun onAccountLogin() {
        hideDialog()
        if (context != null && activity != null) {
            val intent = Intent(context, MainActivity::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && activity!!.isInMultiWindowMode) {
                intent.flags =
                    Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_LAUNCH_ADJACENT
            }
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            requireActivity().startActivity(intent)
        }
    }

    override fun onUsersAccounts(accounts: MutableList<CloudAccount>) {
        adapter.setItems(accounts)
    }

    override fun onAccountDelete(position: Int) {

    }

    override fun onSignIn(portal: String, login: String) {
        hideDialog()
        SignInActivity.showPortalSignIn(this, portal, login)
    }

    override fun showWaitingDialog() {
        showWaitingDialog(
            getString(R.string.dialogs_wait_title),
            getString(R.string.dialogs_common_cancel_button), TAG
        )
    }

    override fun onWebDavLogin(account: CloudAccount) {
        WebDavLoginActivity.show(requireActivity(), WebDavApi.Providers.valueOf(account.webDavProvider ?: ""), Json.encodeToString(account))
    }

    override fun onError(message: String?) {
        hideDialog()
        if (message != null) {
            Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
        }
    }
}