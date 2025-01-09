package app.editors.manager.ui.fragments.share.link

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.documents.core.model.cloud.Access
import app.editors.manager.R
import app.editors.manager.managers.utils.toUi
import lib.compose.ui.addIfNotNull
import lib.compose.ui.theme.ManagerTheme
import lib.compose.ui.theme.colorTextSecondary
import lib.compose.ui.theme.colorTextTertiary
import lib.compose.ui.views.AppDivider
import lib.toolkit.base.managers.utils.TimeUtils

@Composable
fun SharedLinkItem(
    modifier: Modifier = Modifier,
    access: Int,
    expirationDate: String?,
    internal: Boolean,
    isExpired: Boolean,
    onShareClick: () -> Unit,
    onClick: () -> Unit,
) {
    Row(
        modifier = modifier
            .height(dimensionResource(id = lib.toolkit.base.R.dimen.item_onehalf_line_height))
            .fillMaxWidth()
            .addIfNotNull(onClick) { clickable(onClick = it) },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .padding(start = 16.dp)
                .size(40.dp)
                .clip(CircleShape)
                .background(colorResource(id = lib.toolkit.base.R.color.colorIconBackground)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = ImageVector.vectorResource(R.drawable.ic_room_link),
                contentDescription = null,
                tint = MaterialTheme.colors.colorTextTertiary
            )
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 16.dp)
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    val context = LocalContext.current

                    Text(
                        text = if (internal) {
                            stringResource(id = R.string.rooms_share_shared_to_docsspace_users)
                        } else {
                            stringResource(id = R.string.rooms_share_shared_to_anyone)
                        },
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (isExpired) {
                        Text(
                            text = stringResource(id = R.string.rooms_info_link_expired),
                            style = MaterialTheme.typography.body2,
                            color = MaterialTheme.colors.error,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    } else {
                        if (expirationDate != null) {
                            val timeLeftString = TimeUtils.getDateTimeLeft(context, expirationDate)
                            if (timeLeftString == null) {
                                Text(
                                    text = stringResource(id = R.string.rooms_info_link_expired),
                                    style = MaterialTheme.typography.body2,
                                    color = MaterialTheme.colors.error
                                )
                            } else {
                                Text(
                                    text = stringResource(
                                        id = R.string.rooms_share_expires_after,
                                        timeLeftString
                                    ),
                                    style = MaterialTheme.typography.body2,
                                    color = MaterialTheme.colors.colorTextSecondary
                                )
                            }
                        } else {
                            Text(
                                text = stringResource(id = R.string.rooms_share_valid_unlimited),
                                style = MaterialTheme.typography.body2,
                                color = MaterialTheme.colors.colorTextSecondary
                            )
                        }
                    }
                }
                IconButton(modifier = Modifier, onClick = onShareClick) {
                    Icon(
                        imageVector = ImageVector.vectorResource(lib.toolkit.base.R.drawable.ic_list_context_share),
                        tint = MaterialTheme.colors.primary,
                        contentDescription = null
                    )
                }
                Icon(
                    modifier = Modifier.padding(start = 8.dp),
                    imageVector = ImageVector.vectorResource(Access.get(access).toUi().icon),
                    tint = MaterialTheme.colors.colorTextTertiary,
                    contentDescription = null
                )
                Icon(
                    modifier = Modifier.padding(end = 8.dp),
                    imageVector = ImageVector.vectorResource(lib.toolkit.base.R.drawable.ic_arrow_right),
                    tint = MaterialTheme.colors.colorTextTertiary,
                    contentDescription = null
                )
            }
            AppDivider()
        }
    }
}

@Preview
@Composable
private fun Preview() {
    ManagerTheme {
        Surface {
            Column {
                SharedLinkItem(
                    expirationDate = null,
                    internal = true,
                    access = 2,
                    isExpired = false,
                    onClick = {},
                    onShareClick = {}
                )
            }
        }
    }
}
