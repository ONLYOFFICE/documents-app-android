package app.editors.manager.ui.fragments.main

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import app.documents.core.account.CloudAccount
import app.editors.manager.R
import app.editors.manager.databinding.FragmentProfileLayoutBinding
import app.editors.manager.mvp.models.user.Thirdparty
import app.editors.manager.mvp.presenters.main.ProfilePresenter
import app.editors.manager.mvp.presenters.main.ProfileState
import app.editors.manager.mvp.views.main.ProfileView
import app.editors.manager.ui.adapters.ThirdpartyAdapter
import app.editors.manager.ui.binders.ProfileItemBinder
import app.editors.manager.ui.fragments.base.BaseAppFragment
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import lib.toolkit.base.managers.utils.StringUtils.getEncodedString
import lib.toolkit.base.managers.utils.UiUtils
import lib.toolkit.base.ui.dialogs.common.CommonDialog.Dialogs
import moxy.presenter.InjectPresenter

class ProfileFragment : BaseAppFragment(), ProfileView {

    companion object {
        val TAG: String = ProfileFragment::class.java.simpleName

        const val KEY_ACCOUNT = "KEY_ACCOUNT"
        private const val TAG_LOGOUT = "TAG_LOGOUT"
        private const val TAG_REMOVE = "TAG_REMOVE"

        fun newInstance(account: String?): ProfileFragment {
            return ProfileFragment().apply {
                arguments = Bundle().apply {
                    putString(KEY_ACCOUNT, account)
                }
            }
        }
    }

    @InjectPresenter
    lateinit var presenter: ProfilePresenter

    private var usernameBinder: ProfileItemBinder? = null
    private var emailBinder: ProfileItemBinder? = null
    private var portalBinder: ProfileItemBinder? = null
    private var typeBinder: ProfileItemBinder? = null

    private var adapter: ThirdpartyAdapter? = null

    private var viewBinding: FragmentProfileLayoutBinding? = null

    override fun onDestroyView() {
        super.onDestroyView()
        viewBinding = null
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        viewBinding = FragmentProfileLayoutBinding.inflate(inflater, container, false).apply {
            usernameBinder = ProfileItemBinder(this.usernameItem.root)
            emailBinder = ProfileItemBinder(this.emailItem.root)
            portalBinder = ProfileItemBinder(this.portalItem.root)
            typeBinder = ProfileItemBinder(this.userTypeItem.root)
        }
        return viewBinding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        arguments?.getString(KEY_ACCOUNT)?.let {
            presenter.setAccount(it)
        }
        typeBinder?.setTitle(getString(R.string.profile_type_account))
            ?.setImage(R.drawable.ic_contact_calendar)
    }

    override fun onRender(state: ProfileState) {
        when (state) {
            is ProfileState.CloudState -> {
                onCloudState(state.account)
                onOnlineState(state.account)
            }
            is ProfileState.WebDavState -> {
                onWebDavState(state.account)
                onOnlineState(state.account)
            }
            is ProfileState.ProvidersState -> {
                if (state.providers.isEmpty()) {
                    onEmptyThirdparty()
                } else {
                    onSetServices(state.providers)
                }
            }
        }
        initRemoveItem(state.account)
    }

    override fun onAcceptClick(dialogs: Dialogs?, value: String?, tag: String?) {
        super.onAcceptClick(dialogs, value, tag)
        if (tag != null) {
            when (tag) {
                TAG_REMOVE -> {
                    presenter.removeAccount()
                }
                TAG_LOGOUT -> {
                    presenter.logout()
                }
            }
        }
        hideDialog()
    }


    private fun onWebDavState(account: CloudAccount) {
        viewBinding?.emailItem?.root?.visibility = View.VISIBLE
        viewBinding?.portalItem?.root?.visibility = View.VISIBLE
        emailBinder?.setTitle(getString(R.string.login_enterprise_email_hint))?.setImage(R.drawable.ic_email)?.text =
            account.login
        portalBinder?.setTitle(getString(R.string.profile_portal_address))?.setImage(R.drawable.ic_cloud)?.text =
            account.scheme + getEncodedString(account.portal)
    }

