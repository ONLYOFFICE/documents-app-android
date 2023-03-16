package app.editors.manager.ui.fragments.main

import android.Manifest
import android.app.LocaleManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import app.editors.manager.R
import app.editors.manager.app.appComponent
import app.editors.manager.databinding.FragmentAppSettingsLayoutBinding
import app.editors.manager.ui.activities.main.*
import app.editors.manager.ui.dialogs.AppThemeDialog
import app.editors.manager.ui.fragments.base.BaseAppFragment
import app.editors.manager.viewModels.main.AppSettingsViewModel
import lib.toolkit.base.managers.utils.ActivitiesUtils
import lib.toolkit.base.managers.utils.StringUtils
import lib.toolkit.base.managers.utils.capitalize
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
    private var activity: IMainActivity? = null

    private val getWritePermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) { result ->
        if (result) {
            viewModel.clearCache()
        }
    }
    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            activity = context as IMainActivity
            requireContext().appComponent.inject(viewModel)
        } catch (e: ClassCastException) {
            throw RuntimeException(
                DocsOnDeviceFragment::class.java.simpleName + " - must implement - " +
                        IMainActivity::class.java.simpleName
            )
        }
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
        initToolbar()
        initSettingItems()
        viewModel.getData()
    }

    override fun onResume() {
        super.onResume()
        viewModel.getData()
    }

    private fun init() {
        viewModel.cacheLiveData.observe(viewLifecycleOwner) { size: Long? ->
            viewBinding?.clearCache?.optionText?.text = StringUtils.getFormattedSize(requireContext(), size ?: -1)
        }
        viewModel.analyticState.observe(viewLifecycleOwner) { isChecked ->
            viewBinding?.analyticSwitch?.isChecked = isChecked
        }
        viewModel.wifiState.observe(viewLifecycleOwner) { isChecked ->
            viewBinding?.wifiSwitch?.isChecked = isChecked
        }
        viewModel.passcodeState.observe(viewLifecycleOwner) { isPasscodeEnabled ->
            viewBinding?.passcodeImageView?.setImageDrawable(AppCompatResources.getDrawable(requireContext(), if (isPasscodeEnabled) R.drawable.ic_passcode_enabled else R.drawable.ic_passcode_disabled))
            viewBinding?.passcodeSubTextView?.text = if(isPasscodeEnabled) getString(R.string.app_settings_enabled) else getString(R.string.app_settings_disabled)
        }
        viewModel.message.observe(viewLifecycleOwner) { message ->
            showSnackBar(message)
        }
    }

    private fun initToolbar() {
        setActionBarTitle(getString(R.string.settings_item_title))
        activity?.apply {
            setAppBarStates(false)
            showNavigationButton(false)
            showActionButton(false)
        }
    }

    private fun initSettingItems() {
        viewBinding?.let { binding ->
            binding.settingAboutItem.root.setOnClickListener(this)
            binding.settingHelpItem.root.setOnClickListener(this)
            binding.settingSupportItem.root.setOnClickListener(this)
            binding.wifiSwitch.setOnClickListener(this)
            binding.analyticSwitch.setOnClickListener(this)
            binding.passcodeLayout.setOnClickListener(this)
            binding.settingsAppTheme.root.setOnClickListener(this)

            with(binding.clearCache) {
                settingIcon.isVisible = false
                settingIconArrow.isVisible = false
                settingText.setText(R.string.settings_clear_cache)
                root.setOnClickListener(this@AppSettingsFragment)
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val currentAppLocale = with(requireContext().getSystemService(LocaleManager::class.java)) {
                    applicationLocales.get(0) ?: systemLocales.get(0)
                }

                with(binding.appLocale) {
                    root.isVisible = true
                    settingIcon.isVisible = false
                    settingText.setText(R.string.settings_language)
                    optionText.text = currentAppLocale.displayLanguage.capitalize(currentAppLocale)
                    root.setOnClickListener(this@AppSettingsFragment)
                }
            }

            binding.settingAboutItem.settingIcon.setImageResource(R.drawable.ic_drawer_menu_about)
            binding.settingAboutItem.settingText.text = getString(R.string.about_title)

            binding.settingHelpItem.settingIcon.setImageResource(R.drawable.drawable_ic_drawer_menu_help_fill)
            binding.settingHelpItem.settingIconArrow.setImageResource(R.drawable.ic_open_in_new)
            binding.settingHelpItem.settingText.text = getString(R.string.navigation_drawer_menu_help)

            binding.settingSupportItem.settingIcon.setImageResource(R.drawable.ic_drawer_menu_feedback)
            binding.settingSupportItem.settingIconArrow.setImageResource(R.drawable.ic_open_in_new)
            binding.settingSupportItem.settingText.text = getString(lib.toolkit.base.R.string.about_feedback)

            binding.settingsAppTheme.apply {
                settingIcon.isVisible = true
                settingText.text = getString(R.string.app_settings_color_theme)
                settingIconArrow.isVisible = false
                settingIcon.isVisible = false
            }
        }
    }

    override fun onAcceptClick(dialogs: Dialogs?, value: String?, tag: String?) {
        super.onAcceptClick(dialogs, value, tag)
        if (tag != null) {
            when (tag) {
                TAG_DIALOG_TRASH -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        viewModel.clearCache()
                    } else {
                        getWritePermission.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    }
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
            R.id.clearCache -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    AppLocaleConfirmationActivity.show(requireContext())
                }
            }
            R.id.appLocale -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    AppLocalePickerActivity.show(requireContext())
                }
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
                PasscodeActivity.show(requireContext(), bundle = null)
            }
            R.id.settingsAppTheme -> {
                AppThemeDialog().show(parentFragmentManager, null)
            }
        }
    }

    private fun showEmailClientTemplate(message: String) {
        ActivitiesUtils.sendFeedbackEmail(requireContext(), message)
    }

}