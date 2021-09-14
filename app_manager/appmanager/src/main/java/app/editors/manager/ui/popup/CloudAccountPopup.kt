package app.editors.manager.ui.popup

import android.content.Context
import android.view.View
import androidx.core.content.ContextCompat
import app.documents.core.account.CloudAccount
import app.editors.manager.R
import app.editors.manager.databinding.CloudAccountPopupLayoutBinding
import app.editors.manager.managers.utils.GlideUtils
import app.editors.manager.managers.utils.ManagerUiUtils
import app.editors.manager.ui.dialogs.AccountContextDialog.OnAccountContextClickListener
import com.bumptech.glide.Glide
import lib.toolkit.base.managers.utils.AccountUtils
import lib.toolkit.base.managers.utils.UiUtils.setImageTint
import lib.toolkit.base.ui.popup.BasePopup

class CloudAccountPopup(context: Context) : BasePopup(context, R.layout.cloud_account_popup_layout) {

    private var viewBinding: CloudAccountPopupLayoutBinding? = null

    private var clickListener: OnAccountContextClickListener? = null
    private var account: CloudAccount? = null

    override fun bind(view: View) {
        viewBinding = CloudAccountPopupLayoutBinding.bind(popupView)
    }

    override fun hide() {
        super.hide()
        viewBinding = null
        clickListener = null
    }

    fun setListener(listener: OnAccountContextClickListener?) {
        clickListener = listener
    }

    val isVisible: Boolean
        get() = popupWindow.isShowing

    fun setAccount(account: CloudAccount?) {
        this.account = account
        initHeader()
        initSignInItem()
        initProfileItem()
        initLogoutItem()
        initRemoveItem()
        setState()
    }

    private fun initHeader() {
        viewBinding?.headerItem?.imageCheck?.visibility = View.GONE
        viewBinding?.headerItem?.accountItemContext?.visibility = View.GONE
        account?.let {
            viewBinding?.headerItem?.let { header ->
                header.accountItemName.text = it.name
                header.accountItemEmail.text = it.login
                header.accountItemPortal.text = it.portal
                if (it.isWebDav) {
                    header.accountItemName.visibility = View.GONE
                    ManagerUiUtils.setWebDavImage(it.webDavProvider, header.selectableLayout.viewIconSelectableImage)
                } else {
                    loadAvatar()
                }
            }
        }
    }

    private fun initSignInItem() {
        viewBinding?.signInItem?.itemImage?.setImageDrawable(
            ContextCompat.getDrawable(
                context,
                R.drawable.ic_list_item_share_user_icon
            )
        )
        viewBinding?.signInItem?.itemText?.text = context.getString(R.string.dialogs_sign_in_portal_header_text)
        viewBinding?.signInItem?.root?.setOnClickListener {
            clickListener?.onSignInClick()
            hide()
        }
    }

    private fun initProfileItem() {
        viewBinding?.profileItem?.itemImage?.setImageDrawable(
            ContextCompat.getDrawable(context, R.drawable.ic_list_item_share_user_icon)
        )
        viewBinding?.profileItem?.itemText?.text = context.getString(R.string.fragment_profile_title)
        viewBinding?.profileItem?.root?.setOnClickListener {
            clickListener?.onProfileClick(account)
            hide()
        }
    }

    private fun initLogoutItem() {
        viewBinding?.logoutItem?.itemImage?.setImageDrawable(
            ContextCompat.getDrawable(context, R.drawable.ic_account_logout)
        )
        viewBinding?.logoutItem?.itemText?.text = context.getString(R.string.navigation_drawer_menu_logout)
        viewBinding?.logoutItem?.root?.setOnClickListener {
            clickListener?.onLogOutClick()
            hide()
        }
    }

    private fun initRemoveItem() {
        viewBinding?.removeItem?.itemImage?.let {
            it.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_trash))
            setImageTint(it, R.color.colorLightRed)

        }
        viewBinding?.removeItem?.itemText?.let {
            it.text = context.getString(R.string.dialog_remove_account_title)
            it.setTextColor(ContextCompat.getColor(context, R.color.colorLightRed))
        }
        viewBinding?.removeItem?.root?.setOnClickListener {
            clickListener?.onRemoveClick(account)
            hide()
        }
    }

    private fun setState() {
        val password =
            AccountUtils.getPassword(context, account?.getAccountName() ?: "")
        val token =
            AccountUtils.getToken(context, account?.getAccountName() ?: "")
        if (account?.isWebDav == true) {
            if (account?.isOnline == true || password != null && password.isNotEmpty()) {
                viewBinding?.signInItem?.root?.visibility = View.GONE
            }
            if (password == null || password.isEmpty()) {
                viewBinding?.logoutItem?.root?.visibility = View.GONE
            }
            viewBinding?.profileItem?.root?.visibility = View.GONE
        } else {
            if (account?.isOnline == true || token != null && token.isNotEmpty()) {
                viewBinding?.signInItem?.root?.visibility = View.GONE
            }
            if (token == null || token.isEmpty()) {
                viewBinding?.logoutItem?.root?.visibility = View.GONE
                viewBinding?.profileItem?.root?.visibility = View.GONE
            }
        }
    }

    private fun loadAvatar() {
        val url: String = if (account?.avatarUrl?.contains("static") == true) {
            account?.avatarUrl ?: ""
        } else {
            account?.scheme + account?.portal + account?.avatarUrl
        }
        viewBinding?.headerItem?.selectableLayout?.viewIconSelectableImage?.let {
            Glide.with(it)
                .load(
                    GlideUtils.getCorrectLoad(
                        url,
                        AccountUtils.getToken(context, account?.getAccountName() ?: "") ?: ""
                    )
                )
                .apply(GlideUtils.avatarOptions)
                .into(it)
        }
    }
}