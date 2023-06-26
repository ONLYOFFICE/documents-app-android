package lib.compose.ui.views

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role.Companion.Image
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import lib.compose.ui.theme.ManagerTheme
import lib.compose.ui.theme.colorGrey
import lib.toolkit.base.R

/**
 * @param startIconTint pass null so as not to apply any tint
 * @param endIconTint pass null so as not to apply any tint
 **/

@Composable
fun AppArrowItem(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String? = null,
    @DrawableRes startIcon: Int? = null,
    startIconTint: Color? = MaterialTheme.colors.primary,
    @DrawableRes endIcon: Int = R.drawable.ic_arrow_right,
    endIconTint: Color? = MaterialTheme.colors.colorGrey,
    optionTint: Color = MaterialTheme.colors.colorGrey,
    background: Color? = null,
    option: String? = null,
    arrowVisible: Boolean = true,
    dividerVisible: Boolean = true,
    enabled: Boolean = true,
    onClick: (() -> Unit)? = null
) {
    AppListItem(
        modifier = modifier,
        startIcon = startIcon,
        startIconTint = startIconTint,
        background = background,
        dividerVisible = dividerVisible,
        enabled = enabled,
        onClick = onClick,
        title = title,
        subtitle = subtitle,
        endContent = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                option?.let { option ->
                    Text(
                        text = option,
                        style = MaterialTheme.typography.body2,
                        color = optionTint,
                        textAlign = TextAlign.End,
                    )
                }
                if (arrowVisible) {
                    if (endIconTint != null) {
                        Icon(
                            painter = painterResource(id = endIcon),
                            tint = endIconTint,
                            contentDescription = null,
                        )
                    } else {
                        Image(
                            painter = painterResource(id = endIcon),
                            contentDescription = null
                        )
                    }
                }
            }
        }
    )
}

/**
 * @param startIconTint pass null so as not to apply any tint
 * @param endIconTint pass null so as not to apply any tint
 **/

@Composable
fun AppArrowItem(
    modifier: Modifier = Modifier,
    @StringRes title: Int,
    @StringRes subtitle: Int? = null,
    @DrawableRes startIcon: Int? = null,
    startIconTint: Color? = MaterialTheme.colors.primary,
    @DrawableRes endIcon: Int = R.drawable.ic_arrow_right,
    endIconTint: Color? = MaterialTheme.colors.colorGrey,
    optionTint: Color = MaterialTheme.colors.colorGrey,
    background: Color? = null,
    option: String? = null,
    arrowVisible: Boolean = true,
    dividerVisible: Boolean = true,
    enabled: Boolean = true,
    onClick: (() -> Unit)? = null
) {
    AppArrowItem(
        modifier = modifier,
        title = stringResource(id = title),
        subtitle = subtitle?.let { stringResource(id = subtitle) },
        startIcon = startIcon,
        startIconTint = startIconTint,
        endIcon = endIcon,
        endIconTint = endIconTint,
        optionTint = optionTint,
        option = option,
        background = background,
        arrowVisible = arrowVisible,
        dividerVisible = dividerVisible,
        enabled = enabled,
        onClick = onClick
    )
}

@Preview
@Composable
private fun AppArrowItemsPreview() {
    ManagerTheme {
        Surface {
            Column {
                AppArrowItem(title = R.string.about_feedback) {}
                AppArrowItem(
                    title = R.string.app_url_help,
                    startIcon = R.drawable.drawable_ic_logo,
                    enabled = false
                ) {}
                AppArrowItem(
                    title = R.string.toolbar_menu_search_hint,
                    startIcon = R.drawable.drawable_ic_logo
                ) {}
                AppArrowItem(
                    title = R.string.toolbar_menu_search_hint,
                    startIcon = R.drawable.drawable_ic_logo,
                    option = stringResource(id = R.string.app_title)
                ) {}
                AppArrowItem(
                    title = R.string.toolbar_menu_search_hint,
                    startIcon = R.drawable.drawable_ic_logo,
                    option = stringResource(id = R.string.app_title),
                    arrowVisible = false
                ) {}
            }
        }
    }
}

@Preview(uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun AppArrowItemsPreviewDark() {
    AppArrowItemsPreview()
}