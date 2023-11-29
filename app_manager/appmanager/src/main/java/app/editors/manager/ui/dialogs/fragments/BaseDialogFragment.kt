package app.editors.manager.ui.dialogs.fragments

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import app.editors.manager.R
import app.editors.manager.databinding.FragmentDialogBaseLayoutBinding
import lib.toolkit.base.managers.utils.FragmentUtils
import lib.toolkit.base.managers.utils.UiUtils
import lib.toolkit.base.ui.activities.base.BaseActivity

interface IBaseDialogFragment {
    fun setToolbarTitle(title: String)
    fun setToolbarNavigationIcon(isClose: Boolean)
    fun setToolbarButtonTitle(title: String)
    fun setToolbarButtonEnabled(isEnabled: Boolean)
    fun setToolbarButtonVisible(isVisible: Boolean)
    fun setToolbarButtonClickListener(listener: () -> Unit)
    fun setOnMenuItemClickListener(listener: Toolbar.OnMenuItemClickListener)
    fun getMenu(): Menu
    fun dismiss()
}

abstract class BaseDialogFragment : DialogFragment(), BaseActivity.OnBackPressFragment,
    IBaseDialogFragment {

    protected var viewBinding: FragmentDialogBaseLayoutBinding? = null

    override fun onStart() {
        super.onStart()
        if (UiUtils.isTablet(requireContext())) {
            setDialogSize()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewBinding = FragmentDialogBaseLayoutBinding.inflate(inflater)
        return viewBinding?.root
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return object : Dialog(requireContext(), theme) {
            override fun onBackPressed() {
                this@BaseDialogFragment.onBackPressed()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewBinding = null
    }

    override fun onBackPressed(): Boolean {
        if (childFragmentManager.backStackEntryCount > 0) {
            childFragmentManager.popBackStack()
        } else {
            dismiss()
        }
        return true
    }

    override fun getMenu(): Menu {
        return checkNotNull(viewBinding?.appToolbar?.menu)
    }

    override fun dismiss() {
        dialog?.dismiss()
    }

    override fun setToolbarTitle(title: String) {
        viewBinding?.appToolbar?.title = title
    }

    override fun setToolbarNavigationIcon(isClose: Boolean) {
        if (isClose) {
            viewBinding?.appToolbar?.setNavigationIcon(lib.toolkit.base.R.drawable.ic_close)
        } else {
            viewBinding?.appToolbar?.setNavigationIcon(lib.toolkit.base.R.drawable.ic_back)
        }
    }

    override fun setToolbarButtonEnabled(isEnabled: Boolean) {
        viewBinding?.toolbarButton?.isEnabled = isEnabled
    }

    override fun setToolbarButtonVisible(isVisible: Boolean) {
        viewBinding?.toolbarButton?.isVisible = isVisible
    }

    override fun setToolbarButtonTitle(title: String) {
        viewBinding?.toolbarButton?.text = title
    }

    override fun setToolbarButtonClickListener(listener: () -> Unit) {
        viewBinding?.toolbarButton?.setOnClickListener { listener() }
    }

    override fun setOnMenuItemClickListener(listener: Toolbar.OnMenuItemClickListener) {
        viewBinding?.appToolbar?.setOnMenuItemClickListener(listener)
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

    protected fun initToolbar(menu: Int) {
        viewBinding?.appToolbar?.let { toolbar ->
            toolbar.inflateMenu(menu)
            toolbar.setNavigationIcon(lib.toolkit.base.R.drawable.ic_close)
            toolbar.setNavigationOnClickListener { onBackPressed() }
            toolbar.setNavigationIconTint(requireContext().getColor(lib.toolkit.base.R.color.colorPrimary))
        }
    }

    protected fun showFragment(fragment: Fragment) {
        FragmentUtils.showFragment(
            childFragmentManager,
            fragment,
            R.id.frame_container
        )
    }
}