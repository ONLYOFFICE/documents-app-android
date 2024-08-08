package lib.compose.ui.utils

import androidx.lifecycle.Lifecycle
import androidx.navigation.NavController

fun NavController.popBackStackWhenResumed(): Boolean {
    return if (currentBackStackEntry?.lifecycle?.currentState == Lifecycle.State.RESUMED) {
        popBackStack()
    } else {
        false
    }
}