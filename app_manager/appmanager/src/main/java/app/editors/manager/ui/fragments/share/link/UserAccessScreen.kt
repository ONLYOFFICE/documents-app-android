package app.editors.manager.ui.fragments.share.link

import androidx.compose.foundation.layout.Column
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ScaffoldState
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import app.documents.core.network.common.contracts.ApiContract
import app.editors.manager.R
import app.editors.manager.managers.utils.ManagerUiUtils
import app.editors.manager.managers.utils.RoomUtils
import app.editors.manager.viewModels.link.RoomInfoViewModel
import lib.compose.ui.views.AppScaffold
import lib.compose.ui.views.AppSelectItem
import lib.compose.ui.views.AppTopBar

@Composable
fun UserAccessScreen(
    scaffoldState: ScaffoldState,
    viewModel: RoomInfoViewModel,
    navController: NavController,
    roomId: String,
    userId: String,
    currentAccess: Int?,
) {
    AppScaffold(
        scaffoldState = scaffoldState,
        topBar = {
            AppTopBar(title = R.string.share_choose_access_title, backListener = navController::popBackStack)
        }
    ) {
        Column {
            listOf(
                ApiContract.ShareCode.ROOM_ADMIN,
                ApiContract.ShareCode.POWER_USER,
                ApiContract.ShareCode.EDITOR,
                ApiContract.ShareCode.READ,
                ApiContract.ShareCode.NONE
            ).forEach { access ->
                AppSelectItem(
                    title = RoomUtils.getAccessTitle(access),
                    selected = currentAccess == access,
                    startIcon = ManagerUiUtils.getAccessIcon(access),
                    startIconTint = if (access == 0) MaterialTheme.colors.error else MaterialTheme.colors.primary
                ) {
                    viewModel.setUserAccess(roomId, userId, access)
                    navController.popBackStack()
                }
            }
        }
    }
}