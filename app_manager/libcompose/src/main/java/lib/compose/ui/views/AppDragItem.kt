package lib.compose.ui.views

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.gestures.PressGestureScope
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import lib.compose.ui.theme.colorTextSecondary
import lib.compose.ui.theme.colorTextTertiary


@Composable
fun AppDragItem(
    title: String,
    titleColor: Color = MaterialTheme.colors.onSurface,
    subtitle: String? = null,
    @DrawableRes startIcon: Int? = null,
    startIconTint: Color = MaterialTheme.colors.primary,
    dividerVisible: Boolean = true,
    onPress: PressGestureScope.(Offset) -> Unit,
) {
    AppListItem(
        title = title,
        titleColor = titleColor,
        subtitle = subtitle,
        startIcon = startIcon,
        startIconTint = startIconTint,
        dividerVisible = dividerVisible,
        endContent = {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .aspectRatio(1f)
                    .pointerInput(Unit) {
                        detectTapGestures(onPress = onPress)
                    }
            ) {
                Icon(
                    painter = painterResource(id = lib.toolkit.base.R.drawable.ic_drag_handle),
                    tint = MaterialTheme.colors.colorTextTertiary,
                    contentDescription = null,
                    modifier = Modifier
                )
            }
        }
    )
}

@Composable
fun AppDragItem(
    @StringRes title: Int,
    @StringRes subtitle: Int? = null,
    @DrawableRes startIcon: Int? = null,
    startIconTint: Color = MaterialTheme.colors.primary,
    dividerVisible: Boolean = true,
    onPress: PressGestureScope.(Offset) -> Unit,
) {
    AppDragItem(
        title = stringResource(id = title),
        subtitle = subtitle?.let { stringResource(id = subtitle) },
        startIcon = startIcon,
        startIconTint = startIconTint,
        dividerVisible = dividerVisible,
        onPress = onPress
    )
}

@Preview
@Composable
private fun AppDragItemPreview() {
    Surface {
        Column {
            AppDragItem(title = "Sheet 1", onPress = { })
            AppDragItem(title = "Sheet 1", onPress = { })
        }
    }
}