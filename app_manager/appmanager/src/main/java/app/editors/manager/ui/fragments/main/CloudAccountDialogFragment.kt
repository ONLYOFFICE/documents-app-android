package app.editors.manager.ui.fragments.main

import android.app.Dialog
import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.DialogFragment
import app.editors.manager.R
import app.editors.manager.databinding.CloudsAccountsDialogLayoutBinding
import lib.toolkit.base.managers.utils.FragmentUtils
import lib.toolkit.base.ui.activities.base.BaseActivity

interface ICloudAccountDialogFragment {
    var isSelectMode: Boolean
    fun setToolbarTitle(title: String)
    fun setToolbarNavigationIcon(isClose: Boolean)
    fun setOnMenuItemClickListener(listener: Toolbar.OnMenuItemClickListener)
    fun getMenu(): Menu
}

class CloudAccountDialogFragment : DialogFragment(), BaseActivity.OnBackPressFragment, ICloudAccountDialogFragment {

    private var viewBinding: CloudsAccountsDialogLayoutBinding? = null

    override var isSelectMode: Boolean = false

    companion object {
        val TAG: String = CloudAccountFragment::class.java.simpleName
        const val REQUEST_LOGOUT = "request_logout"

        fun newInstance(): CloudAccountDialogFragment {
            return CloudAccountDialogFragment()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewBinding = CloudsAccountsDialogLayoutBinding.inflate(inflater)
        viewBinding?.appToolbar?.inflateMenu(R.menu.cloud_settings_menu)
        return viewBinding?.root
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return object : Dialog(requireContext(), theme) {
            override fun onBackPressed() {
                this@CloudAccountDialogFragment.onBackPressed()
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initToolbar()
        showFragment()
    }

    private fun showFragment() {
        FragmentUtils.showFragment(
            childFragmentManager,
            CloudAccountFragment.newInstance(),
            R.id.frame_container
        )
    }

    private fun initToolbar() {
        viewBinding?.appToolbar?.let { toolbar ->
            toolbar.title = getString(R.string.cloud_accounts_title)
            toolbar.setNavigationIcon(R.drawable.ic_toolbar_close)
            toolbar.setNavigationOnClickListener { onBackPressed() }
            toolbar.setNavigationIconTint(requireContext().getColor(lib.toolkit.base.R.color.colorPrimary))
        }
    }

    override fun onBackPressed(): Boolean {
        if (!isSelectMode) {
            if (childFragmentManager.backStackEntryCount > 0) {
                childFragmentManager.popBackStack()
            } else {
                dialog?.dismiss()
            }
        } else {
            (childFragmentManager.findFragmentByTag(CloudAccountFragment.TAG)
                    as? CloudAccountFragment)?.onDefaultMode()
        }
        return true
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewBinding = null
    }

    override fun setToolbarTitle(title: String) {
        viewBinding?.appToolbar?.title = title
    }

    override fun setToolbarNavigationIcon(isClose: Boolean) {
        if (isClose) {
            viewBinding?.appToolbar?.setNavigationIcon(R.drawable.ic_toolbar_close)
        } else {
            viewBinding?.appToolbar?.setNavigationIcon(R.drawable.ic_toolbar_back)
        }
    }

    override fun getMenu(): Menu {
        return checkNotNull(viewBinding?.appToolbar?.menu)
    }

    override fun setOnMenuItemClickListener(listener: Toolbar.OnMenuItemClickListener) {
        viewBinding?.appToolbar?.setOnMenuItemClickListener(listener)
    }

}