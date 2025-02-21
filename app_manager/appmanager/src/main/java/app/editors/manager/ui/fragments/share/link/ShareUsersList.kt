package app.editors.manager.ui.fragments.share.link

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import app.documents.core.network.common.contracts.ApiContract
import app.documents.core.network.share.models.GroupShare
import app.documents.core.network.share.models.Share
import app.documents.core.network.share.models.ShareEntity
import app.documents.core.network.share.models.ShareType
import app.editors.manager.managers.utils.RoomUtils
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.bumptech.glide.integration.compose.placeholder
import lib.compose.ui.addIf
import lib.compose.ui.theme.colorTextSecondary
import lib.compose.ui.theme.colorTextTertiary
import lib.compose.ui.views.AnimatedVisibilityVerticalFade
import lib.compose.ui.views.AppDivider
import lib.toolkit.base.R


@Composable
internal fun ShareUsersList(
    portal: String?,
    shareList: List<ShareEntity>,
    type: ShareType,
    onClick: (String, Int, Boolean) -> Unit
) {
    var visible by remember { mutableStateOf(true) }
    val title = when (type) {
        ShareType.Admin -> app.editors.manager.R.string.rooms_info_admin_title
        ShareType.User -> app.editors.manager.R.string.rooms_info_users_title
        ShareType.Guests -> app.editors.manager.R.string.rooms_info_guests_title
        ShareType.Group -> app.editors.manager.R.string.rooms_info_groups_title
        ShareType.Expected -> app.editors.manager.R.string.rooms_info_expected_title
    }

    if (shareList.isNotEmpty()) {
        Column {
            Row(modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp)) {
                Text(
                    modifier = Modifier
                        .weight(1f)
                        .align(Alignment.Bottom),
                    text = stringResource(id = title, shareList.size),
                    style = MaterialTheme.typography.subtitle2,
                    color = MaterialTheme.colors.colorTextSecondary
                )
                IconButton(
                    modifier = Modifier.size(24.dp),
                    onClick = { visible = !visible }
                ) {
                    Icon(
                        modifier = Modifier.rotate(if (visible) 0f else 180f),
                        imageVector = ImageVector.vectorResource(R.drawable.ic_arrow_down),
                        contentDescription = null,
                        tint = MaterialTheme.colors.colorTextTertiary
                    )
                }
            }
            AnimatedVisibilityVerticalFade(visible = visible) {
                Column {
                    shareList.forEach { share ->
                        when (share) {
                            is Share -> {
                                ShareUserItem(share = share, portal = portal, key = type) {
                                    onClick.invoke(
                                        share.sharedTo.id,
                                        share.access.code,
                                        share.sharedTo.isOwner || share.sharedTo.isAdmin
                                    )
                                }
                            }
                            is GroupShare -> {
                                ShareUserItem(share = share, portal = portal, key = type) {
                                    onClick.invoke(
                                        share.sharedTo.id,
                                        share.access.code,
                                        share.sharedTo.isOwner || share.sharedTo.isAdmin
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
private fun ShareUserItem(share: ShareEntity, portal: String?, key: ShareType, onClick: () -> Unit) {
    Column(modifier = Modifier.addIf(share.canEditAccess) { clickable(onClick = onClick) }) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .fillMaxWidth()
                .height(56.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (key != ShareType.Group) {
                GlideImage(
                    modifier = Modifier
                        .padding(end = 16.dp)
                        .clip(CircleShape)
                        .size(40.dp)
                        .background(MaterialTheme.colors.colorTextTertiary),
                    model = "${ApiContract.SCHEME_HTTPS}$portal${share.sharedTo.avatarMedium}",
                    loading = placeholder(app.editors.manager.R.drawable.ic_account_placeholder),
                    contentDescription = null
                )
            } else {
                Box(
                    modifier = Modifier
                        .padding(end = 16.dp)
                        .clip(CircleShape)
                        .background(colorResource(id = R.color.colorIconBackground))
                        .size(40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = lib.toolkit.base.managers.utils.StringUtils.getAvatarName(share.sharedTo.nameHtml),
                        style = MaterialTheme.typography.h6,
                        color = MaterialTheme.colors.colorTextSecondary
                    )
                }
            }
            Text(
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.body1,
                text = if (key != ShareType.Group) share.sharedTo.displayNameHtml else share.sharedTo.nameHtml,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = stringResource(id = RoomUtils.getAccessTitleOrOwner(share.isOwner, share.access.code)),
                style = MaterialTheme.typography.body2,
                color = if (share.canEditAccess)
                    MaterialTheme.colors.colorTextSecondary else
                    MaterialTheme.colors.colorTextTertiary
            )
            if (share.canEditAccess) {
                Icon(
                    imageVector = ImageVector.vectorResource(R.drawable.ic_arrow_right),
                    contentDescription = null,
                    tint = MaterialTheme.colors.colorTextTertiary
                )
            }
        }
        AppDivider(startIndent = 16.dp + 40.dp + 16.dp)
    }
}
