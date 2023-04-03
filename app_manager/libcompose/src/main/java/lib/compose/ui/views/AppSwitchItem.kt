package lib.compose.ui.views

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Column
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Switch
import androidx.compose.material.SwitchDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import lib.compose.ui.theme.ManagerTheme

@Composable
fun AppSwitchItem(
    modifier: Modifier = Modifier,
    title: String,
    checked: Boolean,
    subtitle: String? = null,
    enabled: Boolean = true,
    @DrawableRes startIcon: Int? = null,
    startIconTint: Color = MaterialTheme.colors.primary,
    dividerVisible: Boolean = true,
    onCheck: (Boolean) -> Unit
) {
    var checkedState by remember { mutableStateOf(checked) }

    SideEffect {
        checkedState = checked
    }

    AppListItem(
        modifier = modifier,
        title = title,
        subtitle = subtitle,
        startIcon = startIcon,
        startIconTint = startIconTint,
        dividerVisible = dividerVisible,
        enabled = enabled,
        endContent = {
            Switch(
                checked = checkedState,
                enabled = enabled,
                onCheckedChange = {
                    checkedState = !checkedState
                    onCheck(checkedState)
                },
                colors = SwitchDefaults.colors(checkedThumbColor = MaterialTheme.colors.primary)
            )
        },
        onClick = {
            checkedState = !checkedState
            onCheck(checkedState)
        }
    )
}

@Composable
fun AppSwitchItem(
    modifier: Modifier = Modifier,
    @StringRes title: Int,
    checked: Boolean,
    @StringRes subtitle: Int? = null,
    enabled: Boolean = true,
    @DrawableRes startIcon: Int? = null,
    startIconTint: Color = MaterialTheme.colors.primary,
    dividerVisible: Boolean = true,
    onCheck: (Boolean) -> Unit
) {
    AppSwitchItem(
        modifier = modifier,
        title = stringResource(id = title),
        subtitle = subtitle?.let { stringResource(it) },
        checked = checked,
        enabled = enabled,
        startIcon = startIcon,
        startIconTint = startIconTint,
        dividerVisible = dividerVisible,
        onCheck = onCheck
    )
}

@Preview
@Composable
private fun AppSwitchItemPreview() {
    ManagerTheme {
        Surface {
            Column {
                AppSwitchItem(title = "1231231231", checked = true) {}
                AppSwitchItem(title = "1231231231", checked = true, enabled = false) {}
                AppSwitchItem(title = "1231231231", checked = true) {}
            }
        }
    }
}