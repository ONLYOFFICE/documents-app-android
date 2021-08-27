package app.editors.manager.ui.adapters.holders

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import app.editors.manager.R
import app.editors.manager.databinding.ListShareSettingsItemBinding
import app.editors.manager.managers.utils.GlideUtils.loadAvatar
import app.editors.manager.managers.utils.UiUtils.setAccessIcon
import app.editors.manager.mvp.models.ui.ShareUi
import lib.toolkit.base.ui.adapters.holder.BaseViewHolder

class ShareItemViewHolder(view: View, val listener: (view: View, position: Int) -> Unit) :
    BaseViewHolder<ShareUi>(view) {

    private val itemBinding = ListShareSettingsItemBinding.bind(view)
    private val shareImage: ImageView = itemBinding.listShareSettingsImage
    private val itemName: TextView = itemBinding.listShareSettingsName
    private val itemInfo: TextView = itemBinding.listShareSettingsInfo
    private val contextLayoutButton: ConstraintLayout = itemBinding.listShareSettingsContextLayout.root
    private val contextButton: ImageView = itemBinding.listShareSettingsContextLayout.buttonPopupImage

    init {
        contextLayoutButton.setOnClickListener {
            listener(contextLayoutButton, absoluteAdapterPosition)
        }
    }

    override fun bind(item: ShareUi) {
        if (item.sharedTo.avatarSmall.isNotEmpty())
            shareImage.loadAvatar(item.sharedTo.avatarSmall)
        if (item.sharedTo.userName.isNotEmpty()) {
            itemInfo.visibility = View.VISIBLE
            itemName.text = item.sharedTo.displayNameHtml

            // Set info if not empty
            val info = item.sharedTo.department.trim { it <= ' ' }
            if (info.isNotEmpty()) {
                itemInfo.visibility = View.VISIBLE
                itemInfo.text = info
            } else {
                itemInfo.visibility = View.GONE
            }
        } else {
            itemInfo.visibility = View.GONE
            itemName.text = item.sharedTo.name
            shareImage.setImageResource(R.drawable.drawable_list_share_image_item_group_placeholder)
        }

        // Access icons
        setAccessIcon(contextButton, item.access)
    }
}