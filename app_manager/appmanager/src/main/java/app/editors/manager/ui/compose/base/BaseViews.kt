package app.editors.manager.ui.compose.base

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import app.editors.manager.compose.ui.theme.colorAppBar

@Composable
fun Spacer(size: Dp) {
    Spacer(modifier = Modifier.size(size))
}


@Composable
fun CustomAppBar(@StringRes title: Int, @DrawableRes icon: Int, click: () -> Unit) {
    TopAppBar(
        title = { Text(text = stringResource(id = title)) },
        navigationIcon = {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxWidth()) {
                Icon(
                    painter = painterResource(id = icon),
                    contentDescription = "Back",
                    tint = MaterialTheme.colors.primary,
                    modifier = Modifier.clickableRipple { click() }
                )
            }
        },
        backgroundColor = MaterialTheme.colors.colorAppBar
    )
}

fun Modifier.clickableRipple(onClick: () -> Unit): Modifier = composed {
    clickable(
        interactionSource = remember { MutableInteractionSource() },
        indication = rememberRipple(bounded = false),
        onClick = {
            onClick()
        }
    )
}