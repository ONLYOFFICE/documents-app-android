package lib.compose.ui.views

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import lib.compose.ui.theme.colorTextSecondary

@Composable
private fun DropdownMenuButton(
    modifier: Modifier = Modifier,
    state: State<Boolean>,
    title: @Composable () -> Unit,
    items: @Composable ColumnScope.() -> Unit,
    onDismiss: () -> Unit,
    onClick: () -> Unit,
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        title()
        Icon(
            imageVector = ImageVector.vectorResource(lib.toolkit.base.R.drawable.ic_dropdown),
            contentDescription = null,
            tint = MaterialTheme.colors.colorTextSecondary
        )
        DropdownMenu(
            state = state,
            items = items,
            onDismiss = onDismiss
        )
    }
}


@Composable
fun DropdownMenuButton(
    modifier: Modifier = Modifier,
    state: State<Boolean>,
    icon: ImageVector,
    items: @Composable ColumnScope.() -> Unit,
    onDismiss: () -> Unit,
    onClick: () -> Unit,
) {
    DropdownMenuButton(
        modifier = modifier,
        state = state,
        title = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colors.colorTextSecondary
            )
        },
        items = items,
        onDismiss = onDismiss,
        onClick = onClick
    )
}

@Composable
fun DropdownMenuButton(
    modifier: Modifier = Modifier,
    state: State<Boolean>,
    title: String,
    items: @Composable ColumnScope.() -> Unit,
    onDismiss: () -> Unit,
    onClick: () -> Unit,
) {
    DropdownMenuButton(
        modifier = modifier,
        state = state,
        title = {
            Text(
                text = title,
                color = MaterialTheme.colors.colorTextSecondary
            )
        },
        items = items,
        onDismiss = onDismiss,
        onClick = onClick
    )
}

@Composable
fun DropdownMenuItem(
    title: String,
    selected: Boolean,
    startIcon: Int? = null,
    onClick: () -> Unit,
) {
    androidx.compose.material.DropdownMenuItem(onClick = onClick) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f).padding(end = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                startIcon?.let {
                    Icon(
                        imageVector = ImageVector.vectorResource(startIcon),
                        contentDescription = null,
                        tint = MaterialTheme.colors.colorTextSecondary
                    )
                }
                Text(title, maxLines = 1)
            }
            if (selected) {
                Icon(
                    imageVector = ImageVector.vectorResource(lib.toolkit.base.R.drawable.ic_done),
                    tint = MaterialTheme.colors.primary,
                    contentDescription = null
                )
            }
        }
    }
}

@Composable
private fun DropdownMenu(
    modifier: Modifier = Modifier,
    state: State<Boolean>,
    onDismiss: () -> Unit,
    items: @Composable ColumnScope.() -> Unit,
) {
    androidx.compose.material.DropdownMenu(
        modifier = modifier.widthIn(230.dp),
        offset = DpOffset(0.dp, 16.dp),
        expanded = state.value,
        onDismissRequest = onDismiss,
        content = items
    )
}