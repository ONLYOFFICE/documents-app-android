package app.editors.manager.ui.adapters.holders

import android.view.View
import android.widget.FrameLayout
import androidx.appcompat.widget.AppCompatImageButton
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import app.documents.core.account.CloudAccount
import app.editors.manager.R
import app.editors.manager.managers.utils.GlideUtils
import app.editors.manager.managers.utils.ManagerUiUtils
import com.bumptech.glide.Glide

class CloudAccountsViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    private val mAccountLayout: ConstraintLayout = view.findViewById(R.id.accountItemLayout)
    private val mViewIconSelectableImage: AppCompatImageView = view.findViewById(R.id.view_icon_selectable_image)
    private val mViewCheckImage: AppCompatImageView = view.findViewById(R.id.imageCheck)
    private val mViewIconSelectableMask: FrameLayout = view.findViewById(R.id.view_icon_selectable_mask)
    private val mViewIconSelectableLayout: FrameLayout = view.findViewById(R.id.selectableLayout)
    private val mAccountName: AppCompatTextView = view.findViewById(R.id.accountItemName)
    private val mAccountPortal: AppCompatTextView = view.findViewById(R.id.accountItemPortal)
    private val mAccountContext: AppCompatImageButton = view.findViewById(R.id.accountItemContext)

    fun bind(
        account: CloudAccount,
        isSelection: Boolean,
        accountClick: ((account: CloudAccount) -> Unit)? = null,
        accountLongClick: ((account: CloudAccount) -> Unit)? = null,
        accountContextClick: ((account: CloudAccount, position: Int, view: View) -> Unit)? = null
    ) {
        mAccountName.text = account.name
        mAccountPortal.text = account.portal
        mAccountContext.visibility = View.VISIBLE
        if (!isSelection) {
            if (account.isOnline) {
                mViewCheckImage.visibility = View.VISIBLE
            } else {
                mViewCheckImage.visibility = View.GONE
            }
        } else {
            mViewCheckImage.visibility = View.GONE
        }
        if (account.isWebDav) {
            mAccountName.visibility = View.GONE
            ManagerUiUtils.setWebDavImage(account.webDavProvider, mViewIconSelectableImage)
        } else {
            mAccountName.visibility = View.VISIBLE
            account.token?.let { token ->
                val url: String = if (account.avatarUrl?.contains("static") == true) {
                    account.avatarUrl ?: ""
                } else {
                    account.scheme + account.portal + account.avatarUrl
                }
                Glide.with(mViewIconSelectableImage)
                    .load(GlideUtils.getCorrectLoad(url, token))
                    .apply(GlideUtils.avatarOptions)
                    .into(mViewIconSelectableImage)
            }

        }
        mViewIconSelectableLayout.background = null
        mViewIconSelectableMask.background = null
        if (isSelection) {
            mAccountContext.visibility = View.GONE
//            if (account.isSelection) {
//                mViewIconSelectableMask.setBackgroundResource(R.drawable.drawable_list_image_select_mask)
//            } else {
//                mViewIconSelectableMask.setBackgroundResource(R.drawable.drawable_list_image_select_background)
//            }
        }
        setListener(account, accountClick, accountLongClick, accountContextClick)
    }

    private fun setListener(
        account: CloudAccount,
        accountClick: ((account: CloudAccount) -> Unit)? = null,
        accountLongClick: ((account: CloudAccount) -> Unit)? = null,
        accountContextClick: ((account: CloudAccount, position: Int, view: View) -> Unit)? = null
    ) {
        mAccountLayout.setOnClickListener {
            accountClick?.invoke(account)
        }
        mAccountLayout.setOnLongClickListener {
            accountLongClick?.let { listener ->
                listener.invoke(account)
                return@setOnLongClickListener true
            } ?: run {
                return@setOnLongClickListener false
            }

        }
        mAccountContext.setOnClickListener { view: View ->
            accountContextClick?.invoke(account, absoluteAdapterPosition, view)
        }
    }

}