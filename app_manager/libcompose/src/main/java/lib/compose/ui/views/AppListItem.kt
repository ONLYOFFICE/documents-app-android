package lib.compose.ui.views

import android.content.res.Configuration
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import lib.compose.ui.addIfNotNull
import lib.compose.ui.enabled
import lib.compose.ui.theme.ManagerTheme
import lib.toolkit.base.R

@Composable
internal fun AppListItem(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String? = null,
    background: Color? = null,
    @DrawableRes startIcon: Int? = null,
    startIconTint: Color? = MaterialTheme.colors.primary,
    endContent: @Composable () -> Unit = {},
    paddingEnd: Dp = 16.dp,
    dividerVisible: Boolean = true,
    enabled: Boolean = true,
    onClick: (() -> Unit)? = null
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
            .addIfNotNull(onClick) { clickable(enabled = enabled, onClick = it) }
            .addIfNotNull(background) { background(it) },
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .padding(start = 16.dp, end = paddingEnd)
                .weight(1f),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                startIcon?.let {
                    if (startIconTint != null) {
                        Icon(
                            modifier = Modifier
                                .padding(end = 16.dp)
                                .size(24.dp)
                                .enabled(enabled),
                            painter = painterResource(id = startIcon),
                            tint = startIconTint,
                            contentDescription = null
                        )
                    } else {
                        Image(
                            modifier = Modifier
                                .padding(end = 16.dp)
                                .size(24.dp)
                                .enabled(enabled),
                            painter = painterResource(id = startIcon),
                            contentDescription = null
                        )
                    }
                }
                Column(modifier = Modifier.enabled(enabled)) {
                    Text(
                        text = title,
                        color = MaterialTheme.colors.onSurface,
                        style = MaterialTheme.typography.body1,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    subtitle?.let {
                        Text(
                            text = subtitle,
                            color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f),
                            style = MaterialTheme.typography.subtitle2
                        )
                    }
                }
            }
            endContent()
        }
        if (dividerVisible) {
            AppDivider(
                modifier = Modifier.padding(
                    start = startIcon?.let { 16.dp + 24.dp + 16.dp } ?: 16.dp
                )
            )
        }
    }
}

@Preview(widthDp = 360, heightDp = 640)
@Composable
fun AppListItemsInteractivePreview() {
    var state by remember { mutableStateOf(false) }
    var stepperState by remember { mutableStateOf(0) }

    ManagerTheme {
        AppScaffold(topBar = {
            AppTopBar(title = R.string.app_title, actions = {
                TopAppBarAction(icon = R.drawable.drawable_ic_logo) { }
                TopAppBarAction(icon = R.drawable.drawable_ic_logo, enabled = false) { }
            }) { }
        }) {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                AppHeaderItem(title = "Arrow item")
                AppArrowItem(
                    title = R.string.app_title,
                    startIcon = R.drawable.drawable_ic_logo,
                )
                AppArrowItem(
                    title = R.string.app_title,
                    subtitle = R.string.app_title,
                    option = stringResource(id = R.string.app_title),
                    startIcon = R.drawable.drawable_ic_logo,
                    enabled = false
                )
                AppHeaderItem(title = "Select item")
                AppSelectItem(title = R.string.app_title, selected = state) {
                    state = true
                }
                AppSelectItem(title = R.string.app_title, selected = !state) {
                    state = false
                }
                AppHeaderItem(title = "Switch item")
                AppSwitchItem(
                    title = R.string.app_title,
                    checked = !state,
                    startIcon = R.drawable.drawable_ic_logo
                ) {
                    state = !state
                }
                AppHeaderItem(title = "Radio item")
                AppRadioItem(title = R.string.app_title, checked = state) {
                    state = true
                }
                AppRadioItem(title = R.string.app_title, checked = !state) {
                    state = false
                }
                AppHeaderItem(title = "Stepper item")
                AppStepperItem(
                    title = R.string.app_title,
                    value = stepperState.toString(),
                    onDownClick = {
                        stepperState--
                    },
                    onUpClick = {
                        stepperState++
                    }
                )
                AppHeaderItem(title = "Drag item")
                AppDragItem(title = R.string.app_title) {}
            }
        }
    }
}

@Preview(widthDp = 360, heightDp = 640, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun AppListItemsInteractivePreviewDark() {
    AppListItemsInteractivePreview()
}