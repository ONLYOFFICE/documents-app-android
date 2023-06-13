package lib.compose.ui.views

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import lib.compose.ui.theme.ManagerTheme

@Composable
private fun AppBaseButton(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
    backgroundColor: Color = MaterialTheme.colors.primary,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(
            backgroundColor = backgroundColor
        ),
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
            .padding(horizontal = 24.dp)
    ) {
        content()
    }
}

@Composable
fun AppButton(modifier: Modifier = Modifier, title: String, enabled: Boolean = true, onClick: () -> Unit) {
    AppBaseButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier,
        content = {
            Text(text = title)
        }
    )
}

@Composable
fun AppButton(modifier: Modifier = Modifier, title: Int, enabled: Boolean = true, onClick: () -> Unit) {
    AppButton(
        modifier = modifier,
        enabled = enabled,
        title = stringResource(id = title),
        onClick = onClick
    )
}

@Composable
fun AppErrorButton(modifier: Modifier = Modifier, title: String, enabled: Boolean = true, onClick: () -> Unit) {
    AppBaseButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier,
        backgroundColor = MaterialTheme.colors.error,
        content = {
            Text(text = title)
        }
    )
}

@Composable
fun AppErrorButton(modifier: Modifier = Modifier, title: Int, enabled: Boolean = true, onClick: () -> Unit) {
    AppErrorButton(
        modifier = modifier,
        enabled = enabled,
        title = stringResource(id = title),
        onClick = onClick
    )
}

@Preview
@Composable
private fun AppButtonPreview() {
    ManagerTheme {
        AppScaffold {
            Column {
                AppButton(
                    modifier = Modifier.padding(top = 16.dp),
                    title = "App button"
                ) { }
                AppButton(
                    modifier = Modifier.padding(top = 16.dp),
                    enabled = false,
                    title = "Disabled app button"
                ) { }
                AppErrorButton(
                    modifier = Modifier.padding(top = 16.dp),
                    title = "Error button"
                ) { }
                AppErrorButton(
                    modifier = Modifier.padding(top = 16.dp),
                    enabled = false,
                    title = "Disabled error button"
                ) { }
            }
        }
    }
}