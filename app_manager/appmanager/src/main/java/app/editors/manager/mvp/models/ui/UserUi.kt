package app.editors.manager.mvp.models.ui

import app.editors.manager.R
import app.editors.manager.mvp.models.base.ItemProperties
import kotlinx.serialization.Transient
import lib.toolkit.base.managers.utils.StringUtils

data class UserUi(
    val id: String,
    val department: String,
    val displayName: String,
    val avatarMedium: String
): ItemProperties(), ViewType, Comparable<UserUi>{


    @Transient
    val getDisplayNameHtml = StringUtils.getHtmlString(displayName)

    override val type: Int
        get() = R.layout.list_share_add_item


    override fun compareTo(other: UserUi): Int = id.compareTo(other.id)

}