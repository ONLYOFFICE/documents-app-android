package lib.compose.ui.views

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Column
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import lib.compose.ui.enabled
import lib.toolkit.base.R

@Composable
fun AppSelectItem(
    title: String,
    selected: Boolean,
    subtitle: String? = null,
    enabled: Boolean = true,
    @DrawableRes startIcon: Int? = null,
    startIconTint: Color = MaterialTheme.colors.primary,
    dividerVisible: Boolean = true,
    onClick: () -> Unit,
) {
    AppListItem(
        title = title,
        subtitle = subtitle,
        enabled = enabled,
        startIcon = startIcon,
        startIconTint = startIconTint,
        dividerVisible = dividerVisible,
        onClick = onClick,
        endContent = {
            AnimatedVisibility(
                visible = selected,
                enter = fadeIn(animationSpec = tween(100)),
                exit = fadeOut(animationSpec = tween(100))
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_done),
                    tint = MaterialTheme.colors.primary,
                    contentDescription = null,
                    modifier = Modifier.enabled(enabled)
                )
            }
        }
    )
}

@Composable
fun AppSelectItem(
    @StringRes title: Int,
    @StringRes subtitle: Int? = null,
    selected: Boolean,
    enabled: Boolean = true,
    @DrawableRes startIcon: Int? = null,
    startIconTint: Color = MaterialTheme.colors.primary,
    dividerVisible: Boolean = true,
    onClick: () -> Unit,
) {
    AppSelectItem(
        title = stringResource(id = title),
        subtitle = subtitle?.let { stringResource(id = subtitle) },
        selected = selected,
        enabled = enabled,
        startIcon = startIcon,
        startIconTint = startIconTint,
        dividerVisible = dividerVisible,
        onClick = onClick
    )
}

@Preview
@Composable
fun AppSelectItemPreview() {
    Surface {
        Column {
            AppSelectItem(
                title = R.string.app_title,
                subtitle = R.string.about_feedback,
                selected = true,
            ) {}
            AppSelectItem(
                title = R.string.app_title,
                subtitle = R.string.about_feedback,
                selected = false,
            ) {}
            AppSelectItem(
                title = R.string.app_title,
                subtitle = R.string.about_feedback,
                selected = false,
            ) {}
        }
    }
}