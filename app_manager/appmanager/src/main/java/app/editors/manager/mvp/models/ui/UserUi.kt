package app.editors.manager.mvp.models.ui

import android.graphics.drawable.Drawable
import app.editors.manager.R
import app.editors.manager.mvp.models.base.ItemProperties
import kotlinx.serialization.Transient
import lib.toolkit.base.managers.utils.StringUtils
import lib.toolkit.base.ui.adapters.holder.ViewType

data class UserUi(
    val id: String,
    val department: String,
    val displayName: String,
    val avatar: Drawable
): ItemProperties(), ViewType, Comparable<UserUi>{


    @Transient
    val getDisplayNameHtml = StringUtils.getHtmlString(displayName)

    override val viewType: Int
        get() = R.layout.list_share_add_item

    override fun getItemId(): String {
        return id
    }

    override fun getItemName(): String {
        return displayName
    }


    override fun compareTo(other: UserUi): Int = id.compareTo(other.id)

}