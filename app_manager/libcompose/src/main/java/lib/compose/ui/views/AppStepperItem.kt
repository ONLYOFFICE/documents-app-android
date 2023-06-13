package lib.compose.ui.views

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import lib.compose.ui.enabled
import lib.compose.ui.theme.ManagerTheme
import lib.toolkit.base.R


@Composable
fun AppStepperItem(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    enabled: Boolean = true,
    dividerVisible: Boolean = true,
    onDownClick: () -> Unit,
    onUpClick: () -> Unit
) {
    AppListItem(
        modifier = modifier,
        title = title,
        dividerVisible = dividerVisible,
        enabled = enabled,
        paddingEnd = 0.dp,
        endContent = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onDownClick, enabled = enabled) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_arrow_down),
                        contentDescription = null,
                        tint = MaterialTheme.colors.primary,
                        modifier = Modifier.enabled(enabled)
                    )
                }
                Text(
                    modifier = Modifier.widthIn(min = 40.dp),
                    text = value,
                    style = MaterialTheme.typography.body2,
                    color = MaterialTheme.colors.onSurface,
                    textAlign = TextAlign.Center
                )
                IconButton(onClick = onUpClick, enabled = enabled) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_arrow_down),
                        contentDescription = null,
                        tint = MaterialTheme.colors.primary,
                        modifier = Modifier
                            .rotate(180f)
                            .enabled(enabled)
                    )
                }
            }
        }
    )
}

@Composable
fun AppStepperItem(
    @StringRes title: Int,
    value: String,
    onDownClick: () -> Unit,
    onUpClick: () -> Unit
) {
    AppStepperItem(
        title = stringResource(id = title),
        value = value,
        onDownClick = onDownClick,
        onUpClick = onUpClick
    )
}

@Preview
@Composable
private fun AppStepperItemPreview() {
    ManagerTheme {
        Surface {
            Column {
                AppStepperItem(title = "Stepper Item", onDownClick = { }, onUpClick = { }, value = "123")
                AppStepperItem(
                    title = "Stepper Item",
                    value = "123",
                    enabled = false,
                    onDownClick = { },
                    onUpClick = { }
                )
            }
        }
    }
}