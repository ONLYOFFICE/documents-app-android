package lib.compose.ui.views

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import lib.compose.ui.theme.ManagerTheme
import lib.toolkit.base.R

/**
 * Button types for the app.
 */
enum class AppButtonType {
    Default,
    Error
}

/**
 * Universal button composable for the app.
 */
@Composable
fun AppButton(
    modifier: Modifier = Modifier,
    title: String? = null,
    titleResId: Int? = null,
    enabled: Boolean = true,
    buttonType: AppButtonType = AppButtonType.Default,
    onClick: () -> Unit,
) {
    val buttonText = titleResId?.let { stringResource(id = it) } ?: title ?: ""
    val backgroundColor = when (buttonType) {
        AppButtonType.Default -> MaterialTheme.colors.primary
        AppButtonType.Error -> MaterialTheme.colors.error
    }

    Button(
        onClick = onClick,
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(
            backgroundColor = backgroundColor
        ),
        modifier = modifier
            .padding(top = 16.dp)
            .width(dimensionResource(id = R.dimen.default_button_width))
            .heightIn(min = dimensionResource(id = R.dimen.default_button_height))
    ) {
        Text(
            text = buttonText,
            textAlign = TextAlign.Center
        )
    }
}


@Preview
@Preview(uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun AppButtonPreview() {
    ManagerTheme {
        Surface(color = MaterialTheme.colors.background) {
            Column(modifier = Modifier.padding(24.dp)) {
                AppButton(
                    title = "App button"
                ) { }
                AppButton(
                    enabled = false,
                    title = "Disabled app button"
                ) { }
                AppButton(
                    title = "Error button",
                    buttonType = AppButtonType.Error
                ) { }
                AppButton(
                    enabled = false,
                    title = "Disabled error button",
                    buttonType = AppButtonType.Error
                ) { }
            }
        }
    }
}