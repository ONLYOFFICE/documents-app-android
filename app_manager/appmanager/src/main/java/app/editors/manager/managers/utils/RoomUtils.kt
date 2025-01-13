package app.editors.manager.managers.utils

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import app.documents.core.network.common.contracts.ApiContract
import app.editors.manager.R


data class RoomInfo(@DrawableRes val icon: Int, @StringRes val title: Int, @StringRes val description: Int)

object RoomUtils {

    val roomTypes: List<Int> = listOf(
        ApiContract.RoomType.PUBLIC_ROOM,
        ApiContract.RoomType.FILL_FORMS_ROOM,
        ApiContract.RoomType.VIRTUAL_ROOM,
        ApiContract.RoomType.COLLABORATION_ROOM,
        ApiContract.RoomType.CUSTOM_ROOM
    )

    fun getRoomInfo(type: Int): RoomInfo {
        val icon = when (type) {
            ApiContract.RoomType.COLLABORATION_ROOM -> R.drawable.ic_collaboration_room
            ApiContract.RoomType.PUBLIC_ROOM -> R.drawable.ic_public_room
            ApiContract.RoomType.CUSTOM_ROOM -> R.drawable.ic_custom_room
            ApiContract.RoomType.FILL_FORMS_ROOM -> R.drawable.ic_fill_forms_room
            ApiContract.RoomType.VIRTUAL_ROOM -> R.drawable.ic_vdr_room
            else -> R.drawable.ic_collaboration_room
        }
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

    fun getAccessTitle(access: Int): Int = when (access) {
        ApiContract.ShareCode.ROOM_ADMIN -> R.string.share_access_room_admin
        ApiContract.ShareCode.POWER_USER -> R.string.share_access_room_power_user
        ApiContract.ShareCode.READ_WRITE -> R.string.share_access_room_editor
        ApiContract.ShareCode.CUSTOM_FILTER -> R.string.share_popup_access_custom_filter
        ApiContract.ShareCode.EDITOR -> R.string.share_access_room_editor
        ApiContract.ShareCode.FILL_FORMS -> R.string.share_access_room_form_filler
        ApiContract.ShareCode.REVIEW -> R.string.share_access_room_reviewer
        ApiContract.ShareCode.COMMENT -> R.string.share_access_room_commentator
        ApiContract.ShareCode.RESTRICT -> R.string.share_popup_access_deny_access
        ApiContract.ShareCode.NONE -> R.string.share_popup_access_deny_remove
        else -> R.string.share_access_room_viewer
    }

    fun getAccessOptions(roomType: Int, isRemove: Boolean, isAdmin: Boolean = false): List<Int> {
        if (isAdmin) {
            return listOfNotNull(
                ApiContract.ShareCode.ROOM_ADMIN,
                ApiContract.ShareCode.NONE.takeIf { isRemove }
            )
        }

        return when (roomType) {
            ApiContract.RoomType.PUBLIC_ROOM -> {
                mutableListOf(
                    ApiContract.ShareCode.ROOM_ADMIN,
                    ApiContract.ShareCode.POWER_USER
                )
            }
            ApiContract.RoomType.COLLABORATION_ROOM -> {
                mutableListOf(
                    ApiContract.ShareCode.ROOM_ADMIN,
                    ApiContract.ShareCode.POWER_USER,
                    ApiContract.ShareCode.EDITOR,
                    ApiContract.ShareCode.READ
                )
            }
            ApiContract.RoomType.CUSTOM_ROOM -> {
                mutableListOf(
                    ApiContract.ShareCode.ROOM_ADMIN,
                    ApiContract.ShareCode.POWER_USER,
                    ApiContract.ShareCode.EDITOR,
                    ApiContract.ShareCode.FILL_FORMS,
                    ApiContract.ShareCode.REVIEW,
                    ApiContract.ShareCode.COMMENT,
                    ApiContract.ShareCode.READ
                )
            }
            ApiContract.RoomType.FILL_FORMS_ROOM -> {
                mutableListOf(
                    ApiContract.ShareCode.ROOM_ADMIN,
                    ApiContract.ShareCode.POWER_USER,
                    ApiContract.ShareCode.FILL_FORMS,
                )
            }
            else -> mutableListOf()
        }.apply { if (isRemove) add(ApiContract.ShareCode.NONE) }
    }

    fun getAccessTitleOrOwner(isOwner: Boolean, access: Int): Int =
        if (isOwner) R.string.share_access_room_owner else getAccessTitle(access)

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