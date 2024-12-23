package app.editors.manager.ui.fragments.share.link

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.editors.manager.managers.utils.ManagerUiUtils
import lib.compose.ui.addIfNotNull
import lib.compose.ui.theme.ManagerTheme
import lib.compose.ui.theme.colorTextSecondary
import lib.compose.ui.theme.colorTextTertiary
import lib.toolkit.base.R


@Composable
fun ExternalLinkItem(
    linkTitle: String,
    access: Int,
    hasPassword: Boolean,
    expiring: Boolean,
    isExpired: Boolean,
    canEdit: Boolean,
    onShareClick: () -> Unit,
    onClick: (() -> Unit)? = null,
) {
    Row(
        modifier = Modifier
            .height(dimensionResource(id = R.dimen.item_onehalf_line_height))
            .fillMaxWidth()
            .addIfNotNull(onClick) { clickable(onClick = it) }
            .padding(end = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .padding(start = 16.dp)
                .size(40.dp)
                .clip(CircleShape)
                .background(colorResource(id = R.color.colorIconBackground)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = ImageVector.vectorResource(app.editors.manager.R.drawable.ic_room_link),
                contentDescription = null,
                tint = MaterialTheme.colors.colorTextTertiary
            )
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 16.dp),
        ) {
            Text(text = linkTitle)
            if (isExpired) {
                Text(
                    text = stringResource(id = app.editors.manager.R.string.rooms_info_link_expired),
                    style = MaterialTheme.typography.body2,
                    color = MaterialTheme.colors.error
                )
            } else {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    AttributeIcon(icon = app.editors.manager.R.drawable.ic_small_lock, activate = hasPassword)
                    AttributeIcon(icon = app.editors.manager.R.drawable.ic_small_clock, activate = expiring)
                }
            }
        }
        IconButton(modifier = Modifier, onClick = onShareClick) {
            Icon(
                imageVector = ImageVector.vectorResource(R.drawable.ic_list_context_share),
                tint = MaterialTheme.colors.primary,
                contentDescription = null
            )
        }
        Icon(
            modifier = Modifier.padding(start = 8.dp),
            imageVector = ImageVector.vectorResource(ManagerUiUtils.getAccessIcon(access)),
            tint = MaterialTheme.colors.colorTextSecondary,
            contentDescription = null
        )
        if (canEdit) {
            Icon(
                imageVector = ImageVector.vectorResource(R.drawable.ic_arrow_right),
                tint = MaterialTheme.colors.colorTextTertiary,
                contentDescription = null
            )
        }
    }
}

@Composable
private fun AttributeIcon(icon: Int, activate: Boolean = true) {
    Box(
        modifier = Modifier
            .clip(CircleShape)
            .size(20.dp)
            .background(
                if (!activate) {
                    colorResource(id = R.color.colorIconBackground)
                } else {
                    MaterialTheme.colors.primary
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = ImageVector.vectorResource(icon),
            contentDescription = null,
            tint = if (!activate) {
                MaterialTheme.colors.colorTextTertiary
            } else {
                MaterialTheme.colors.onPrimary
            }
        )
    }
}

@Preview
@Composable
private fun Preview() {
    ManagerTheme {
        Surface {
            Column {
                ExternalLinkItem(
                    linkTitle = "Shared link",
                    access = 2,
                    hasPassword = true,
                    expiring = false,
                    isExpired = false,
                    canEdit = true,
                    {},
                ) {}
                ExternalLinkItem(
                    linkTitle = "Shared link", access = 2,
                    hasPassword = true,
                    expiring = false,
                    isExpired = true,
                    canEdit = false,
                    {},
                ) {}
            }
        }
    }
}