package app.editors.manager.ui.adapters.holders

import android.view.View
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import app.documents.core.network.manager.models.base.ItemProperties
import app.editors.manager.R
import app.editors.manager.databinding.ListShareAddItemBinding
import app.editors.manager.managers.utils.setAvatarFromUrl
import app.editors.manager.mvp.models.ui.GroupUi
import app.editors.manager.mvp.models.ui.UserUi
import lib.toolkit.base.ui.adapters.BaseAdapter
import lib.toolkit.base.ui.adapters.holder.BaseViewHolder
import lib.toolkit.base.ui.adapters.holder.ViewType

class ShareAddItemViewHolder(
    view: View,
    private val listener: BaseAdapter.OnItemClickListener?
) : BaseViewHolder<ViewType>(view) {

    private val binding = ListShareAddItemBinding.bind(view)

    override fun bind(item: ViewType, payloads: List<Any>) {
        setSelected(item)
    }

    private fun setSelected(item: ViewType) {
        with(binding) {
            if (item is ItemProperties && item.isSelected) {
                shareAddItemAvatar.foreground =
                    AppCompatResources.getDrawable(root.context, R.drawable.drawable_list_image_select_foreground)
            } else {
                shareAddItemAvatar.foreground = null
            }
        }
    }

    fun bind(item: ViewType, mode: BaseAdapter.Mode, previousItem: ViewType?, firstLetterVisible: Boolean) {
        with(binding) {
            when (item) {
                is UserUi -> {
                    if (mode != BaseAdapter.Mode.COMMON && firstLetterVisible) {
                        val letter = getLetter(item, previousItem as? UserUi)
                        shareAddItemLetter.isInvisible = letter == null
                        letter?.let { shareAddItemLetter.text = letter.toString() }
                    } else {
                        shareAddItemLetter.isVisible = false
                    }
                    shareAddItemInfo.isVisible = item.department.isNotEmpty()
                    shareAddItemInfo.text = item.department.trim()
                    shareAddItemTitle.text = item.getDisplayNameHtml
                    shareAddItemAvatar.setAvatarFromUrl(item.avatarUrl)
                }
                is GroupUi -> {
                    shareAddItemAvatar.setImageResource(R.drawable.drawable_list_share_image_item_group_placeholder)
                    shareAddItemTitle.text = item.name
                    shareAddItemInfo.isVisible = false
                    shareAddItemLetter.isVisible = false
                }
            }
            shareAddItemLayout.setOnClickListener { view -> listener?.onItemClick(view, absoluteAdapterPosition) }
        }
        setSelected(item)
    }

    private fun getLetter(user: UserUi, userBefore: UserUi?): Char? {
        return if (userBefore != null) {
            if (user.getDisplayNameHtml[0].equals(userBefore.getDisplayNameHtml[0], true)) return null
            user.getDisplayNameHtml[0].uppercaseChar()
        } else user.getDisplayNameHtml[0].uppercaseChar()
    }

}
