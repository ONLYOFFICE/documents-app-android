package app.editors.manager.managers.utils

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import app.documents.core.model.cloud.Access
import app.documents.core.network.common.contracts.ApiContract
import app.editors.manager.R


data class RoomInfo(
    @DrawableRes val icon: Int,
    @StringRes val title: Int,
    @StringRes val description: Int,
)

object RoomUtils {

    val roomTypes: List<Int> = listOf(
        ApiContract.RoomType.PUBLIC_ROOM,
        ApiContract.RoomType.FILL_FORMS_ROOM,
        ApiContract.RoomType.VIRTUAL_ROOM,
        ApiContract.RoomType.COLLABORATION_ROOM,
        ApiContract.RoomType.CUSTOM_ROOM
    )

    fun getRoomInfo(type: Int, isTemplate: Boolean = false): RoomInfo {
        if (isTemplate && type == 0) return getFromTemplateInfo()

        val icon = if (isTemplate) getTemplateIcon(type) else getRoomIcon(type)
        val title = when (type) {
            ApiContract.RoomType.COLLABORATION_ROOM -> R.string.rooms_add_collaboration
            ApiContract.RoomType.PUBLIC_ROOM -> R.string.rooms_add_public_room
            ApiContract.RoomType.CUSTOM_ROOM -> R.string.rooms_add_custom
            ApiContract.RoomType.FILL_FORMS_ROOM -> R.string.rooms_add_fill_forms
            ApiContract.RoomType.VIRTUAL_ROOM -> R.string.rooms_vdr
            else -> R.string.rooms_add_collaboration
        }
        val des = when (type) {
            ApiContract.RoomType.COLLABORATION_ROOM -> R.string.rooms_add_collaboration_des
            ApiContract.RoomType.PUBLIC_ROOM -> R.string.rooms_add_public_room_des
            ApiContract.RoomType.CUSTOM_ROOM -> R.string.rooms_add_custom_des
            ApiContract.RoomType.FILL_FORMS_ROOM -> R.string.rooms_add_fill_forms_des
            ApiContract.RoomType.VIRTUAL_ROOM -> R.string.rooms_vdr_desc
            else -> R.string.rooms_add_collaboration_des
        }
        return RoomInfo(icon, title, des)
    }

    private fun getRoomIcon(type: Int) = when (type) {
        ApiContract.RoomType.COLLABORATION_ROOM -> R.drawable.ic_collaboration_room
        ApiContract.RoomType.PUBLIC_ROOM -> R.drawable.ic_public_room
        ApiContract.RoomType.CUSTOM_ROOM -> R.drawable.ic_custom_room
        ApiContract.RoomType.FILL_FORMS_ROOM -> R.drawable.ic_fill_forms_room
        ApiContract.RoomType.VIRTUAL_ROOM -> R.drawable.ic_vdr_room
        else -> R.drawable.ic_collaboration_room
    }

    private fun getTemplateIcon(type: Int) = when (type) {
        ApiContract.RoomType.COLLABORATION_ROOM -> R.drawable.ic_collaboration_template
        ApiContract.RoomType.PUBLIC_ROOM -> R.drawable.ic_public_template
        ApiContract.RoomType.CUSTOM_ROOM -> R.drawable.ic_custom_template
        ApiContract.RoomType.FILL_FORMS_ROOM -> R.drawable.ic_fill_forms_template
        ApiContract.RoomType.VIRTUAL_ROOM -> R.drawable.ic_vdr_template
        else -> R.drawable.ic_collaboration_template
    }

    private fun getFromTemplateInfo(): RoomInfo {
        return RoomInfo(
            icon = R.drawable.ic_create_template,
            title = R.string.title_create_from_template,
            description = R.string.desc_create_from_template
        )
    }

    fun getLinkAccessOptions(): List<Access> = listOf(
        Access.Editor,
        Access.Review,
        Access.Comment,
        Access.Read
    )

    fun getAccessOptions(roomType: Int, isRemove: Boolean, isAdmin: Boolean = false): List<Access> {
        return buildList {
            if (isAdmin) add(Access.RoomManager)
            add(Access.ContentCreator)
            when (roomType) {
                ApiContract.RoomType.COLLABORATION_ROOM -> {
                    add(Access.Editor)
                    add(Access.Read)
                }
                ApiContract.RoomType.CUSTOM_ROOM -> {
                    add(Access.Editor)
                    add(Access.Review)
                    add(Access.Comment)
                    add(Access.Read)
                }
                ApiContract.RoomType.FILL_FORMS_ROOM -> {
                    add(Access.FormFiller)
                }
                ApiContract.RoomType.VIRTUAL_ROOM -> {
                    add(Access.Editor)
                    add(Access.Read)
                    add(Access.FormFiller)
                }
            }
            if (isRemove) add(Access.None)
        }
    }

    fun getAccessTitleOrOwner(isOwner: Boolean, access: Int, isRoom: Boolean): Int =
        if (isOwner) {
            if (isRoom) {
                R.string.share_access_room_owner
            } else {
                R.string.share_popup_access_full
            }
        } else {
            Access.get(access).toUi(!isRoom).title
        }

    fun getRoomInitials(title: String): String? {
        return try {
            val words = title.split(" ").filter { it.first().isLetterOrDigit() }
            when (words.size) {
                1 -> title[0].toString()
                2 -> "${words[0][0]}${words[1][0]}"
                else -> "${words[0][0]}${words[words.lastIndex][0]}"
            }.uppercase()
        } catch (_: RuntimeException) {
            null
        }
    }
}