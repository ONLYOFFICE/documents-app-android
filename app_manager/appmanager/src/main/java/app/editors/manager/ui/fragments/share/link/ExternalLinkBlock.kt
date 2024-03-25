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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.documents.core.network.share.models.ExternalLink
import lib.compose.ui.addIfNotNull
import lib.compose.ui.theme.ManagerTheme
import lib.compose.ui.theme.colorTextTertiary
import lib.compose.ui.views.AppDescriptionItem
import lib.compose.ui.views.AppHeaderItem
import lib.compose.ui.views.AppTextButton
import lib.toolkit.base.R
import lib.toolkit.base.managers.utils.openSendTextActivity


@Composable
internal fun ExternalLinkBlock(
    generalLink: ExternalLink?,
    additionalLinks: List<ExternalLink>,
    canEditRoom: Boolean,
    onLinkClick: (ExternalLink) -> Unit,
    onGeneralLinkCreate: () -> Unit,
    onAdditionalLinkCreate: () -> Unit,
    onCopyLinkClick: (String) -> Unit
) {
    val context = LocalContext.current
    if (!canEditRoom && (generalLink != null || additionalLinks.isNotEmpty()) || canEditRoom) {
        AppDescriptionItem(
            modifier = Modifier.padding(top = 8.dp),
            text = app.editors.manager.R.string.rooms_info_access_desc
        )
    }
    if (!canEditRoom && generalLink != null || canEditRoom) {
        AppHeaderItem(title = app.editors.manager.R.string.rooms_info_general_link)
    }
    if (generalLink != null) {
        ExternalLinkItem(
            linkTitle = generalLink.sharedTo.title,
            access = generalLink.access,
            hasPassword = !generalLink.sharedTo.password.isNullOrEmpty(),
            expiring = false,
            isExpired = generalLink.sharedTo.isExpired,
            canEdit = canEditRoom,
            onCopyLinkClick = { onCopyLinkClick.invoke(generalLink.sharedTo.shareLink) },
            onShareClick = {
                context.openSendTextActivity(
                    context.getString(app.editors.manager.R.string.toolbar_menu_main_share),
                    generalLink.sharedTo.shareLink
                )
            },
            onClick = { onLinkClick.invoke(generalLink) }.takeIf { canEditRoom }
        )
    }

    if (canEditRoom && generalLink == null) {
        AppTextButton(
            modifier = Modifier.padding(start = 8.dp),
            title = app.editors.manager.R.string.rooms_info_create_link,
            onClick = onGeneralLinkCreate
        )
    }

    if (!canEditRoom && additionalLinks.isNotEmpty() || canEditRoom) {
        Row {
            AppHeaderItem(
                modifier = Modifier.weight(1f),
                title = stringResource(
                    id = app.editors.manager.R.string.rooms_info_additional_links,
                    additionalLinks.size,
                    RoomInfoFragment.MAX_ADDITIONAL_LINKS_COUNT
                )
            )
            if (additionalLinks.size in 1..4) {
                IconButton(onClick = onAdditionalLinkCreate) {
                    Icon(
                        imageVector = ImageVector.vectorResource(app.editors.manager.R.drawable.ic_action_button_docs_add),
                        tint = MaterialTheme.colors.primary,
                        contentDescription = null
                    )
                }
            }
        }
    }

    additionalLinks.forEach { link ->
        ExternalLinkItem(
            linkTitle = link.sharedTo.title,
            access = link.access,
            hasPassword = !link.sharedTo.password.isNullOrEmpty(),
            expiring = !link.sharedTo.expirationDate.isNullOrEmpty(),
            isExpired = link.sharedTo.isExpired,
            canEdit = canEditRoom,
            onCopyLinkClick = { onCopyLinkClick.invoke(link.sharedTo.shareLink) },
            onShareClick = {
                context.openSendTextActivity(
                    context.getString(app.editors.manager.R.string.toolbar_menu_main_share),
                    link.sharedTo.shareLink
                )
            },
            onClick = { onLinkClick.invoke(link) }.takeIf { canEditRoom }
        )
    }
    if (additionalLinks.isEmpty() && canEditRoom) {
        AppTextButton(
            modifier = Modifier.padding(start = 8.dp),
            title = app.editors.manager.R.string.rooms_info_create_link,
            onClick = onAdditionalLinkCreate
        )
    }
}

@Composable
private fun ExternalLinkItem(
    linkTitle: String,
    access: Int,
    hasPassword: Boolean,
    expiring: Boolean,
    isExpired: Boolean,
    canEdit: Boolean,
    onCopyLinkClick: () -> Unit,
    onShareClick: () -> Unit,
    onClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .height(dimensionResource(id = R.dimen.item_onehalf_line_height))
            .fillMaxWidth()
            .addIfNotNull(onClick) { clickable(onClick = it) },
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
        if (canEdit) {
            IconButton(modifier = Modifier, onClick = onCopyLinkClick) {
                Icon(
                    imageVector = ImageVector.vectorResource(app.editors.manager.R.drawable.ic_list_context_external_link),
                    tint = MaterialTheme.colors.primary,
                    contentDescription = null
                )
            }
            IconButton(modifier = Modifier, onClick = onShareClick) {
                Icon(
                    imageVector = ImageVector.vectorResource(R.drawable.ic_list_context_share),
                    tint = MaterialTheme.colors.primary,
                    contentDescription = null
                )
            }
            Icon(
                modifier = Modifier.padding(horizontal = 8.dp),
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
                    {}
                ) {}
                ExternalLinkItem(
                    linkTitle = "Shared link", access = 2,
                    hasPassword = true,
                    expiring = false,
                    isExpired = true,
                    canEdit = false,
                    {},
                    {}
                ) {}
            }
        }
    }
}