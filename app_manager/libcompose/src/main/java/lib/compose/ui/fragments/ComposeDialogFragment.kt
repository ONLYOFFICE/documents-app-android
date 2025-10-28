package lib.compose.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.ComposeView
import lib.compose.ui.theme.LocalUseTabletPadding
import lib.toolkit.base.ui.fragments.base.BaseDialogFragment

abstract class ComposeDialogFragment : BaseDialogFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return ComposeView(requireContext())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        (view as? ComposeView)?.setContent {
            CompositionLocalProvider(LocalUseTabletPadding provides false) {
                BackHandler(onBack = ::dismiss)
                Content()
            }
        }
    }

    @Composable
    abstract fun Content()
}