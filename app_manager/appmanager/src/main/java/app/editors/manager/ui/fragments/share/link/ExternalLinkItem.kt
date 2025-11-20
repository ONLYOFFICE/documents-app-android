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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.documents.core.model.cloud.Access
import app.editors.manager.managers.utils.toUi
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
    internal: Boolean,
    onShareClick: () -> Unit,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
) {
    Row(
        modifier = modifier
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
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (hasPassword) {
                        AttributeIcon(app.editors.manager.R.drawable.ic_small_lock)
                    }
                    if (expiring) {
                        AttributeIcon(app.editors.manager.R.drawable.ic_small_clock)
                    }
                    Text(
                        text = if (internal) {
                            stringResource(id = app.editors.manager.R.string.rooms_share_shared_to_docsspace_users)
                        } else {
                            stringResource(id = app.editors.manager.R.string.rooms_share_shared_to_anyone)
                        },
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.body2,
                        color = MaterialTheme.colors.colorTextSecondary
                    )
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
            imageVector = ImageVector.vectorResource(Access.get(access).toUi().icon),
            tint = MaterialTheme.colors.colorTextSecondary,
            contentDescription = null
        )
        if (onClick != null) {
            Icon(
                imageVector = ImageVector.vectorResource(R.drawable.ic_arrow_right),
                tint = MaterialTheme.colors.colorTextTertiary,
                contentDescription = null
            )
        }
    }
}

@Composable
private fun AttributeIcon(icon: Int) {
    Box(
        modifier = Modifier
            .clip(CircleShape)
            .size(20.dp)
            .background(MaterialTheme.colors.primary),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = ImageVector.vectorResource(icon),
            contentDescription = null,
            tint = MaterialTheme.colors.onPrimary
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
                    internal = true,
                    onClick = {},
                    onShareClick = {}
                )
                ExternalLinkItem(
                    linkTitle = "Shared link",
                    access = 2,
                    hasPassword = true,
                    expiring = false,
                    isExpired = true,
                    internal = false,
                    onClick = {},
                    onShareClick = {}
                )
            }
        }
    }
}