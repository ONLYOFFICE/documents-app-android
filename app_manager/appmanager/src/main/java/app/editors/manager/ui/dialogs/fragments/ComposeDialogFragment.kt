package app.editors.manager.ui.dialogs.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.DialogFragment
import app.editors.manager.R
import lib.toolkit.base.managers.utils.UiUtils

abstract class ComposeDialogFragment : DialogFragment() {

    override fun onStart() {
        super.onStart()
        if (UiUtils.isTablet(requireContext())) {
            setDialogSize()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val styleRes = if (!UiUtils.isTablet(requireContext())) {
            R.style.FullScreenDialog
        } else {
            R.style.TabletDialog
        }
        setStyle(STYLE_NORMAL, styleRes)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return ComposeView(requireContext())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        (view as? ComposeView)?.setContent {
            BackHandler {
                dismiss()
            }
            Content()
        }
    }

    private fun setDialogSize() {
        val width = resources.getDimension(lib.toolkit.base.R.dimen.accounts_dialog_fragment_width)
        val height = if (UiUtils.isLandscape(requireContext())) {
            resources.displayMetrics.heightPixels / 1.2
        } else {
            width * 1.3
        }
        dialog?.window?.setLayout(width.toInt(), height.toInt())
    }

    protected fun showSnackbar(message: String) {
        UiUtils.getSnackBar(requireView()).setText(message).show()
    }

    @Composable
    abstract fun Content()
}