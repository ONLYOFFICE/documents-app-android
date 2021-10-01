package app.editors.manager.compose.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable

private val DarkColorPalette = darkColors(
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

private val LightColorPalette = lightColors(
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

@Composable
fun AppManagerTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    val colors = if (darkTheme) {
        DarkColorPalette
    } else {
        LightColorPalette
    }

    MaterialTheme(
        colors = colors,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}