package app.editors.manager.ui.fragments.share.link

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import app.documents.core.network.share.models.ExternalLink
import lib.compose.ui.views.AppDescriptionItem
import lib.compose.ui.views.AppHeaderItem
import lib.compose.ui.views.AppTextButton
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