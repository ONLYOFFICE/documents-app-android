package app.editors.manager.ui.views.custom

import android.content.res.Configuration
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.documents.core.model.cloud.Access
import app.editors.manager.R
import app.editors.manager.managers.utils.RoomUtils
import app.editors.manager.managers.utils.toUi
import lib.compose.ui.theme.ManagerTheme
import lib.compose.ui.theme.colorTextSecondary
import lib.compose.ui.views.AppDivider

@Composable
fun AccessDropdownMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    accessList: List<Access>,
    onClick: (Access) -> Unit,
) {
    DropdownMenu(
        onDismissRequest = onDismissRequest,
        expanded = expanded,
        content = { Content(accessList, onClick) }
    )
}

@Suppress("UnusedReceiverParameter")
@Composable
private fun ColumnScope.Content(accessList: List<Access>, onClick: (Access) -> Unit) {
    accessList.forEach { access ->
        val accessUi = access.toUi()
        if (access == Access.None) {
            AppDivider()
        }
        DropdownMenuItem(onClick = { onClick.invoke(access) }) {
            Icon(
                modifier = Modifier.padding(end = 16.dp),
                imageVector = ImageVector.vectorResource(
                    id = if (access == Access.None) {
                        R.drawable.ic_list_context_delete
                    } else {
                        accessUi.icon
                    }
                ),
                contentDescription = null,
                tint = if (access == Access.None) {
                    MaterialTheme.colors.error
                } else {
                    MaterialTheme.colors.colorTextSecondary
                }
            )
            Text(text = stringResource(id = accessUi.title))
        }
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL)
@Composable
private fun Preview() {
    ManagerTheme {
        Surface {
            Column {
                Content(accessList = RoomUtils.getAccessOptions(2, true)) {}
            }
        }
    }
}
