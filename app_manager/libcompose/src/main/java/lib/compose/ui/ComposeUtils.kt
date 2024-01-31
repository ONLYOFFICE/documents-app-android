@file:SuppressLint("ComposableModifierFactory")

package lib.compose.ui

import android.annotation.SuppressLint
import android.app.Dialog
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import lib.toolkit.base.managers.utils.UiUtils

@Composable
fun <T> Modifier.addIfNotNull(obj: T?, modifier: @Composable Modifier.(T) -> Modifier) =
    if (obj != null) modifier(obj) else this

@Composable
fun Modifier.addIf(condition: Boolean, modifier: @Composable Modifier.() -> Modifier): Modifier =
    if (condition) modifier() else this

@Composable
fun Modifier.visible(visible: Boolean) = addIf(!visible) { alpha(0f) }

@Composable
fun Modifier.enabled(enabled: Boolean) = addIf(!enabled) { alpha(0.4f) }

@Composable
@SuppressLint("ComposableNaming")
fun rememberWaitingDialog(title: Int, onCancel: () -> Unit): Dialog {
    val context = LocalContext.current
    val wrapper = remember {
        UiUtils.getWaitingDialog(
            context = context,
            isCircle = true,
            title = context.getString(title),
            cancelListener = onCancel
        )
    }
    return wrapper
}