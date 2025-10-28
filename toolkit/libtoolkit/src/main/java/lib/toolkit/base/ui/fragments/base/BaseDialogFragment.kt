package lib.toolkit.base.ui.fragments.base

import android.os.Bundle
import androidx.fragment.app.DialogFragment
import lib.toolkit.base.R
import lib.toolkit.base.managers.utils.UiUtils


abstract class BaseDialogFragment : DialogFragment() {

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

    private fun setDialogSize() {
        val width = resources.getDimension(R.dimen.accounts_dialog_fragment_width)
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
}