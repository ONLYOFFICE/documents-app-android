package app.editors.manager.mvp.models.ui

import android.graphics.drawable.Drawable
import app.editors.manager.R
import app.editors.manager.mvp.models.base.ItemProperties
import kotlinx.serialization.Transient
import lib.toolkit.base.managers.utils.StringUtils

data class UserUi(
    val id: String,
    val department: String,
    val displayName: String,
    val avatarUrl: String,
    var avatar: Drawable? = null,
    val status: Int
): ItemProperties(), ShareViewType, Comparable<UserUi>{

    @Transient
    val getDisplayNameHtml = StringUtils.getHtmlString(displayName)

    override val viewType: Int
        get() = R.layout.list_share_add_item

    override val itemId: String
        get() = id

    override val itemName: String
        get() = displayName

    override fun compareTo(other: UserUi): Int = id.compareTo(other.id)
}