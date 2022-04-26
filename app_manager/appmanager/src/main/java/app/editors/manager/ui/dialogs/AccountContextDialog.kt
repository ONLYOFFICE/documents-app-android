package app.editors.manager.ui.dialogs

import android.accounts.Account
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.content.ContextCompat
import app.documents.core.account.CloudAccount
import app.editors.manager.R
import app.editors.manager.databinding.AccountContextLayoutBinding
import app.editors.manager.managers.utils.GlideUtils
import app.editors.manager.managers.utils.ManagerUiUtils
import app.editors.manager.managers.utils.ManagerUiUtils.setDropboxImage
import app.editors.manager.managers.utils.ManagerUiUtils.setOneDriveImage
import app.editors.manager.managers.utils.isVisible
import com.bumptech.glide.Glide
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import lib.toolkit.base.managers.utils.AccountUtils
import lib.toolkit.base.managers.utils.UiUtils.setImageTint
import lib.toolkit.base.ui.dialogs.base.BaseBottomDialog

class AccountContextDialog : BaseBottomDialog() {

    interface OnAccountContextClickListener {
        fun onProfileClick(account: CloudAccount?)
        fun onLogOutClick()
        fun onRemoveClick(account: CloudAccount?)
        fun onSignInClick()
    }

    companion object {
        val TAG: String = AccountContextDialog::class.java.simpleName

        private const val KEY_ACCOUNT = "KEY_ACCOUNT"
        private const val KEY_TOKEN = "KEY_TOKEN"

        fun newInstance(account: String, token: String?): AccountContextDialog {
            return AccountContextDialog().apply {
                arguments = Bundle(2).apply {
                    putString(KEY_ACCOUNT, account)
                    putString(KEY_TOKEN, token)
                }
            }
        }
    }

    private var account: CloudAccount? = null
    private var mClickListener: OnAccountContextClickListener? = null
    private var viewBinding: AccountContextLayoutBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_FRAME, lib.toolkit.base.R.style.ContextMenuDialog)
        arguments?.containsKey(KEY_ACCOUNT)?.let {
            arguments?.getString(KEY_ACCOUNT)?.let { acc ->
                account = Json.decodeFromString(acc)
            }
        } ?: run {
            Log.d(TAG, "onCreate: account error")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mClickListener = null
        viewBinding = null
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewBinding = AccountContextLayoutBinding
            .inflate(LayoutInflater.from(context), container, false)
        return viewBinding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initHeader()
        initListener()
        initSignInItem()
        initProfileItem()
        initLogoutItem()
        initRemoveItem()
        setState()
    }

    private fun initHeader() {
        viewBinding?.headerItem?.let { binding ->
            binding.imageCheck.isVisible = false
            binding.accountItemContext.isVisible = false
            account?.let { acc ->
                binding.accountItemName.text = acc.name
                binding.accountItemPortal.text = acc.portal
                if (acc.isWebDav) {
                    binding.accountItemName.isVisible = false
                    ManagerUiUtils.setWebDavImage(
                        acc.webDavProvider,
                        binding.selectableLayout.viewIconSelectableImage)
                } else if(account?.isOneDrive == true) {
                    binding.selectableLayout.viewIconSelectableImage.setOneDriveImage()
                } else if(account?.isDropbox == true) {
                    binding.selectableLayout.viewIconSelectableImage.setDropboxImage(account!!)
                } else {
                    loadAvatar(binding.selectableLayout.viewIconSelectableImage)
                }
            }
        }
    }

    private fun initListener() {
        requireFragmentManager().fragments.find { it is OnAccountContextClickListener }?.let {
            mClickListener = (it as OnAccountContextClickListener)
        }
    }

    private fun initSignInItem() {
        viewBinding?.signInItem?.let {
            it.itemImage.setImageDrawable(ContextCompat
                .getDrawable(requireContext(),R.drawable.ic_list_item_share_user_icon))
            it.itemText.text = getString(R.string.dialogs_sign_in_portal_header_text)
            it.itemLayout.setOnClickListener {
                mClickListener?.onSignInClick()
                dismiss()
            }
        }
    }

    private fun initProfileItem() {
        arguments?.getString(KEY_TOKEN)?.let {
            viewBinding?.profileItem?.let {
                it.itemImage.setImageDrawable(ContextCompat
                    .getDrawable(requireContext(),R.drawable.ic_list_item_share_user_icon))
                it.itemText.text = getString(R.string.fragment_profile_title)
                it.itemLayout.setOnClickListener{
                    mClickListener?.onProfileClick(account)
                    dismiss()
                }
            }
        } ?: run {
            viewBinding?.profileItem?.root?.isVisible = false
        }
    }

    private fun initLogoutItem() {
        arguments?.getString(KEY_TOKEN)?.let {
            viewBinding?.logoutItem?.let {
                it.itemImage.setImageDrawable(ContextCompat
                    .getDrawable(requireContext(), R.drawable.ic_account_logout))
                it.itemText.text = getString(R.string.navigation_drawer_menu_logout)
                it.itemLayout.setOnClickListener {
                    mClickListener?.onLogOutClick()
                    dismiss()
                }
            }
        } ?: run {
            viewBinding?.logoutItem?.root?.isVisible = false
        }
    }

    private fun initRemoveItem() {
        viewBinding?.removeItem?.let {
            it.itemImage.setImageDrawable(ContextCompat
                .getDrawable(requireContext(), R.drawable.ic_trash))
            setImageTint(it.itemImage, lib.toolkit.base.R.color.colorLightRed)
            it.itemText.text = getString(R.string.dialog_remove_account_title)
            it.itemText.setTextColor(ContextCompat.getColor(requireContext(), lib.toolkit.base.R.color.colorLightRed))
            it.itemLayout.setOnClickListener {
                mClickListener?.onRemoveClick(account)
                dismiss()
            }

        }
    }

    private fun setState() {
        account?.let { account ->
            val password = AccountUtils.getPassword(requireContext(),
                Account(account.getAccountName(), requireContext().getString(lib.toolkit.base.R.string.account_type)))
            val token = AccountUtils.getToken(requireContext(),
                Account(account.getAccountName(), requireContext().getString(lib.toolkit.base.R.string.account_type)))
            viewBinding?.let {
                if (account.isWebDav) {
                    if (account.isOnline || password?.isNotEmpty() == true) {
                        it.signInItem.itemLayout.isVisible = false
                    }
                    if (password?.isEmpty() == true) {
                        it.logoutItem.itemLayout.isVisible = false
                    }
                    it.profileItem.itemLayout.isVisible = false
                } else {
                    if (account.isOnline || token?.isNotEmpty() == true) {
                        it.signInItem.itemLayout.isVisible = false
                    }
                    if (token?.isEmpty() == true) {
                        it.logoutItem.itemLayout.isVisible = false
                        it.profileItem.itemLayout.isVisible = false
                    }
                }
            }
        }
    }

    private fun loadAvatar(imageView: ImageView) {
        account?.let {
            val url = if (it.avatarUrl?.contains("static") == true) {
                it.avatarUrl
            } else {
                it.scheme + it.portal + it.avatarUrl
            }
            Glide.with(imageView)
                .load(GlideUtils.getCorrectLoad(url ?: "", arguments?.getString(KEY_TOKEN) ?: ""))
                .apply(GlideUtils.avatarOptions)
                .into(imageView)
        }
    }
}