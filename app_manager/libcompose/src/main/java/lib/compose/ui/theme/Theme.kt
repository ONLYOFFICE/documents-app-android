package lib.compose.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val ManagerNightColors by lazy {
    darkColors(
        primary = NightColorPrimary,
        secondary = NightColorSecondary,
        onSecondary = NightColorOnSecondary,
        onPrimary = NightColorOnPrimary,
        background = NightColorBackground,
        onBackground = NightColorOnBackground,
        surface = NightColorSurface,
        onSurface = NightColorOnSurface,
        error = NightColorError,
        onError = NightColorOnError
    )
}

val ManagerLightColors by lazy {
    lightColors(
        primary = ColorPrimary,
        secondary = ColorSecondary,
        onSecondary = ColorOnSecondary,
        onPrimary = ColorOnPrimary,
        background = ColorBackground,
        onBackground = ColorOnBackground,
        surface = ColorSurface,
        onSurface = ColorOnSurface,
        error = ColorError,
        onError = ColorOnError
    )
}

@Composable
fun ManagerTheme(
    primaryColor: Color? = null,
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (!darkTheme) {
        ManagerLightColors
    } else {
        ManagerNightColors
    }

    MaterialTheme(
        colors = primaryColor?.let { colors.copy(primary = it) } ?: colors,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}