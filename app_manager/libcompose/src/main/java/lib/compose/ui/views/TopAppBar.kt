package lib.compose.ui.views

import android.util.Log
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import lib.toolkit.base.R

@Composable
fun TopAppBar(
    title: String,
    isClose: Boolean = false,
    tint: Color? = null,
    actions: @Composable (() -> Unit)? = null,
    backListener: () -> Unit
) {
    CompositionLocalProvider(LocalElevationOverlay provides null) { // For correct background color
        TopAppBar(
            title = {
                Text(
                    text = title,
                    color = MaterialTheme.colors.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(end = 16.dp)
                )
            },
            backgroundColor = MaterialTheme.colors.background,
            navigationIcon = {
                IconButton(onClick = backListener) {
                    Icon(
                        painter = painterResource(id = if (isClose) R.drawable.ic_close else R.drawable.ic_back),
                        tint = tint ?: MaterialTheme.colors.primary,
                        contentDescription = null
                    )
                }
            }, actions = {
                actions?.invoke()
            }
        )
    }
}

@Composable
fun TopAppBar(
    @StringRes title: Int,
    isClose: Boolean = false,
    tint: Color? = null,
    actions: @Composable (() -> Unit)? = null,
    backListener: () -> Unit
) {
    TopAppBar(
        title = stringResource(id = title),
        isClose = isClose,
        tint = tint,
        actions = actions,
        backListener = backListener
    )
}

@Preview
@Composable
private fun PreviewEditorsAppBar() {
    TopAppBar(title = R.string.app_title, isClose = true) {
        Log.d("PreviewEditorsAppBar", "PreviewEditorsAppBar: back click")
    }
}