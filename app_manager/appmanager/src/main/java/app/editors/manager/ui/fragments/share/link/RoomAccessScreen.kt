package app.editors.manager.ui.fragments.share.link

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import app.documents.core.network.common.contracts.ApiContract
import app.editors.manager.R
import app.editors.manager.managers.utils.ManagerUiUtils
import app.editors.manager.managers.utils.RoomUtils
import lib.compose.ui.theme.ManagerTheme
import lib.compose.ui.views.AppScaffold
import lib.compose.ui.views.AppSelectItem
import lib.compose.ui.views.AppTopBar

@Composable
fun RoomAccessScreen(
    roomType: Int?,
    currentAccess: Int?,
    ownerOrAdmin: Boolean,
    onSetUserAccess: (newAccess: Int) -> Unit,
    onBack: () -> Unit
) {
    BackHandler(onBack = onBack)

    AppScaffold(
        useTablePaddings = false,
        topBar = {
            AppTopBar(title = R.string.share_choose_access_title, backListener = onBack)
        }
    ) {
        Column {
            getAccessList(ownerOrAdmin, roomType).forEach { access ->
                AppSelectItem(
                    title = RoomUtils.getAccessTitle(access),
                    selected = currentAccess == access,
                    startIcon = ManagerUiUtils.getAccessIcon(access),
                    startIconTint = if (access == 0) MaterialTheme.colors.error else MaterialTheme.colors.primary
                ) {
                    onSetUserAccess.invoke(access)
                    onBack.invoke()
                }
            }
        }
    }
}

private fun getAccessList(ownerOrAdmin: Boolean, roomType: Int?) = when {
    ownerOrAdmin -> listOf(
        ApiContract.ShareCode.ROOM_ADMIN,
        ApiContract.ShareCode.NONE
    )
    roomType == ApiContract.RoomType.COLLABORATION_ROOM -> listOf(
        ApiContract.ShareCode.ROOM_ADMIN,
        ApiContract.ShareCode.POWER_USER,
        ApiContract.ShareCode.EDITOR,
        ApiContract.ShareCode.READ,
        ApiContract.ShareCode.NONE
    )
    else -> listOf(
        ApiContract.ShareCode.ROOM_ADMIN,
        ApiContract.ShareCode.POWER_USER,
        ApiContract.ShareCode.EDITOR,
        ApiContract.ShareCode.FILL_FORMS,
        ApiContract.ShareCode.REVIEW,
        ApiContract.ShareCode.COMMENT,
        ApiContract.ShareCode.READ,
        ApiContract.ShareCode.NONE
    )
}

@Preview(locale = "ru")
@Composable
private fun Preview() {
    ManagerTheme {
        RoomAccessScreen(
            roomType = ApiContract.RoomType.COLLABORATION_ROOM,
            currentAccess = 2,
            ownerOrAdmin = false,
            onSetUserAccess = {}
        ) {}
    }
}