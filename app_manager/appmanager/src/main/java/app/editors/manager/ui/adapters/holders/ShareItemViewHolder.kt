package app.editors.manager.ui.adapters.holders

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import app.editors.manager.R
import app.editors.manager.databinding.ListShareSettingsItemBinding
import app.editors.manager.managers.utils.ManagerUiUtils
import app.editors.manager.mvp.models.ui.ShareUi
import com.google.android.material.button.MaterialButton
import lib.toolkit.base.ui.adapters.holder.BaseViewHolder
import lib.toolkit.base.ui.adapters.holder.ViewType

class ShareItemViewHolder(view: View, val listener: (view: View, position: Int) -> Unit) :
    BaseViewHolder<ViewType>(view) {

    private val itemBinding = ListShareSettingsItemBinding.bind(view)
    private val shareImage: ImageView = itemBinding.listShareSettingsImage
    private val itemName: TextView = itemBinding.listShareSettingsName
    private val itemInfo: TextView = itemBinding.listShareSettingsInfo
    private val contextButton: MaterialButton = itemBinding.listShareSettingsContextLayout

    init {
        contextButton.setOnClickListener {
            listener(contextButton, absoluteAdapterPosition)
        }
    }

    override fun bind(item: ViewType) {
        if (item is ShareUi) {
            if (item.sharedTo.userName.isNotEmpty()) {
                itemInfo.visibility = View.VISIBLE
                itemName.text = item.sharedTo.displayNameHtml.takeIf { it.isNotEmpty() }
                    ?: item.sharedTo.email

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
                itemName.text = item.sharedTo.name.takeIf { it.isNotEmpty() }
                    ?: item.sharedTo.email
                shareImage.setImageResource(R.drawable.drawable_list_share_image_item_group_placeholder)
            }

            // Access icons
            contextButton.isVisible = !item.isOwner
            itemBinding.listShareSettingsOwner.isVisible = item.isOwner
            if (!item.isOwner) {
                contextButton.setIconResource(ManagerUiUtils.getAccessIcon(item.access))
            }
        }
    }
}