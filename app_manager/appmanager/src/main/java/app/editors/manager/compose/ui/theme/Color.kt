package app.editors.manager.compose.ui.theme

import androidx.compose.material.Colors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource

val ColorPrimary = Color(0xFF1A73E9)
val ColorOnPrimary = Color.White
val ColorSecondary = Color(0xFFED7309)
val ColorOnSecondary = Color.White
val ColorBackground = Color.White
val ColorOnBackground = Color.Black
val ColorSurface = Color.White
val ColorOnSurface = Color.Black
val ColorError = Color(0xFFFF0C3E)
val ColorOnError = Color.Black

val NightColorPrimary = Color(0xFF3E9CF0)
val NightColorOnPrimary = Color.White
val NightColorSecondary = Color(0xFFFFAF49)
val NightColorOnSecondary = Color.Black
val NightColorBackground = Color(0xFF121212)
val NightColorOnBackground = Color(0xDEFFFFFF)
val NightColorOnSurface = Color(0xDEFFFFFF)
val NightColorSurface = Color(0xFF252525)
val NightColorError = Color(0xFFFF5679)
val NightColorOnError = Color.Black


val Colors.colorAppBar: Color
    @Composable
    get() = if (isLight) {
        Color.White
    } else {
        colorResource(id = lib.toolkit.base.R.color.colorAppBar)
    }

