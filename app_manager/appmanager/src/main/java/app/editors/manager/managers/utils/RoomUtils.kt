package app.editors.manager.managers.utils

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import app.documents.core.network.common.contracts.ApiContract
import app.documents.core.network.share.models.Share
import app.editors.manager.R


data class RoomInfo(@DrawableRes val icon: Int, @StringRes val title: Int, @StringRes val description: Int)

object RoomUtils {

    fun getRoomInfo(type: Int): RoomInfo {
        val icon = when (type) {
            2 -> R.drawable.ic_collaboration_room
            6 -> R.drawable.ic_public_room
            5 -> R.drawable.ic_custom_room
            else -> {
                R.drawable.ic_collaboration_room
            }
        }
        val title = when (type) {
            2 -> R.string.rooms_add_collaboration
            6 -> R.string.rooms_add_public_room
            5 -> R.string.rooms_add_custom
            else -> {
                R.string.rooms_add_collaboration
            }
        }
        val des = when (type) {
            2 -> R.string.rooms_add_collaboration_des
            6 -> R.string.rooms_add_public_room_des
            5 -> R.string.rooms_add_custom_des
            else -> {
                R.string.rooms_add_collaboration_des
            }
        }
        return RoomInfo(icon, title, des)
    }

    fun getAccessTitle(access: Int): Int = when (access) {
        ApiContract.ShareCode.ROOM_ADMIN -> R.string.share_access_room_admin
        ApiContract.ShareCode.POWER_USER -> R.string.share_access_room_power_user
        ApiContract.ShareCode.EDITOR -> R.string.share_access_room_editor
        ApiContract.ShareCode.FILL_FORMS -> R.string.share_access_room_form_filler
        ApiContract.ShareCode.REVIEW -> R.string.share_access_room_reviewer
        ApiContract.ShareCode.COMMENT -> R.string.share_access_room_commentator
        ApiContract.ShareCode.NONE -> R.string.share_popup_access_deny_remove
        else -> R.string.share_access_room_viewer
    }

    fun getAccessTitleOrOwner(share: Share): Int =
        if (share.isOwner) R.string.share_access_room_owner else getAccessTitle(share.intAccess)
    fun getRoomInitials(title: String): String? {
        return try {
            val words = title.split(" ")
            when (words.size) {
                1 -> title[0].toString()
                2 -> "${words[0][0]}${words[1][0]}"
                else -> "${words[0][0]}${words[words.lastIndex][0]}"
            }.uppercase()
        } catch (_: IndexOutOfBoundsException) {
            null
        }
    }

}