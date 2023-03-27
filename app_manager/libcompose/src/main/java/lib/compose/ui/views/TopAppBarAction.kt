package lib.compose.ui.views

import androidx.annotation.DrawableRes
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource

@Composable
fun TopAppBarAction(
    @DrawableRes icon: Int,
    tint: Color = MaterialTheme.colors.primary,
    enabled: Boolean,
    contentDescription: String? = null,
    onClick: () -> Unit
) {
    IconButton(
        enabled = enabled,
        onClick = onClick
    ) {
        Icon(
            painter = painterResource(id = icon),
            tint = tint,
            contentDescription = contentDescription
        )
    }
}