    private fun onCloudState(account: CloudAccount) {
        viewBinding?.usernameItem?.root?.visibility = View.VISIBLE
        viewBinding?.userTypeItem?.root?.visibility = View.VISIBLE
        viewBinding?.emailItem?.root?.visibility = View.VISIBLE
        viewBinding?.portalItem?.root?.visibility = View.VISIBLE
        emailBinder?.setTitle(getString(R.string.login_enterprise_email_hint))?.setImage(R.drawable.ic_email)?.text =
            account.login
        portalBinder?.setTitle(getString(R.string.profile_portal_address))?.setImage(R.drawable.ic_cloud)?.text =
            account.scheme + getEncodedString(account.portal)
        usernameBinder?.setTitle(getString(R.string.profile_username_title))
            ?.setText(account.name)
            ?.setImage(R.drawable.ic_list_item_share_user_icon)
        setType(account)
    }

    private fun onOnlineState(account: CloudAccount) {
        if (account.isOnline) {
            viewBinding?.logoutItem?.root?.visibility = View.VISIBLE
            viewBinding?.logoutItem?.itemImage?.setImageDrawable(
                ContextCompat.getDrawable(
                    requireContext(),
                    R.drawable.ic_account_logout
                )
            )
            viewBinding?.logoutItem?.itemText?.text = getString(R.string.navigation_drawer_menu_logout)
            viewBinding?.logoutItem?.root?.setOnClickListener {
                showQuestionDialog(
                    getString(R.string.dialog_logout_account_title),
                    getString(R.string.dialog_logout_account_description),
                    getString(R.string.navigation_drawer_menu_logout),
                    getString(R.string.dialogs_common_cancel_button), TAG_LOGOUT
                )
            }
            UiUtils.setImageTint(viewBinding?.logoutItem?.itemImage ?: throw Error("Error inflate"), R.color.colorLight)
        }
    }

    private fun onEmptyThirdparty() {
        viewBinding?.recyclerView?.visibility = View.GONE
    }

    private fun onSetServices(list: List<Thirdparty>) {
        viewBinding?.recyclerView?.visibility = View.VISIBLE
        adapter = ThirdpartyAdapter()
        viewBinding?.recyclerView?.layoutManager = LinearLayoutManager(requireContext())
        viewBinding?.recyclerView?.adapter = adapter
        adapter?.setItems(list)
    }

    private fun initRemoveItem(account: CloudAccount) {
        viewBinding?.removeItem?.itemImage?.setImageDrawable(
            ContextCompat.getDrawable(
                requireContext(),
                R.drawable.ic_trash
            )
        )
        viewBinding?.removeItem?.itemText?.text = getString(R.string.dialog_remove_account_title)
        viewBinding?.removeItem?.itemText?.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorLightRed))
        UiUtils.setImageTint(viewBinding?.removeItem?.itemImage ?: throw Error("Error inflate"), R.color.colorLightRed)

        viewBinding?.removeItem?.root?.setOnClickListener {
            showQuestionDialog(
                getString(R.string.dialog_remove_account_title),
                getString(R.string.dialog_remove_account_description, account.login, account.portal),
                getString(R.string.dialogs_question_accept_remove),
                getString(R.string.dialogs_common_cancel_button),
                TAG_REMOVE
            )
        }
    }

    private fun setType(account: CloudAccount) {
        val type = when {
            account.isVisitor -> {
                getString(R.string.profile_type_visitor)
            }
            account.isAdmin -> {
                getString(R.string.profile_type_admin)
            }
            else -> {
                getString(R.string.profile_type_user)
            }
        }
        typeBinder?.text = type
    }

    override fun onClose(isLogout: Boolean, account: CloudAccount?) {
        if(isLogout) {
            requireActivity().intent.putExtra(KEY_ACCOUNT, Json.encodeToString(account))
            requireActivity().setResult(Activity.RESULT_OK, requireActivity().intent)
        } else {
            requireActivity().setResult(Activity.RESULT_CANCELED)
        }
        requireActivity().finish()
    }

    override fun onError(message: String?) {
        message?.let { showSnackBar(it) }
    }

}