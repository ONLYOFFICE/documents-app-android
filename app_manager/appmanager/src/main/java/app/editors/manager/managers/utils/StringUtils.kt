package app.editors.manager.managers.utils

import android.content.Context
import app.documents.core.network.manager.models.explorer.CloudFile
import app.documents.core.network.manager.models.explorer.CloudFolder
import app.documents.core.network.manager.models.explorer.Item
import app.editors.manager.R
import app.editors.manager.managers.tools.ActionMenuItem
import app.editors.manager.ui.adapters.AdapterState
import lib.toolkit.base.managers.utils.StringUtils
import lib.toolkit.base.managers.utils.TimeUtils

internal object StringUtils {

    fun getCloudItemInfo(
        context: Context,
        item: Item,
        state: AdapterState?
    ): String? {
        return when (item) {
            is CloudFolder -> getFolderInfo(context, item, state)
            is CloudFile -> getFileInfo(context, item, state)
            else -> return null
        }.joinToString(context.getString(R.string.placeholder_point))
    }

    private fun getFolderInfo(
        context: Context,
        folder: CloudFolder,
        state: AdapterState?
    ): List<String> {
        if (state == null) {
            return listOfNotNull(
                folder.createdBy.displayNameFromHtml,
                TimeUtils.getWeekDate(folder.updated)
            )
        }


        val date = TimeUtils.getWeekDate(folder.updated)
        val owner = getItemOwner(context, folder, state.accountId).takeUnless { state.isSectionMy }

        return when {
            state.isIndexing -> {
                listOfNotNull(
                    context.getString(R.string.rooms_index_subtitle, folder.order),
                    date,
                    owner
                )
            }
            folder.isRoom -> {
                getRoomInfo(
                    roomType = folder.roomType,
                    context = context,
                    date = date,
                    owner = owner,
                    isGridView = state.isGridView,
                    sortBy = state.sortBy
                )
            }
            else -> listOfNotNull(owner, date)
        }
    }

    fun getRoomInfo(
        roomType: Int,
        context: Context,
        date: String?,
        owner: String?,
        isGridView: Boolean,
        sortBy: String?
    ) : List<String> {
        val roomTypeTitle = context.getString(RoomUtils.getRoomInfo(roomType).title)
        if (isGridView) {
            return listOfNotNull(roomTypeTitle)
        }

        return when (sortBy) {
            ActionMenuItem.Date.sortValue -> listOfNotNull(date, roomTypeTitle, owner)
            ActionMenuItem.Author.sortValue -> listOfNotNull(owner, roomTypeTitle, date)
            ActionMenuItem.Type.sortValue -> listOfNotNull(roomTypeTitle, owner, date)
            else -> listOfNotNull(roomTypeTitle, owner, date)
        }
    }

    private fun getFileInfo(
        context: Context,
        file: CloudFile,
        state: AdapterState?
    ): List<String> {
        if (state == null) {
            return listOfNotNull(
                file.createdBy.displayNameFromHtml,
                TimeUtils.getWeekDate(file.updated)
            )
        }

        val date = TimeUtils.getWeekDate(file.updated)
        val owner = getItemOwner(context, file, state.accountId).takeUnless { state.isSectionMy }

        if (state.isIndexing) {
            return listOfNotNull(
                context.getString(R.string.rooms_index_subtitle, file.order),
                date,
                owner
            )
        }

        val size = StringUtils.getFormattedSize(context, file.pureContentLength)
        return when (state.sortBy) {
            ActionMenuItem.Date.sortValue -> listOfNotNull(date, owner, size)
            ActionMenuItem.Author.sortValue -> listOfNotNull(owner, date, size)
            ActionMenuItem.Size.sortValue -> listOfNotNull(size, owner, date)
            else -> listOfNotNull(owner, date, size)
        }
    }

    fun getItemOwner(context: Context, item: Item, userId: String?): String? {
        return when {
            userId.equals(
                item.createdBy.id,
                ignoreCase = true
            ) -> context.getString(R.string.item_owner_self)

            item.createdBy.displayName.isNotEmpty() -> item.createdBy.displayNameFromHtml
            else -> null
        }
    }
}