package app.editors.manager.ui.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat.getColor
import app.editors.manager.R
import app.editors.manager.databinding.AccountContextLayoutBinding
import lib.toolkit.base.managers.utils.UiUtils.setImageTint
import lib.toolkit.base.ui.dialogs.base.BaseBottomDialog

class AccountContextDialog : BaseBottomDialog() {

    interface OnAccountContextClickListener {
        fun onProfileClick()
        fun onRemoveClick()
    }

    companion object {
        val TAG: String = AccountContextDialog::class.java.simpleName

        fun newInstance(): AccountContextDialog {
            return AccountContextDialog()
        }
    }

    private var mClickListener: OnAccountContextClickListener? = null
    private var viewBinding: AccountContextLayoutBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_FRAME, lib.toolkit.base.R.style.Theme_Common_BottomSheetDialog)
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
        viewBinding = AccountContextLayoutBinding.inflate(LayoutInflater.from(context), container, false)
        return viewBinding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initListener()
        initProfileItem()
        initRemoveItem()
    }

    private fun initListener() {
        parentFragmentManager.fragments
            .find { it is OnAccountContextClickListener }
            ?.let { mClickListener = (it as OnAccountContextClickListener) }
    }


    private fun initProfileItem() {
        viewBinding?.profileItem?.let {
            it.itemImage.setImageResource(R.drawable.ic_list_item_share_user_icon)
            it.itemText.setText(R.string.fragment_profile_title)
            it.itemLayout.setOnClickListener {
                mClickListener?.onProfileClick()
                dismiss()
            }
        }
    }

    private fun initRemoveItem() {
        viewBinding?.removeItem?.let {
            it.itemImage.setImageResource(R.drawable.ic_trash)
            setImageTint(it.itemImage, lib.toolkit.base.R.color.colorError)
            it.itemText.setText(R.string.dialog_remove_account_title)
            it.itemText.setTextColor(getColor(requireContext(), lib.toolkit.base.R.color.colorError))
            it.itemLayout.setOnClickListener {
                mClickListener?.onRemoveClick()
                dismiss()
            }
        }
    }

}