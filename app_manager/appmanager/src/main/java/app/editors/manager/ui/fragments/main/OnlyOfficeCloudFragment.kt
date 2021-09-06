package app.editors.manager.ui.fragments.main

import android.content.Context
import android.os.Bundle
import android.view.*
import app.editors.manager.R
import app.editors.manager.databinding.FragmentCloudLayoutBinding
import app.editors.manager.ui.activities.login.PortalsActivity
import app.editors.manager.ui.activities.main.CloudsActivity
import app.editors.manager.ui.activities.main.IMainActivity
import app.editors.manager.ui.activities.main.SettingsActivity
import app.editors.manager.ui.fragments.base.BaseAppFragment

class OnlyOfficeCloudFragment : BaseAppFragment() {

    private var viewBinding: FragmentCloudLayoutBinding? = null
    private var mainActivity: IMainActivity? = null
    private var isAccounts = false

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is IMainActivity) {
            mainActivity = context
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mainActivity = null
        viewBinding = null
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewBinding = FragmentCloudLayoutBinding.inflate(layoutInflater, container, false)
        return viewBinding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        checkArguments()
        init()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.cloud_settings_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.settingsItem) {
            SettingsActivity.show(requireContext())
        }
        return true
    }

    private fun checkArguments() {
        if (arguments?.containsKey(KEY_PROFILE) == true) {
            arguments?.getBoolean(KEY_PROFILE)?.let {
                isAccounts = it
                setHasOptionsMenu(isAccounts)
            }
        }
    }

    private fun init() {
        setActionBarTitle(getString(if (isAccounts)
            R.string.cloud_accounts_title else R.string.fragment_clouds_title))
        mainActivity?.showActionButton(false)
        initListeners()
    }

    private fun initListeners() {
        viewBinding?.let {
            it.startButton.setOnClickListener {
                PortalsActivity.show(requireActivity())
            }
            it.otherStorageButton.setOnClickListener {
                CloudsActivity.show(requireContext())
            }
        }
    }

    companion object {
        val TAG = OnlyOfficeCloudFragment::class.java.simpleName
        private const val KEY_PROFILE = "KEY_PROFILE"

        fun newInstance(isProfile: Boolean): OnlyOfficeCloudFragment =
            OnlyOfficeCloudFragment().apply {
                arguments = Bundle(1).apply {
                    putBoolean(KEY_PROFILE, isProfile)
                }
            }
    }
}