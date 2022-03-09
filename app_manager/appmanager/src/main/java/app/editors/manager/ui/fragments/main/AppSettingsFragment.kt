package app.editors.manager.ui.fragments.main

import android.Manifest
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import app.editors.manager.R
import app.editors.manager.app.appComponent
import app.editors.manager.databinding.FragmentAppSettingsLayoutBinding
import app.editors.manager.ui.activities.main.AboutActivity
import app.editors.manager.ui.activities.main.PasscodeActivity
import app.editors.manager.ui.dialogs.AppThemeDialog
import app.editors.manager.ui.fragments.base.BaseAppFragment
import app.editors.manager.viewModels.main.AppSettingsViewModel
import lib.toolkit.base.managers.utils.ActivitiesUtils.showEmail
import lib.toolkit.base.managers.utils.StringUtils
import lib.toolkit.base.managers.utils.UiUtils.getDeviceInfoString
import lib.toolkit.base.ui.dialogs.common.CommonDialog.Dialogs

class AppSettingsFragment : BaseAppFragment(), View.OnClickListener {

    companion object {
        val TAG: String = AppSettingsFragment::class.java.simpleName

        fun newInstance(): AppSettingsFragment {
            return AppSettingsFragment()
        }

        private const val TAG_DIALOG_TRASH = "TAG_DIALOG_TRASH"
        private const val TAG_DIALOG_RATE_FEEDBACK = "TAG_DIALOG_RATE_FEEDBACK"
    }


    private val viewModel by viewModels<AppSettingsViewModel>()
    private var viewBinding: FragmentAppSettingsLayoutBinding? = null

    private val getWritePermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) { result ->
        if (result) {
            viewModel.clearCache()
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        requireContext().appComponent.inject(viewModel)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewBinding = null
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        viewBinding = FragmentAppSettingsLayoutBinding.inflate(inflater, container, false)
        return viewBinding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
        initSettingItems()
        viewModel.getData()
    }

    override fun onResume() {
        super.onResume()
        viewModel.getData()
    }

    private fun init() {
        setActionBarTitle(getString(R.string.settings_item_title))
        viewModel.cacheLiveData.observe(viewLifecycleOwner) { size: Long? ->
            viewBinding?.cacheSizeTextView?.text = StringUtils.getFormattedSize(requireContext(), size ?: -1)
        }
        viewModel.analyticState.observe(viewLifecycleOwner) { isChecked ->
            viewBinding?.analyticSwitch?.isChecked = isChecked
        }
        viewModel.wifiState.observe(viewLifecycleOwner) { isChecked ->
            viewBinding?.wifiSwitch?.isChecked = isChecked
        }
        viewModel.passcodeState.observe(viewLifecycleOwner) { isPasscodeEnabled ->
            //TODO make correct translate
            viewBinding?.passcodeSubTextView?.text = if(!isPasscodeEnabled) getString(R.string.app_settings_enable) else getString(R.string.app_settings_disable)
        }
        viewModel.message.observe(viewLifecycleOwner) { message ->
            showSnackBar(message)
        }
    }

    private fun initSettingItems() {
        viewBinding?.let { binding ->
            binding.clearCacheLayout.setOnClickListener(this)
            binding.settingAboutItem.root.setOnClickListener(this)
            binding.settingHelpItem.root.setOnClickListener(this)
            binding.settingSupportItem.root.setOnClickListener(this)
            binding.wifiSwitch.setOnClickListener(this)
            binding.analyticSwitch.setOnClickListener(this)
            binding.passcodeLayout.setOnClickListener(this)
            binding.settingsAppTheme.root.setOnClickListener(this)

            binding.settingAboutItem.settingIcon.setImageResource(R.drawable.ic_drawer_menu_about)
            binding.settingAboutItem.settingText.text = getString(R.string.about_title)

            binding.settingHelpItem.settingIcon.setImageResource(R.drawable.drawable_ic_drawer_menu_help_fill)
            binding.settingHelpItem.settingIconArrow.setImageResource(R.drawable.ic_open_in_new)
            binding.settingHelpItem.settingText.text = getString(R.string.navigation_drawer_menu_help)

            binding.settingSupportItem.settingIcon.setImageResource(R.drawable.ic_drawer_menu_feedback)
            binding.settingSupportItem.settingIconArrow.setImageResource(R.drawable.ic_open_in_new)
            binding.settingSupportItem.settingText.text = getString(R.string.navigation_drawer_menu_feedback)

            binding.settingsAppTheme.apply {
                settingIcon.isVisible = true
                settingText.text = getString(R.string.app_settings_color_theme)
                settingIconArrow.isVisible = false
            }
        }
    }

    override fun onAcceptClick(dialogs: Dialogs?, value: String?, tag: String?) {
        super.onAcceptClick(dialogs, value, tag)
        if (tag != null) {
            when (tag) {
                TAG_DIALOG_TRASH -> {
                    getWritePermission.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                }
                TAG_DIALOG_RATE_FEEDBACK -> {
                    value?.let { showEmailClientTemplate(it) }
                }
            }
        }
        hideDialog()
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.clearCacheLayout -> {
                showQuestionDialog(
                    requireContext().getString(R.string.dialog_clear_cache),
                    null,
                    getString(R.string.dialogs_common_ok_button),
                    getString(R.string.dialogs_common_cancel_button),
                    TAG_DIALOG_TRASH
                )
            }
            R.id.settingAboutItem -> {
                AboutActivity.show(requireContext())
            }
            R.id.settingHelpItem -> {
                showUrlInBrowser(getString(R.string.app_url_help))
            }
            R.id.settingSupportItem -> {
                showEmailClientTemplate("")
            }
            R.id.wifiSwitch -> {
                viewModel.setWifiState(viewBinding?.wifiSwitch?.isChecked ?: false)
            }
            R.id.analyticSwitch -> {
                viewModel.setAnalytic(viewBinding?.analyticSwitch?.isChecked ?: false)
            }
            R.id.passcodeLayout -> {
                PasscodeActivity.show(requireContext())
            }
            R.id.settingsAppTheme -> {
                AppThemeDialog().show(parentFragmentManager, null)
            }
        }
    }

    private fun showEmailClientTemplate(message: String) {
        showEmail(
            requireContext(),
            getString(R.string.chooser_email_client),
            getString(R.string.app_support_email),
            getString(R.string.about_email_subject),
            message + getDeviceInfoString(requireContext(), false)
        )
    }

}