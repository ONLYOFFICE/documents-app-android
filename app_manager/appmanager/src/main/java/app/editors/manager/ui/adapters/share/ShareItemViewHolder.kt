package app.editors.manager.ui.adapters.share

import android.view.View
import android.widget.ImageView
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import app.documents.core.network.ApiContract
import app.documents.core.network.models.share.SharedTo
import app.editors.manager.R
import app.editors.manager.app.App
import app.editors.manager.managers.utils.GlideUtils
import app.editors.manager.mvp.models.ui.ShareUi
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import lib.toolkit.base.managers.utils.AccountUtils

class ShareItemViewHolder(val view: View, val listener: (view: View, position: Int) -> Unit) :
    RecyclerView.ViewHolder(view) {

    private val shareImage: AppCompatImageView = view.findViewById(R.id.list_share_settings_image)
    private val itemName: AppCompatTextView = view.findViewById(R.id.list_share_settings_name)
    private val itemInfo: AppCompatTextView = view.findViewById(R.id.list_share_settings_info)
    private val contextLayoutButton: ConstraintLayout = view.findViewById(R.id.list_share_settings_context_layout)
    private val contextButton: AppCompatImageView = view.findViewById(R.id.button_popup_image)

    init {
        contextLayoutButton.setOnClickListener {
            listener(contextLayoutButton, absoluteAdapterPosition)
        }
    }

    fun bind(share: ShareUi) {
        if (share.sharedTo.userName.isNotEmpty()) {
            itemInfo.visibility = View.VISIBLE
            itemName.text = share.sharedTo.displayNameHtml

            // Set info if not empty
            val info = share.sharedTo.department.trim { it <= ' ' }
            if (info.isNotEmpty()) {
                itemInfo.visibility = View.VISIBLE
                itemInfo.text = info
            } else {
                itemInfo.visibility = View.GONE
            }
        } else {
            itemInfo.visibility = View.GONE
            itemName.text = share.sharedTo.name
            shareImage.setImageResource(R.drawable.drawable_list_share_image_item_group_placeholder)
        }

        // Access icons
        setAccessIcon(contextButton, share.access)
        loadAvatar(share.sharedTo)
    }

    private fun loadAvatar(sharedTo: SharedTo) {
        CoroutineScope(Dispatchers.Default).launch {
            App.getApp().appComponent.accountsDao.getAccountOnline()?.let { account ->
                AccountUtils.getToken(
                    context = view.context,
                    accountName = account.getAccountName()
                )?.let {
                    val url = GlideUtils.getCorrectLoad(account.scheme + account.portal + sharedTo.avatarSmall, it)
                    withContext(Dispatchers.Main) {
                        Glide.with(view.context)
                            .load(url)
                            .apply(RequestOptions().apply {
                                skipMemoryCache(true)
                                diskCacheStrategy(DiskCacheStrategy.NONE)
                                timeout(30 * 1000)
                                circleCrop()
                                error(R.drawable.drawable_list_share_image_item_user_placeholder)
                                placeholder(R.drawable.drawable_list_share_image_item_user_placeholder)
                            })
                            .into(shareImage)
                    }
                }
            } ?: run {
                withContext(Dispatchers.Main) {
                    Glide.with(view.context)
                        .load(R.drawable.drawable_list_share_image_item_user_placeholder)
                        .into(shareImage)
                }
            }
        }

    }

    private fun setAccessIcon(imageView: ImageView, accessCode: Int) {
        when (accessCode) {
            ApiContract.ShareCode.NONE, ApiContract.ShareCode.RESTRICT -> {
                imageView.setImageResource(R.drawable.ic_access_deny)
                return
            }
            ApiContract.ShareCode.REVIEW -> imageView.setImageResource(R.drawable.ic_access_review)
            ApiContract.ShareCode.READ -> imageView.setImageResource(R.drawable.ic_access_read)
            ApiContract.ShareCode.READ_WRITE -> imageView.setImageResource(R.drawable.ic_access_full)
            ApiContract.ShareCode.COMMENT -> imageView.setImageResource(R.drawable.ic_access_comment)
            ApiContract.ShareCode.FILL_FORMS -> imageView.setImageResource(R.drawable.ic_access_fill_form)
        }
    }

}