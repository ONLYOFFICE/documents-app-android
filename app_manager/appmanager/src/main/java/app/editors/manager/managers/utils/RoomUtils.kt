package app.editors.manager.managers.utils

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import app.documents.core.model.cloud.Access
import app.documents.core.model.login.User
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

    fun getLinkAccessOptions(): List<Access> = listOf(
        Access.Editor,
        Access.Review,
        Access.Comment,
        Access.Read
    )

    fun getAccessOptions(roomType: Int, isRemove: Boolean, isAdmin: Boolean = false): List<Access> {
        return when (roomType) {
            ApiContract.RoomType.PUBLIC_ROOM -> {
                mutableListOf<Access>(
                    Access.ContentCreator
                )
            }

            ApiContract.RoomType.COLLABORATION_ROOM -> {
                mutableListOf(
                    Access.ContentCreator,
                    Access.Editor,
                    Access.Read
                )
            }

            ApiContract.RoomType.CUSTOM_ROOM -> {
                mutableListOf(
                   Access.ContentCreator,
                   Access.Editor,
                   Access.Review,
                   Access.Comment,
                   Access.Read
                )
            }

            ApiContract.RoomType.FILL_FORMS_ROOM -> {
                mutableListOf(
                    Access.ContentCreator,
                    Access.FormFiller
                )
            }

            else -> mutableListOf()
        }.apply {
            if (isRemove) add(Access.None)
            if (isAdmin) add(0, Access.RoomManager)
        }
    }

    fun getAccessTitleOrOwner(isOwner: Boolean, access: Int): Int =
        if (isOwner) R.string.share_access_room_owner else Access.get(access).toUi().title

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

    fun getUserRole(user: User): Int {
        return when {
            user.isOwner -> R.string.share_access_room_owner
            user.isAdmin -> R.string.share_access_room_docspace_admin
            user.isRoomAdmin -> R.string.share_access_room_admin
            user.isGuest -> R.string.profile_type_visitor
            else -> R.string.profile_type_user
        }
    }
}