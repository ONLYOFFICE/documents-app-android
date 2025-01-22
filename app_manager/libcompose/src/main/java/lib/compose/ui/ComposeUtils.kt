@file:SuppressLint("ComposableModifierFactory")

package lib.compose.ui

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
fun TouchDisable(
    modifier: Modifier = Modifier,
    disableTouch: Boolean,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier.enabled(!disableTouch)
    ) {
        content()
        Box(
            modifier = Modifier.matchParentSize().then(
                if (disableTouch) {
                    Modifier.clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {}
                    )
                } else {
                    Modifier
                }
            )
        )
    }
}

@Composable
fun LockScreenOrientation(orientation: Int) {
    val context = LocalContext.current
    if (!UiUtils.isTablet(context)) {
        DisposableEffect(orientation) {
            val activity = context.findActivity() ?: return@DisposableEffect onDispose {}
            val originalOrientation = activity.requestedOrientation
            activity.requestedOrientation = orientation
            onDispose { activity.requestedOrientation = originalOrientation }
        }
    }
}

private fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

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