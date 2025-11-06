package app.editors.manager.ui.views.custom

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import app.documents.core.model.cloud.Access
import app.editors.manager.R
import app.editors.manager.mvp.models.ui.AccessUI
import lib.compose.ui.theme.colorTextSecondary
import lib.compose.ui.visible

@Composable
fun AccessIconButton(
    access: AccessUI,
    enabled: Boolean,
    accessList: List<AccessUI>,
    onAccess: (Access) -> Unit
) {
    var dropdown by remember { mutableStateOf(false) }

    IconButton(
        onClick = { dropdown = true },
        modifier = Modifier.padding(end = 16.dp),
        enabled = enabled
    ) {
        Row(
            modifier = Modifier,
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.End
        ) {
            Icon(
                imageVector = ImageVector.vectorResource(access.icon),
                contentDescription = null,
                tint = MaterialTheme.colors.colorTextSecondary
            )
            Spacer(modifier = Modifier.padding(end = 4.dp))
            Icon(
                modifier = Modifier.visible(enabled),
                imageVector = ImageVector.vectorResource(R.drawable.ic_drawer_menu_header_arrow),
                contentDescription = null,
                tint = MaterialTheme.colors.colorTextSecondary
            )
            AccessDropdownMenu(
                onDismissRequest = { dropdown = false },
                expanded = dropdown,
                accessList = accessList,
                onClick = { newAccess ->
                    onAccess.invoke(newAccess)
                    dropdown = false
                }
            )
        }
    }
}