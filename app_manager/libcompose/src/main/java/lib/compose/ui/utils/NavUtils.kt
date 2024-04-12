package lib.compose.ui.utils

import androidx.lifecycle.Lifecycle
import androidx.navigation.NavController

fun NavController.popBackStackWhenResumed() {
    if (currentBackStackEntry?.lifecycle?.currentState == Lifecycle.State.RESUMED) {
        popBackStack()
    }
}