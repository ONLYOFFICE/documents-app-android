package app.editors.manager.ui.fragments.share.link

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.documents.core.model.cloud.Access
import app.documents.core.network.share.models.GroupShare
import app.documents.core.network.share.models.ShareEntity
import app.editors.manager.R
import app.editors.manager.mvp.models.ui.AccessUI
import lib.compose.ui.theme.ManagerTheme
import lib.compose.ui.views.AppDescriptionItem
import lib.compose.ui.views.AppHeaderItem
import lib.compose.ui.views.AppScaffold
import lib.compose.ui.views.AppSelectItem
import lib.compose.ui.views.AppTopBar

@Composable
fun ChangeUserAccessScreen(
    currentAccess: Access,
    accessList: List<AccessUI>,
    portal: String = "",
    users: List<GroupShare>? = null,
    onUserClick: (ShareEntity) -> Unit = { },
    onChangeAccess: (newAccess: Access) -> Unit,
    onBack: () -> Unit
) {
    BackHandler(onBack = onBack)

    AppScaffold(
        useTablePaddings = false,
        topBar = {
            AppTopBar(title = R.string.share_choose_access_title, backListener = onBack)
        }
    ) {
        if (users == null) {
            Column {
                accessList.forEach { access ->
                    AppSelectItem(
                        title = access.title,
                        selected = currentAccess == access.access,
                        startIcon = access.icon,
                        startIconTint = if (access.access == Access.None)
                            MaterialTheme.colors.error else
                            MaterialTheme.colors.primary
                    ) {
                        onChangeAccess.invoke(access.access)
                        onBack.invoke()
                    }
                }
            }
        } else {
            val groupOptions = accessList.filter { it.access !is Access.RoomManager }

            Column {
                AppHeaderItem(title = R.string.share_access_room_type)
                groupOptions.forEach { access ->
                    AppSelectItem(
                        title = access.title,
                        selected = currentAccess == access.access,
                        startIcon = access.icon,
                        startIconTint = if (access.access == Access.None)
                            MaterialTheme.colors.error else
                            MaterialTheme.colors.primary
                    ) {
                        onChangeAccess.invoke(access.access)
                        onBack.invoke()
                    }
                }
                AppDescriptionItem(
                    modifier = Modifier.padding(top = 8.dp),
                    text = R.string.share_access_room_group_desc
                )
                ShareUsersList(
                    isRoom = true,
                    portal = portal,
                    shareList = users,
                    title = R.string.rooms_info_users_title,
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
        ChangeUserAccessScreen(
            currentAccess = Access.Read,
            portal = "",
//            users = listOf(
//                GroupShare(sharedTo = SharedTo(displayName = "Name"), canEditAccess = true),
//                GroupShare(isOwner = true),
//                GroupShare(),
//            ),
            onChangeAccess = {},
            accessList = emptyList()
        ) {}
    }
}