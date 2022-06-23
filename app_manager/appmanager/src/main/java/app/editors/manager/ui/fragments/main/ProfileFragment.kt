package app.editors.manager.ui.fragments.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
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
import lib.toolkit.base.managers.utils.StringUtils.getEncodedString
import lib.toolkit.base.managers.utils.UiUtils
import lib.toolkit.base.ui.dialogs.common.CommonDialog.Dialogs
import moxy.presenter.InjectPresenter

class ProfileFragment : BaseAppFragment(), ProfileView {

    companion object {
        val TAG: String = ProfileFragment::class.java.simpleName

        const val KEY_ACCOUNT = "KEY_ACCOUNT"
        private const val TAG_LOGOUT = "TAG_LOGOUT"

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

    private val accountDialogFragment: ICloudAccountDialogFragment?
        get() = requireActivity().supportFragmentManager
            .findFragmentByTag(CloudAccountDialogFragment.TAG) as? ICloudAccountDialogFragment

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
        typeBinder?.setTitle(getString(R.string.profile_type_account))?.setImage(R.drawable.ic_contact_calendar)
        initToolbar()
    }

    private fun initToolbar() {
        if (isTablet) {
            accountDialogFragment?.setToolbarTitle(getString(R.string.fragment_profile_title))
            accountDialogFragment?.setToolbarNavigationIcon(isClose = false)
        } else {
            setActionBarTitle(getString(R.string.fragment_profile_title))
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
        }
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
    }

    override fun onAcceptClick(dialogs: Dialogs?, value: String?, tag: String?) {
        super.onAcceptClick(dialogs, value, tag)
        if (tag != null) {
            when (tag) {
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
            viewBinding?.logoutItem?.let { item ->
                UiUtils.setImageTint(item.itemImage, lib.toolkit.base.R.color.colorLightRed)
                item.root.visibility = View.VISIBLE
                item.itemImage.setImageDrawable(
                    ContextCompat.getDrawable(
                        requireContext(),
                        R.drawable.ic_account_logout
                    )
                )
                item.itemText.text = getString(R.string.navigation_drawer_menu_logout)
                item.itemText
                    .setTextColor(item.root.context.getColor(lib.toolkit.base.R.color.colorLightRed))
                item.root.setOnClickListener {
                    showQuestionDialog(
                        getString(R.string.dialog_logout_account_title),
                        getString(R.string.dialog_logout_account_description),
                        getString(R.string.navigation_drawer_menu_logout),
                        getString(R.string.dialogs_common_cancel_button), TAG_LOGOUT
                    )
                }
            }
        } else {
            viewBinding?.logoutItem?.let { item ->
                UiUtils.setImageTint(item.itemImage, lib.toolkit.base.R.color.colorPrimary)
                item.root.visibility = View.VISIBLE
                item.itemImage.setImageDrawable(
                    ContextCompat.getDrawable(
                        requireContext(),
                        R.drawable.ic_account_login
                    )
                )
                item.itemText.text = getString(R.string.navigation_drawer_menu_sign_in)
                item.itemText
                    .setTextColor(item.root.context.getColor(lib.toolkit.base.R.color.colorPrimary))
                item.root.setOnClickListener {
                    parentFragmentManager.setFragmentResult(
                        CloudAccountFragment.REQUEST_PROFILE,
                        bundleOf(CloudAccountFragment.RESULT_SIGN_IN to null)
                    )
                    parentFragmentManager.popBackStack()
                }
            }
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
        parentFragmentManager.setFragmentResult(
            CloudAccountFragment.REQUEST_PROFILE,
            bundleOf(CloudAccountFragment.RESULT_LOG_OUT to null)
        )
        parentFragmentManager.popBackStack()
    }

    override fun onError(message: String?) {
        message?.let { showSnackBar(it) }
    }

}