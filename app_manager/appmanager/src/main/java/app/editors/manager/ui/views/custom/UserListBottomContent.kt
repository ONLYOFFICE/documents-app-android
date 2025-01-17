package app.editors.manager.ui.views.custom

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.documents.core.model.cloud.Access
import lib.compose.ui.theme.ManagerTheme
import lib.compose.ui.theme.colorTextSecondary
import lib.compose.ui.views.AppDivider
import lib.compose.ui.views.AppTextButton
import lib.toolkit.base.R

@Composable
fun UserListBottomContent(
    nextButtonTitle: Int,
    count: Int? = null,
    access: Access? = null,
    accessList: List<Access> = emptyList(),
    onAccess: (Access) -> Unit = {},
    onDelete: (() -> Unit)? = null,
    onNext: () -> Unit
) {
    AppDivider()
    Row(
        modifier = Modifier
            .height(56.dp)
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        onDelete?.let {
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = ImageVector.vectorResource(R.drawable.ic_close),
                    contentDescription = null,
                    tint = MaterialTheme.colors.colorTextSecondary
                )
            }
        }
        count?.let {
            Text(text = "$count", style = MaterialTheme.typography.h6, textAlign = TextAlign.Center)
        }
        access?.let {
            AccessIconButton(
                access = access,
                enabled = true,
                accessList = accessList,
                onAccess = onAccess::invoke
            )
        }
        Spacer(modifier = Modifier.weight(1f))
        AppTextButton(
            enabled = count?.let { count > 0 } ?: true,
            title = nextButtonTitle,
            onClick = onNext
        )
    }
}

@Composable
@Preview
private fun UserListBottomContentPreview() {
    ManagerTheme {
        Surface {
            UserListBottomContent(
                nextButtonTitle = R.string.common_next,
                count = 1,
                access = Access.Read,
                accessList = listOf(Access.None, Access.Restrict, Access.Read),
                onAccess = {},
                onDelete = {},
                onNext = {}
            )
        }
    }
}