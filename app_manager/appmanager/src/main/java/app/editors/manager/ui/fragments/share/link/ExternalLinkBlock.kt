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
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import app.documents.core.network.share.models.ExternalLink
import app.editors.manager.R
import lib.compose.ui.views.AppDescriptionItem
import lib.compose.ui.views.AppHeaderItem
import lib.compose.ui.views.AppTextButton
import lib.toolkit.base.managers.utils.openSendTextActivity


@Composable
internal fun ExternalLinkBlock(
    sharedLinks: List<ExternalLink>,
    canEditRoom: Boolean,
    onLinkClick: (ExternalLink) -> Unit,
    onSharedLinkCreate: () -> Unit,
    onCopyLinkClick: (String) -> Unit
) {
    val context = LocalContext.current
    if (sharedLinks.isNotEmpty()) {
        AppDescriptionItem(
            modifier = Modifier.padding(top = 8.dp),
            text = R.string.rooms_info_access_desc
        )

        Row {
            AppHeaderItem(modifier = Modifier.weight(1f), title = R.string.rooms_share_shared_links)
            if (canEditRoom) {
                IconButton(onClick = onSharedLinkCreate) {
                    Icon(
                        imageVector = ImageVector.vectorResource(R.drawable.ic_action_button_docs_add),
                        tint = MaterialTheme.colors.primary,
                        contentDescription = null
                    )
                }
            }
        }

        sharedLinks.forEach { link ->
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
                        context.getString(R.string.toolbar_menu_main_share),
                        link.sharedTo.shareLink
                    )
                },
                onClick = { onLinkClick.invoke(link) }.takeIf { canEditRoom }
            )
        }
    }

    if (canEditRoom && sharedLinks.isEmpty()) {
        AppTextButton(
            modifier = Modifier.padding(start = 8.dp),
            title = R.string.rooms_info_create_link,
            onClick = onSharedLinkCreate
        )
    }
}