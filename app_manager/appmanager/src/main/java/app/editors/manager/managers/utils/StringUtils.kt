package app.editors.manager.managers.utils

import android.content.Context
import app.documents.core.network.manager.models.explorer.CloudFile
import app.documents.core.network.manager.models.explorer.CloudFolder
import app.documents.core.network.manager.models.explorer.Item
import app.editors.manager.R
import app.editors.manager.managers.tools.ActionMenuItem
import lib.toolkit.base.managers.utils.StringUtils
import lib.toolkit.base.managers.utils.TimeUtils

internal object StringUtils {

    fun getCloudItemInfo(
        context: Context,
        item: Item,
        userId: String?,
        sortBy: String? = null,
        isSectionMy: Boolean = false
    ): String? {
        return when (item) {
            is CloudFolder -> getFolderInfo(context, item, userId, sortBy, isSectionMy)
            is CloudFile -> getFileInfo(context, item, userId, sortBy, isSectionMy)
            else -> return null
        }.filterNotNull().joinToString(context.getString(R.string.placeholder_point))
    }

    private fun getFolderInfo(
        context: Context,
        folder: CloudFolder,
        userId: String?,
        sortBy: String? = null,
        isSectionMy: Boolean = false
    ): Array<String?> {
        val date = TimeUtils.getWeekDate(folder.updated)
        val owner = getItemOwner(context, folder, userId).takeUnless { isSectionMy }

        return if (folder.isRoom) {
            val roomType = context.getString(RoomUtils.getRoomInfo(folder.roomType).title)
            when (sortBy) {
                ActionMenuItem.Date.sortValue -> arrayOf(date, roomType, owner)
                ActionMenuItem.Author.sortValue -> arrayOf(owner, roomType, date)
                ActionMenuItem.Type.sortValue -> arrayOf(roomType, owner, date)
                else -> arrayOf(roomType, owner, date)
            }
        } else arrayOf(owner, date)
    }

    private fun getFileInfo(
        context: Context,
        file: CloudFile,
        userId: String?,
        sortBy: String? = null,
        isSectionMy: Boolean = false
    ): Array<String?> {
        val date = TimeUtils.getWeekDate(file.updated)
        val owner = getItemOwner(context, file, userId).takeUnless { isSectionMy }
        val size = StringUtils.getFormattedSize(context, file.pureContentLength)

        return when (sortBy) {
            ActionMenuItem.Date.sortValue -> arrayOf(date, owner, size)
            ActionMenuItem.Author.sortValue -> arrayOf(owner, date, size)
            ActionMenuItem.Size.sortValue -> arrayOf(size, owner, date)
            else -> arrayOf(owner, date, size)
        }
    }

    private fun getItemOwner(context: Context, item: Item, userId: String?): String? {
        return when {
            userId.equals(item.createdBy.id, ignoreCase = true) -> context.getString(R.string.item_owner_self)
            item.createdBy.displayName.isNotEmpty() -> item.createdBy.displayName
            else -> null
        }
    }

}