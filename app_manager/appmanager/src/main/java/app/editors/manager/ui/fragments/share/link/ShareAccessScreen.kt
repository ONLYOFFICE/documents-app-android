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
fun ShareAccessScreen(
    currentAccess: Int?,
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
            getAccessList().forEach { access ->
                AppSelectItem(
                    title = RoomUtils.getAccessTitle(access),
                    selected = currentAccess == access,
                    startIcon = ManagerUiUtils.getAccessIcon(access),
                    startIconTint = MaterialTheme.colors.primary
                ) {
                    onSetUserAccess.invoke(access)
                    onBack.invoke()
                }
            }
        }
    }
}

private fun getAccessList(): List<Int> = listOf(
    ApiContract.ShareCode.EDITOR,
    ApiContract.ShareCode.REVIEW,
    ApiContract.ShareCode.COMMENT,
    ApiContract.ShareCode.READ,
    ApiContract.ShareCode.RESTRICT
)

@Preview
@Composable
private fun Preview() {
    ManagerTheme {
        ShareAccessScreen(
            currentAccess = 2,
            onSetUserAccess = {}
        ) {}
    }
}