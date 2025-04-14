package app.editors.manager.ui.fragments.main.versionhistory

import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.AlertDialog
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import app.documents.core.model.cloud.isDocSpace
import app.documents.core.network.manager.models.explorer.CloudFile
import app.editors.manager.R
import app.editors.manager.app.accountOnline
import app.editors.manager.mvp.models.ui.FileVersionUi
import app.editors.manager.ui.dialogs.explorer.ExplorerContextItem
import lib.compose.ui.theme.ManagerTheme
import lib.compose.ui.theme.colorTextSecondary
import lib.compose.ui.views.AppCircularProgress
import lib.compose.ui.views.AppListItem
import lib.toolkit.base.managers.utils.UiUtils
import java.util.Date

enum class FileVersionDialogAction(
    @StringRes val titleResId: Int,
    @StringRes val subtitleResId: Int,
    @StringRes val confirmTextResId: Int,
    @StringRes val loadingTextResId: Int
){
    Restore(
        titleResId = R.string.version_restore_title,
        subtitleResId = R.string.version_restore_subtitle,
        confirmTextResId = R.string.version_restore_confirm,
        loadingTextResId = R.string.version_restore_loading
    ),
    Delete(
        titleResId = R.string.version_delete_title,
        subtitleResId = R.string.version_delete_subtitle,
        confirmTextResId = R.string.version_delete_confirm,
        loadingTextResId = R.string.version_delete_loading
    )
}

data class MenuItemData(
    val title: Int,
    val icon: Int,
    val onClick: () -> Unit,
    val showDivider: Boolean = false
)

@Composable
internal fun BottomSheetContextMenu(
    currentVersionItem: FileVersionUi,
    onContextMenuItemClick: (ExplorerContextItem) -> Unit,
    goToEditComment: () -> Unit
){
    val isDocSpace = LocalContext.current.accountOnline?.isDocSpace ?: false
    val canEditVersion = currentVersionItem.editAccess && isDocSpace
    val menuItems = buildList {
        add(
            MenuItemData(
                title = ExplorerContextItem.Open.title,
                icon = ExplorerContextItem.Open.icon,
                onClick = { onContextMenuItemClick(ExplorerContextItem.Open) }
            )
        )
        if (canEditVersion){
            add(
                MenuItemData(
                    title = ExplorerContextItem.EditComment.title,
                    icon = ExplorerContextItem.EditComment.icon,
                    onClick = goToEditComment
                )
            )
            if (!currentVersionItem.isCurrentVersion) {
                add(
                    MenuItemData(
                        title = ExplorerContextItem.Restore.title,
                        icon = ExplorerContextItem.Restore.icon,
                        onClick = { onContextMenuItemClick(ExplorerContextItem.Restore) }
                    )
                )
            }
        }
        add(
            MenuItemData(
                title = ExplorerContextItem.Download.title,
                icon = ExplorerContextItem.Download.icon,
                onClick = { onContextMenuItemClick(ExplorerContextItem.Download) },
                showDivider = canEditVersion && !currentVersionItem.isCurrentVersion
            )
        )
        if (canEditVersion && !currentVersionItem.isCurrentVersion){
            add(
                MenuItemData(
                    title = ExplorerContextItem.DeleteVersion.title,
                    icon = ExplorerContextItem.DeleteVersion.icon,
                    onClick = { onContextMenuItemClick(ExplorerContextItem.DeleteVersion) }
                )
            )
        }
    }
    BottomSheetContextMenu(menuItems, currentVersionItem)
}

@Composable
fun BottomSheetContextMenu(
    menuItems: List<MenuItemData>,
    currentVersionItem: FileVersionUi
){
    Column(
        modifier = Modifier
            .wrapContentHeight()
            .background(MaterialTheme.colors.surface)
            .padding(bottom = 8.dp)
    ) {
        DividerHandle()
        VersionItem(currentVersionItem)

        menuItems.forEach { item ->
            AppListItem(
                title = stringResource(item.title),
                startIcon = item.icon,
                startIconTint = MaterialTheme.colors.colorTextSecondary,
                dividerVisible = item.showDivider,
                onClick = item.onClick
            )
        }
    }
}

@Composable
private fun DividerHandle(modifier: Modifier = Modifier) {
    Image(
        painter = painterResource(id = lib.toolkit.base.R.drawable.ic_bottom_divider),
        contentDescription = null,
        alignment = Alignment.TopCenter,
        colorFilter = ColorFilter.tint(color = MaterialTheme.colors.onSurface),
        modifier = modifier
            .padding(8.dp)
            .fillMaxWidth()
    )
}

@Composable
fun ConfirmationDialog(
    item: ExplorerContextItem,
    onConfirm: (ExplorerContextItem) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
){
    val actionDialog = when(item){
        ExplorerContextItem.Restore -> FileVersionDialogAction.Restore
        ExplorerContextItem.DeleteVersion -> FileVersionDialogAction.Delete
        else -> null
    }
    actionDialog?.let { action ->
        AlertDialog(
            onDismissRequest = onDismiss,
            confirmButton = {
                TextButton(onClick = { onConfirm(item) }) {
                    Text(stringResource(action.confirmTextResId))
                }
            },
            dismissButton = {
                TextButton(onClick = { onDismiss() }) {
                    Text(stringResource(R.string.version_dialog_cancel))
                }
            },
            shape = RoundedCornerShape(8.dp),
            title = {
                Text(
                    text = stringResource(action.titleResId),
                    style = MaterialTheme.typography.h6
                )
            },
            text = {
                Text(
                    text = stringResource(action.subtitleResId),
                    style = MaterialTheme.typography.body2
                )
            },
            modifier = modifier.fillMaxWidth(
                if (UiUtils.isTablet(LocalContext.current)) 0.7f else 1.0f
            )
        )
    }
}

@Composable
fun LoadingDialog(
    item: ExplorerContextItem,
    modifier: Modifier = Modifier
){
    val actionDialog = when(item){
        ExplorerContextItem.Restore -> FileVersionDialogAction.Restore
        ExplorerContextItem.DeleteVersion -> FileVersionDialogAction.Delete
        else -> null
    }
    actionDialog?.let { action ->
        Dialog(onDismissRequest = { }) {
            Card(
                modifier = modifier
                    .fillMaxWidth(if (UiUtils.isTablet(LocalContext.current)) 0.7f else 1.0f)
                    .height(200.dp)
                    .padding(16.dp),
                shape = RoundedCornerShape(8.dp),
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(
                        space = 16.dp,
                        alignment = Alignment.CenterVertically
                    ),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(action.loadingTextResId),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.h6
                    )
                    AppCircularProgress()
                }
            }
        }
    }
}

@Preview
@Composable
fun ConfirmationDialogPreview(){
    ManagerTheme {
        ConfirmationDialog(
            item = ExplorerContextItem.Restore,
            onConfirm = {},
            onDismiss = {}
        )
    }
}

@Preview
@Composable
fun BottomSheetContextMenuPreview(){
    ManagerTheme {
        val item = FileVersionUi(
            version = 1,
            versionGroup = "1",
            fileId = "",
            date = Date(12312312),
            initiatorDisplayName = "John Krasinski",
            comment = "Edited",
            fileExst = "docx",
            viewUrl = "",
            title = "",
            isCurrentVersion = false,
            editAccess = true,
            file = CloudFile()
        )
        BottomSheetContextMenu(
            currentVersionItem = item,
            onContextMenuItemClick = {},
            goToEditComment = {}
        )
    }
}