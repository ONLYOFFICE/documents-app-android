package lib.compose.ui.views

import androidx.compose.foundation.layout.Column
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import lib.compose.ui.theme.ManagerTheme

@Composable
fun AppTextButton(
    modifier: Modifier = Modifier,
    title: String,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    Button(
        modifier = modifier,
        onClick = onClick,
        enabled = enabled,
        contentPadding = ButtonDefaults.TextButtonContentPadding,
        colors = ButtonDefaults.textButtonColors(),
        shape = MaterialTheme.shapes.large,
        elevation = ButtonDefaults.elevation(defaultElevation = 0.dp, pressedElevation = 0.dp)
    ) {
        Text(text = title)
    }
}

@Composable
fun AppTextButton(
    modifier: Modifier = Modifier,
    title: Int,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    AppTextButton(
        modifier = modifier,
        title = stringResource(id = title),
        enabled = enabled,
        onClick = onClick
    )
}

@Preview
@Composable
private fun AppTextButtonPreview() {
    ManagerTheme {
        AppScaffold {
            Column {
                AppTextButton(title = lib.toolkit.base.R.string.about_feedback) {}
                AppTextButton(title = lib.toolkit.base.R.string.about_feedback, enabled = false) {}
            }
        }
    }
}