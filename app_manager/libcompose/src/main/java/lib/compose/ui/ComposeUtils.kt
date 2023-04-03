package lib.compose.ui

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha

fun <T> Modifier.addIfNotNull(obj: T?, modifier: Modifier.(T) -> Modifier) = if (obj != null) modifier(obj) else this

fun Modifier.addIf(condition: Boolean, modifier: Modifier.() -> Modifier): Modifier =
    if (condition) modifier() else this

fun Modifier.visible(visible: Boolean) = addIf(!visible) { alpha(0f) }

fun Modifier.enabled(enabled: Boolean) = addIf(!enabled) { alpha(0.4f) }
