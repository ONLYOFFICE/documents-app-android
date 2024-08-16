package lib.compose.ui.views

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.datasource.LoremIpsum
import androidx.compose.ui.unit.dp
import lib.compose.ui.theme.colorTextSecondary
import lib.compose.ui.theme.colorTextTertiary


@Composable
fun AppMultilineArrowItem(
    title: String,
    description: String,
    icon: Int? = null,
    selected: Boolean? = null,
    clickable: Boolean = true,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .clickable(enabled = clickable, onClick = onClick)
            .widthIn(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        icon?.let {
            Image(
                modifier = Modifier
                    .padding(start = 16.dp)
                    .size(40.dp),
                imageVector = ImageVector.vectorResource(id = icon),
                contentDescription = null
            )
        }
        Column(
            modifier = Modifier
                .padding(start = 16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.body1,
                        modifier = Modifier.align(Alignment.Start)
                    )
                    Text(
                        text = description,
                        style = MaterialTheme.typography.caption,
                        color = MaterialTheme.colors.colorTextSecondary,
                        modifier = Modifier.align(Alignment.Start)
                    )
                }
                if (selected != null) {
                    if (selected) {
                        Icon(
                            modifier = Modifier.padding(end = 16.dp),
                            imageVector = ImageVector.vectorResource(id = lib.toolkit.base.R.drawable.ic_done),
                            tint = MaterialTheme.colors.primary,
                            contentDescription = null
                        )
                    }
                } else {
                    if (clickable) {
                        Icon(
                            modifier = Modifier.padding(end = 16.dp),
                            imageVector = ImageVector.vectorResource(id = lib.toolkit.base.R.drawable.ic_arrow_right),
                            contentDescription = null,
                            tint = MaterialTheme.colors.colorTextTertiary
                        )
                    }
                }
            }
            AppDivider()
        }
    }
}

@Preview
@Composable
private fun Preview() {
    MaterialTheme {
        Surface {
            NestedColumn {
                AppMultilineArrowItem(
                    title = stringResource(id = lib.toolkit.base.R.string.app_title),
                    description = LoremIpsum(10).values.single().replace("\n", ""),
                    onClick = {}
                )
                AppMultilineArrowItem(
                    title = stringResource(id = lib.toolkit.base.R.string.app_title),
                    description = LoremIpsum(20).values.single().replace("\n", ""),
                    onClick = {}
                )
                AppMultilineArrowItem(
                    icon = lib.toolkit.base.R.drawable.ic_list_context_share,
                    title = stringResource(id = lib.toolkit.base.R.string.app_title),
                    description = LoremIpsum(20).values.single().replace("\n", ""),
                    onClick = {}
                )
            }
        }
    }
}