package app.editors.manager.ui.fragments.share.link

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.documents.core.network.common.contracts.ApiContract
import app.documents.core.network.share.models.GroupShare
import app.documents.core.network.share.models.ShareType
import app.editors.manager.R
import app.editors.manager.managers.utils.ManagerUiUtils
import app.editors.manager.managers.utils.RoomUtils
import lib.compose.ui.theme.ManagerTheme
import lib.compose.ui.views.AppDescriptionItem
import lib.compose.ui.views.AppHeaderItem
import lib.compose.ui.views.AppScaffold
import lib.compose.ui.views.AppSelectItem
import lib.compose.ui.views.AppTopBar

@Composable
fun RoomAccessScreen(
    roomType: Int,
    currentAccess: Int,
    ownerOrAdmin: Boolean,
    portal: String,
    isRemove: Boolean = false,
    users: List<GroupShare>? = null,
    onChangeAccess: (newAccess: Int) -> Unit,
    onUserClick: (String, Int, Boolean) -> Unit = { _, _, _ -> },
    onBack: () -> Unit
) {
    BackHandler(onBack = onBack)

    AppScaffold(
        useTablePaddings = false,
        topBar = {
            AppTopBar(title = R.string.share_choose_access_title, backListener = onBack)
        }
    ) {
        val options = RoomUtils.getAccessOptions(roomType, isRemove, ownerOrAdmin)
        if (users == null) {
            Column {
                options.forEach { access ->
                    AppSelectItem(
                        title = RoomUtils.getAccessTitle(access),
                        selected = currentAccess == access,
                        startIcon = ManagerUiUtils.getAccessIcon(access),
                        startIconTint = if (access == 0) MaterialTheme.colors.error else MaterialTheme.colors.primary
                    ) {
                        onChangeAccess.invoke(access)
                        onBack.invoke()
                    }
                }
            }
        } else {
            val groupOptions = options.minus(
                arrayOf(
                    ApiContract.ShareCode.ROOM_ADMIN,
                    ApiContract.ShareCode.POWER_USER
                )
            )

            Column {
                AppHeaderItem(title = R.string.share_access_room_type)
                groupOptions.forEach { access ->
                    AppSelectItem(
                        title = RoomUtils.getAccessTitle(access),
                        selected = currentAccess == access,
                        startIcon = ManagerUiUtils.getAccessIcon(access),
                        startIconTint = if (access == 0) MaterialTheme.colors.error else MaterialTheme.colors.primary
                    ) {
                        onChangeAccess.invoke(access)
                        onBack.invoke()
                    }
                }
                AppDescriptionItem(
                    modifier = Modifier.padding(top = 8.dp),
                    text = R.string.share_access_room_group_desc
                )
                ShareUsersList(
                    portal = portal,
                    shareList = users,
                    type = ShareType.User,
                    onClick = onUserClick
                )
            }
        }
    }
}

@Preview(locale = "ru")
@Composable
private fun Preview() {
    ManagerTheme {
        RoomAccessScreen(
            roomType = ApiContract.RoomType.COLLABORATION_ROOM,
            currentAccess = 2,
            portal = "",
//            users = listOf(
//                GroupShare(sharedTo = SharedTo(displayName = "Name"), canEditAccess = true),
//                GroupShare(isOwner = true),
//                GroupShare(),
//            ),
            ownerOrAdmin = true,
            onChangeAccess = {},
            onUserClick = { _, _, _ -> }
        ) {}
    }
}