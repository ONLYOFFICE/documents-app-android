package app.editors.manager.ui.popup

import android.content.Context
import android.view.View
import androidx.core.content.ContextCompat
import app.documents.core.model.cloud.CloudAccount
import app.editors.manager.R
import app.editors.manager.databinding.CloudAccountPopupLayoutBinding
import app.editors.manager.ui.dialogs.AccountContextDialog.OnAccountContextClickListener
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
        initProfileItem()
        initRemoveItem()
    }

    private fun initProfileItem() {
        viewBinding?.profileItem?.itemImage?.setImageDrawable(
            ContextCompat.getDrawable(context, R.drawable.ic_list_item_share_user_icon)
        )
        viewBinding?.profileItem?.itemText?.text = context.getString(R.string.fragment_profile_title)
        viewBinding?.profileItem?.root?.setOnClickListener {
            clickListener?.onProfileClick()
            hide()
        }
    }

    private fun initRemoveItem() {
        viewBinding?.removeItem?.itemImage?.let {
            it.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_trash))
            setImageTint(it, lib.toolkit.base.R.color.colorError)
        }
        viewBinding?.removeItem?.itemText?.let {
            it.text = context.getString(R.string.dialog_remove_account_title)
            it.setTextColor(ContextCompat.getColor(context, lib.toolkit.base.R.color.colorError))
        }
        viewBinding?.removeItem?.root?.setOnClickListener {
            clickListener?.onRemoveClick()
            hide()
        }
    }

    fun show(view: View) {
        popupWindow.showAsDropDown(
            view,
            -popupView.measuredWidth + view.measuredWidth,
            -popupView.measuredHeight + view.measuredHeight / 2
        )
    }
}