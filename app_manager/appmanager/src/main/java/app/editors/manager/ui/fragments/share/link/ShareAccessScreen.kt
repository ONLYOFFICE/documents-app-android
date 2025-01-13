package app.editors.manager.ui.fragments.share.link

import androidx.activity.compose.BackHandler
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import app.editors.manager.R
import app.editors.manager.managers.utils.ManagerUiUtils
import app.editors.manager.managers.utils.RoomUtils
import lib.compose.ui.theme.ManagerTheme
import lib.compose.ui.views.AppScaffold
import lib.compose.ui.views.AppSelectItem
import lib.compose.ui.views.AppTopBar
import lib.compose.ui.views.NestedColumn
import lib.toolkit.base.managers.utils.StringUtils

@Composable
fun ShareAccessScreen(
    currentAccess: Int?,
    fileExtension: String,
    useTabletPadding: Boolean = false,
    onSetUserAccess: (newAccess: Int) -> Unit,
    onBack: () -> Unit,
) {
    BackHandler(onBack = onBack)

    AppScaffold(
        useTablePaddings = useTabletPadding,
        topBar = {
            AppTopBar(title = R.string.share_choose_access_title, backListener = onBack)
        }
    ) {
        NestedColumn {
            ManagerUiUtils.getAccessList(
                extension = StringUtils.getExtension(fileExtension),
                removable = false,
                isDocSpace = true
            )
                .forEach { access ->
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

@Preview
@Composable
private fun Preview() {
    ManagerTheme {
        ShareAccessScreen(
            useTabletPadding = false,
            fileExtension = "pdf",
            currentAccess = 2,
            onSetUserAccess = {}
        ) {}
    }
}