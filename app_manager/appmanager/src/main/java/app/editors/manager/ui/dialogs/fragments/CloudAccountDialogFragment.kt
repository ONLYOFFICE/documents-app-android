package app.editors.manager.ui.dialogs.fragments

import android.os.Bundle
import android.view.View
import app.editors.manager.R
import app.editors.manager.mvp.presenters.main.CloudAccountPresenter
import app.editors.manager.ui.fragments.main.CloudAccountFragment

class CloudAccountDialogFragment : BaseDialogFragment() {

    companion object {
        val TAG: String = CloudAccountDialogFragment::class.java.simpleName

        fun newInstance(isSwitch: Boolean = false): CloudAccountDialogFragment {
            return CloudAccountDialogFragment().apply {
                arguments = Bundle(1).apply {
                    putBoolean(CloudAccountPresenter.KEY_SWITCH, isSwitch)
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initToolbar(R.menu.cloud_settings_menu)
        showFragment(CloudAccountFragment.newInstance(arguments?.getBoolean(CloudAccountPresenter.KEY_SWITCH) ?: false))
    }

    override fun onBackPressed(): Boolean {
        return if ((childFragmentManager.findFragmentByTag(CloudAccountFragment.TAG)
                    as? CloudAccountFragment)?.onBackPressed() == true
        ) true
        else super.onBackPressed()
    }
}