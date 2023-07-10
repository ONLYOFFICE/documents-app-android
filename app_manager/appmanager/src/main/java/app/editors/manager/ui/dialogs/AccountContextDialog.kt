package app.editors.manager.ui.dialogs

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import app.documents.core.storage.account.CloudAccount
import app.editors.manager.R
import app.editors.manager.databinding.AccountContextLayoutBinding
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
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
        setStyle(STYLE_NO_FRAME, lib.toolkit.base.R.style.Theme_Common_BottomSheetDialog)
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
        initListener()
        initProfileItem()
        initRemoveItem()
    }

    private fun initListener() {
        requireFragmentManager().fragments.find { it is OnAccountContextClickListener }?.let {
            mClickListener = (it as OnAccountContextClickListener)
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

    private fun initRemoveItem() {
        viewBinding?.removeItem?.let {
            it.itemImage.setImageDrawable(ContextCompat
                .getDrawable(requireContext(), R.drawable.ic_trash))
            setImageTint(it.itemImage, lib.toolkit.base.R.color.colorError)
            it.itemText.text = getString(R.string.dialog_remove_account_title)
            it.itemText.setTextColor(ContextCompat.getColor(requireContext(), lib.toolkit.base.R.color.colorError))
            it.itemLayout.setOnClickListener {
                mClickListener?.onRemoveClick(account)
                dismiss()
            }

        }
    }

}