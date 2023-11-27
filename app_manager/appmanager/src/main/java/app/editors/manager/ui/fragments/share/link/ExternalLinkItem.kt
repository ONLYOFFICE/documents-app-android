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
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import lib.compose.ui.theme.ManagerTheme
import lib.compose.ui.theme.colorTextTertiary
import lib.compose.ui.views.AppScaffold
import lib.toolkit.base.R


@Composable
fun ExternalLinkItem(linkTitle: String, accessCode: Int) {
    Row(
        modifier = Modifier
            .height(dimensionResource(id = R.dimen.item_onehalf_line_height))
            .fillMaxWidth()
            .clickable { },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .padding(start = 16.dp)
                .size(40.dp)
                .clip(CircleShape)
                .background(colorResource(id = R.color.colorBlack12)),
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
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                AttributeIcon(icon = app.editors.manager.R.drawable.ic_small_lock)
                AttributeIcon(icon = app.editors.manager.R.drawable.ic_small_clock)
            }
        }
        Icon(
            modifier = Modifier.padding(end = 24.dp),
            imageVector = ImageVector.vectorResource(app.editors.manager.R.drawable.ic_list_context_share),
            tint = MaterialTheme.colors.primary,
            contentDescription = null
        )
        Icon(
            modifier = Modifier.padding(end = 12.dp),
            imageVector = ImageVector.vectorResource(R.drawable.ic_arrow_right),
            tint = MaterialTheme.colors.colorTextTertiary,
            contentDescription = null
        )
    }
}

@Composable
private fun AttributeIcon(icon: Int) {
    Box(
        modifier = Modifier
            .clip(CircleShape)
            .size(20.dp)
            .background(colorResource(id = R.color.colorBackdrop)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = ImageVector.vectorResource(icon),
            contentDescription = null,
            tint = MaterialTheme.colors.colorTextTertiary
        )
    }
}

@Preview
@Composable
private fun Preview() {
    ManagerTheme {
        AppScaffold {
            ExternalLinkItem(linkTitle = "Shared link", accessCode = 2)
        }
    }
}