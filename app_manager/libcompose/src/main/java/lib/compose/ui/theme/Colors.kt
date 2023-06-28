package lib.compose.ui.theme

import androidx.compose.material.Colors
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import lib.compose.ui.R

val ColorPrimary = Color(0xFF3880BE)
val ColorOnPrimary = Color.White
val ColorSecondary = Color(0xFFF77F00)
val ColorOnSecondary = Color.White
val ColorBackground = Color.White
val ColorOnBackground = Color.Black
val ColorSurface = Color.White
val ColorOnSurface = Color.Black
val ColorError = Color(0xFFED0A34)
val ColorOnError = Color.White

val NightColorPrimary = Color(0xFF3E9CF0)
val NightColorOnPrimary = Color.White
val NightColorSecondary = Color(0xFFFFB05C)
val NightColorOnSecondary = Color.White
val NightColorBackground = Color(0xFF232323)
val NightColorOnBackground = Color(0xDEFFFFFF)
val NightColorSurface = Color(0xFF333333)
val NightColorOnSurface = Color(0xDEFFFFFF)
val NightColorError = Color(0xFFEA5353)
val NightColorOnError = Color.Black

val Colors.colorTextPrimary: Color
    @Composable
    get() = MaterialTheme.colors.onSurface

val Colors.colorTextSecondary: Color
    @Composable
    get() = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)

val Colors.colorTextTertiary: Color
    @Composable
    get() = MaterialTheme.colors.onSurface.copy(alpha = 0.4f)

val Colors.colorButtonBackground: Color
    @Composable
    get() = colorResource(id = lib.toolkit.base.R.color.colorButtonBackground)

val Colors.colorTopAppBar: Color
    @Composable
    get() = colorResource(id = lib.toolkit.base.R.color.colorAppBar)