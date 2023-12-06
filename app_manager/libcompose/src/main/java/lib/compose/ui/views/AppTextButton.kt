package lib.compose.ui.views

import androidx.compose.foundation.layout.Column
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import lib.compose.ui.theme.ManagerTheme

@Composable
fun AppTextButton(
    modifier: Modifier = Modifier,
    title: String,
    textColor: Color = MaterialTheme.colors.primary,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    TextButton(
        modifier = modifier,
        enabled = enabled,
        onClick = onClick,
        colors = ButtonDefaults.textButtonColors(contentColor = textColor),
    ) {
        Text(text = title)
    }
}

@Composable
fun AppTextButton(
    modifier: Modifier = Modifier,
    title: Int,
    textColor: Color = MaterialTheme.colors.primary,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    AppTextButton(
        modifier = modifier,
        title = stringResource(id = title),
        textColor = textColor,
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