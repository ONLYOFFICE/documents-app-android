package lib.compose.ui.views

import android.content.res.Configuration
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.LocalElevationOverlay
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import lib.compose.ui.theme.ManagerTheme
import lib.compose.ui.theme.colorTopAppBar
import lib.toolkit.base.R

@Composable
fun AppTopBar(
    title: @Composable () -> Unit,
    isClose: Boolean = false,
    tint: Color? = null,
    actions: @Composable (() -> Unit)? = null,
    backListener: (() -> Unit)? = null
) {
    CompositionLocalProvider(LocalElevationOverlay provides null) { // For correct background color
        TopAppBar(
            title = { title.invoke() },
            backgroundColor = MaterialTheme.colors.colorTopAppBar,
            navigationIcon = backListener?.let {
                {
                    IconButton(onClick = backListener) {
                        Icon(
                            painter = painterResource(id = if (isClose) R.drawable.ic_close else R.drawable.ic_back),
                            tint = tint ?: MaterialTheme.colors.primary,
                            contentDescription = null
                        )
                    }
                }
            }, actions = {
                actions?.invoke()
            }
        )
    }
}

@Composable
fun AppTopBar(
    title: String,
    isClose: Boolean = false,
    tint: Color? = null,
    actions: @Composable (() -> Unit)? = null,
    backListener: (() -> Unit)? = null
) {
    AppTopBar(
        title = {
            Text(
                text = title,
                color = MaterialTheme.colors.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(end = 16.dp)
            )
        },
        isClose = isClose,
        tint = tint,
        actions = actions,
        backListener = backListener
    )
}

@Composable
fun AppTopBar(
    @StringRes title: Int,
    isClose: Boolean = false,
    tint: Color? = null,
    actions: @Composable (() -> Unit)? = null,
    backListener: (() -> Unit)? = null
) {
    AppTopBar(
        title = stringResource(id = title),
        isClose = isClose,
        tint = tint,
        actions = actions,
        backListener = backListener
    )
}

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun TopAppBarPreview() {
    ManagerTheme {
        AppTopBar(
            title = R.string.app_title,
            actions = {
                TopAppBarAction(icon = R.drawable.drawable_ic_visibility_off, enabled = false) { }
                TopAppBarAction(icon = R.drawable.ic_close) { }
            }
        ) { }
    }
}

@Preview
@Composable
private fun TopAppBarPreviewNoNavigationButton() {
    ManagerTheme {
        AppTopBar(
            title = R.string.app_title,
            isClose = true,
            actions = {
                TopAppBarAction(icon = R.drawable.drawable_ic_logo) { }
                TopAppBarAction(icon = R.drawable.drawable_ic_visibility_off, enabled = false) { }
            }
        )
    }
}