package app.editors.manager.ui.adapters.holders

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.Guideline
import app.editors.manager.R
import app.editors.manager.databinding.ListShareAddItemBinding
import app.editors.manager.managers.utils.GlideUtils.setAvatar
import app.editors.manager.managers.utils.UiUtils.setMargins
import app.editors.manager.mvp.models.ui.GroupUi
import app.editors.manager.mvp.models.ui.UserUi
import lib.toolkit.base.managers.tools.ResourcesProvider
import lib.toolkit.base.ui.adapters.BaseAdapter
import lib.toolkit.base.ui.adapters.holder.ViewType
import java.util.*

class ShareAddItemViewHolder(
    view: View,
    private val listener: BaseAdapter.OnItemClickListener?)
    : ShareViewHolder(view) {

    private val itemBinding = ListShareAddItemBinding.bind(view)
    private var shareLayout: ConstraintLayout = itemBinding.shareAddItemLayout
    private var alphaText: TextView = itemBinding.shareAddItemAlphaText
    private var guideline: Guideline = itemBinding.guideline1
    private var avatarImage: ImageView = itemBinding.shareAddItemAvatarImage
    private var mainTitle: TextView = itemBinding.shareAddItemMainTitle
    private var infoTitle: TextView = itemBinding.shareAddItemInfoTitle
    private val resourcesProvider = ResourcesProvider(itemBinding.root.context)
    private val guideLine = itemBinding.root.context.resources.getDimension(R.dimen.share_group_guideline).toInt()
    private val leftMargin = itemBinding.root.context.resources.getDimension(R.dimen.screen_margin_large).toInt()

    override fun bind(item: ViewType, mode: BaseAdapter.Mode, previousItem: ViewType?) {
        listener?.let { listener ->
            shareLayout.setOnClickListener { view ->
                listener.onItemClick(view, absoluteAdapterPosition)
            }
        }

        when (item) {
            is UserUi -> {
                if (mode.ordinal == BaseAdapter.Mode.USERS.ordinal) {
                    guideline.layoutParams =
                        (guideline.layoutParams as ConstraintLayout.LayoutParams).apply {
                            guideBegin = guideLine
                        }
                    getLetter(item, previousItem as UserUi?)
                    avatarImage.setMargins(0, 0, 0, 0)
                }
                else if (mode.ordinal == BaseAdapter.Mode.COMMON.ordinal){
                    removeAlpha()
                }
                mainTitle.text = item.getDisplayNameHtml
                avatarImage.setAvatar(if(item.isSelected) resourcesProvider
                    .getDrawable(R.drawable.drawable_list_image_select_mask)
                else { item.avatar })
                setInfo(item)
            }
            is GroupUi -> {
                avatarImage.setAvatar(if(item.isSelected) resourcesProvider
                    .getDrawable(R.drawable.drawable_list_image_select_mask)
                else { resourcesProvider
                    .getDrawable(R.drawable.drawable_list_share_image_item_group_placeholder) })
                removeAlpha()
                setAvatarMargins(item)
            }
            else -> {
                if (mode.ordinal != BaseAdapter.Mode.USERS.ordinal)
                    removeAlpha()
            }
        }
    }

    private fun getLetter(user: UserUi, userBefore: UserUi?) {
        userBefore?.let { previous ->
            if (user.getDisplayNameHtml[0] != previous.getDisplayNameHtml[0]) {
                alphaText.visibility = View.VISIBLE
                alphaText.text = user.getDisplayNameHtml[0].toString()
                    .uppercase(Locale.getDefault())
            } else {
                alphaText.visibility = View.GONE
            }
        } ?: run {
            alphaText.visibility = View.VISIBLE
            alphaText.text = user.getDisplayNameHtml[0].toString()
                .uppercase(Locale.getDefault())
        }
    }

    private fun setAvatarMargins(item: GroupUi) {
        avatarImage.setMargins(leftMargin, 0, 0, 0)
        mainTitle.text = item.name
        infoTitle.visibility = View.GONE
    }

    private fun removeAlpha() {
        alphaText.visibility = View.GONE
        guideline.layoutParams =
            (guideline.layoutParams as
                    ConstraintLayout.LayoutParams).apply {
                guideBegin = 0
            }
    }

    private fun setInfo(item: UserUi) {
        val info = item.department.trim { it <= ' ' }
        if (info.isNotEmpty()) {
            infoTitle.visibility = View.VISIBLE
            infoTitle.text = info
        } else {
            infoTitle.visibility = View.GONE
        }
    }
}
