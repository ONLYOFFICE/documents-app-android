package app.editors.manager.ui.compose.base

import android.util.LayoutDirection
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.core.text.layoutDirection
import app.editors.manager.compose.ui.theme.colorAppBar
import java.util.*

@Composable
fun Spacer(size: Dp) {
    Spacer(modifier = Modifier.size(size))
}

@Composable
fun CustomAppBar(@StringRes title: Int, @DrawableRes icon: Int, onClick: () -> Unit) {
    TopAppBar(
        title = { Text(text = stringResource(id = title)) },
        navigationIcon = {
            IconButton(onClick = onClick, modifier = Modifier.mirror()) {
                Icon(
                    painter = painterResource(id = icon),
                    contentDescription = "Back",
                    tint = MaterialTheme.colors.primary,
                )
            }
        },
        backgroundColor = MaterialTheme.colors.colorAppBar
    )
}

fun Modifier.mirror(): Modifier {
    return if (Locale.getDefault().layoutDirection == LayoutDirection.RTL)
        this.scale(scaleX = -1f, scaleY = 1f)
    else
        this
}