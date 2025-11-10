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
import app.documents.core.network.common.contracts.ApiContract
import app.documents.core.network.share.models.ExternalLink
import app.editors.manager.R
import lib.compose.ui.theme.colorTextTertiary
import lib.compose.ui.views.AppDescriptionItem
import lib.compose.ui.views.AppHeaderItem
import lib.compose.ui.views.AppTextButton
import lib.toolkit.base.managers.utils.openSendTextActivity

@Composable
internal fun ExternalLinkBlock(
    sharedLinks: List<ExternalLink>,
    canEditRoom: Boolean,
    roomType: Int?,
    onLinkClick: (ExternalLink) -> Unit,
    onSharedLinkCreate: () -> Unit
) {
    val context = LocalContext.current
    val canAddLinks = sharedLinks.size < 6

    if (sharedLinks.isNotEmpty()) {
        AppDescriptionItem(
            modifier = Modifier.padding(top = 8.dp),
            text = if (roomType == ApiContract.RoomType.FILL_FORMS_ROOM) {
                R.string.rooms_info_fill_form_desc
            } else {
                R.string.rooms_info_access_desc
            }
        )

        Row {
            if (roomType != ApiContract.RoomType.FILL_FORMS_ROOM) {
                AppHeaderItem(
                    modifier = Modifier.weight(1f),
                    title = stringResource(id = R.string.rooms_share_shared_links_count, sharedLinks.size)
                )
                if (canEditRoom) {
                    IconButton(onClick = onSharedLinkCreate, enabled = canAddLinks) {
                        Icon(
                            imageVector = ImageVector.vectorResource(lib.toolkit.base.R.drawable.ic_default_add),
                            tint = if (canAddLinks) MaterialTheme.colors.primary else MaterialTheme.colors.colorTextTertiary,
                            contentDescription = null
                        )
                    }
                }
            } else {
                AppHeaderItem(title = R.string.rooms_share_shared_links)
            }
        }

        sharedLinks.forEach { link ->
            ExternalLinkItem(
                linkTitle = link.sharedTo.title,
                access = link.access,
                hasPassword = !link.sharedTo.password.isNullOrEmpty(),
                expiring = !link.sharedTo.expirationDate.isNullOrEmpty(),
                internal = link.sharedTo.internal == true,
                isExpired = link.sharedTo.isExpired,
                onShareClick = {
                    context.openSendTextActivity(
                        context.getString(R.string.toolbar_menu_main_share),
                        link.sharedTo.shareLink
                    )
                },
                onClick = { onLinkClick.invoke(link) }.takeIf { canEditRoom }
            )
        }
    } else {
        if (canEditRoom) {
            AppHeaderItem(title = R.string.rooms_share_shared_links)
            AppTextButton(
                modifier = Modifier.padding(start = 8.dp),
                title = R.string.rooms_info_create_link,
                onClick = onSharedLinkCreate
            )
        }
    }
}