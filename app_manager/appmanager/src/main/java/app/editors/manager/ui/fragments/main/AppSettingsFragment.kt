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
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.fragment.app.viewModels
import app.editors.manager.BuildConfig
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import app.editors.manager.R
import app.editors.manager.managers.tools.PreferenceTool
import app.editors.manager.ui.activities.main.AboutActivity
import app.editors.manager.ui.activities.main.AppLocalePickerActivity
import app.editors.manager.ui.activities.main.IMainActivity
import app.editors.manager.ui.activities.main.PasscodeActivity
import app.editors.manager.ui.fragments.base.BaseAppFragment
import app.editors.manager.viewModels.main.AppSettingsState
import app.editors.manager.viewModels.main.AppSettingsViewModel
import app.editors.manager.viewModels.main.AppSettingsViewModelFactory
import kotlinx.coroutines.flow.collectLatest
import lib.compose.ui.theme.ManagerTheme
import lib.compose.ui.theme.Previews
import lib.compose.ui.views.AppArrowItem
import lib.compose.ui.views.AppDivider
import lib.compose.ui.views.AppHeaderItem
import lib.compose.ui.views.AppScaffold
import lib.compose.ui.views.AppSelectItem
import lib.compose.ui.views.AppSwitchItem
import lib.toolkit.base.managers.tools.ResourcesProvider
import lib.toolkit.base.managers.tools.ThemePreferencesTools
import lib.toolkit.base.managers.utils.ActivitiesUtils
import lib.toolkit.base.managers.utils.StringUtils
import lib.toolkit.base.managers.utils.capitalize
import lib.toolkit.base.ui.dialogs.common.CommonDialog.Dialogs

private data class ClearCacheMessage(
    val title: String?,
    val message: String
)

private enum class SCREENS {
    MAIN, THEME
}

class AppSettingsFragment : BaseAppFragment() {

    companion object {
        val TAG: String = AppSettingsFragment::class.java.simpleName

        fun newInstance(): AppSettingsFragment = AppSettingsFragment()

        private const val TAG_DIALOG_CLEAR_CACHE = "TAG_DIALOG_CLEAR_CACHE"
        private const val TAG_DIALOG_RATE_FEEDBACK = "TAG_DIALOG_RATE_FEEDBACK"
    }


    private val viewModel by viewModels<AppSettingsViewModel>() {
        AppSettingsViewModelFactory(
            themePrefs = ThemePreferencesTools(requireContext()),
            resourcesProvider = ResourcesProvider(requireContext()),
            preferenceTool = PreferenceTool(requireContext())
        )
    }

    private val getWritePermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) { result ->
        if (result) {
            viewModel.clearCache()
        }
    }

    private val clearCacheMessage: ClearCacheMessage
        get() {
            val string = requireContext().getString(R.string.dialog_clear_cache)
            return try {
                val (title, message) = string.split("\n")
                ClearCacheMessage(title, message)
            } catch (_: Exception) {
                ClearCacheMessage(null, string)
            }
        }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return ComposeView(requireContext())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        (view as ComposeView).setContent {
            val settingsState by viewModel.settingsState.collectAsState()
            LaunchedEffect(Unit) {
                viewModel.message.collectLatest { message ->
                    showSnackBar(message)
                }
            }

            val navController = rememberNavController()

            ManagerTheme {
                AppScaffold {
                    NavHost(navController = navController, startDestination = SCREENS.MAIN.name) {
                        composable(SCREENS.MAIN.name) {
                            initToolbar(SCREENS.MAIN)
                            SettingsScreen(
                                context = requireContext(),
                                settingsState = settingsState,
                                onThemeClick = { navController.navigate(SCREENS.THEME.name) },
                                onWifiState = viewModel::setWifiState,
                                onAnalytics = viewModel::setAnalytic
                            )
                        }
                        composable(SCREENS.THEME.name) {
                            initToolbar(SCREENS.THEME)
                            ThemeScreen()
                        }
                    }
                }
            }

        }
    }

    override fun onAcceptClick(dialogs: Dialogs?, value: String?, tag: String?) {
        super.onAcceptClick(dialogs, value, tag)
        if (tag != null) {
            when (tag) {
                TAG_DIALOG_CLEAR_CACHE -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        viewModel.clearCache()
                    } else {
                        getWritePermission.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    }
                }

                TAG_DIALOG_RATE_FEEDBACK -> {
                    ActivitiesUtils.sendFeedbackEmail(requireContext(), value.orEmpty())
                }
            }
        }
        hideDialog()
    }

    private fun initToolbar(theme: SCREENS) {
        when (theme) {
            SCREENS.MAIN -> {
                setActionBarTitle(getString(R.string.settings_item_title))
                (activity as? IMainActivity)?.apply {
                    setAppBarStates(false)
                    showNavigationButton(false)
                    showActionButton(false)
                }
            }

            SCREENS.THEME -> {
                setActionBarTitle(getString(R.string.app_settings_color_theme))
                (activity as? IMainActivity)?.apply {
                    showNavigationButton(true)
                }
            }
        }

    }

    @Composable
    private fun SettingsScreen(
        context: Context,
        settingsState: AppSettingsState,
        onThemeClick: () -> Unit,
        onWifiState: (Boolean) -> Unit,
        onAnalytics: (Boolean) -> Unit
    ) {
        Surface(color = MaterialTheme.colors.background, modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                AppHeaderItem(title = R.string.app_settings_analytic_title)
                AppSwitchItem(
                    title = R.string.app_settings_analytic,
                    checked = settingsState.analytics,
                    onCheck = onAnalytics,
                    dividerVisible = false
                )
                AppDivider()
                AppHeaderItem(title = R.string.setting_title_wifi)
                AppSwitchItem(
                    title = R.string.setting_wifi,
                    checked = settingsState.wifi,
                    onCheck = onWifiState,
                    dividerVisible = false
                )
                AppDivider()
                AppHeaderItem(title = R.string.app_settings_security)
                AppArrowItem(
                    title = R.string.app_settings_passcode,
                    arrowVisible = true,
                    dividerVisible = false
                ) {
                    PasscodeActivity.show(context, bundle = null)
                }
                AppDivider()
                AppHeaderItem(title = R.string.settings_title_common)
                AppArrowItem(
                    title = R.string.settings_clear_cache,
                    option = StringUtils.getFormattedSize(context, settingsState.cache),
                    dividerVisible = false
                ) {
                    val message = clearCacheMessage
                    showQuestionDialog(
                        message.title.orEmpty(),
                        message.message,
                        getString(R.string.dialogs_common_ok_button),
                        getString(R.string.dialogs_common_cancel_button),
                        TAG_DIALOG_CLEAR_CACHE
                    )
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && BuildConfig.APPLICATION_ID == "com.onlyoffice.documents") {
                    with(requireContext().getSystemService(LocaleManager::class.java)) {
                        val currentAppLocale = applicationLocales.get(0) ?: systemLocales.get(0)
                        AppArrowItem(
                            title = R.string.settings_language,
                            option = currentAppLocale.displayLanguage.capitalize(currentAppLocale),
                            dividerVisible = false
                        ) {
                            AppLocalePickerActivity.show(requireContext())
                        }
                    }
                }
                AppArrowItem(
                    title = R.string.app_settings_color_theme,
                    option = getThemeString(settingsState.themeMode)?.let { stringResource(id = it) },
                    dividerVisible = false,
                    onClick = onThemeClick
                )
                AppArrowItem(
                    title = R.string.about_title,
                    arrowVisible = true,
                    dividerVisible = false
                ) {
                    AboutActivity.show(context)
                }
                AppArrowItem(title = lib.editors.gbase.R.string.context_settings_main_help, dividerVisible = false) {
                    showUrlInBrowser(getString(R.string.app_url_help))
                }
                AppArrowItem(title = lib.toolkit.base.R.string.about_feedback, dividerVisible = false) {
                    ActivitiesUtils.sendFeedbackEmail(context, "")
                }
            }
        }
    }

    @Composable
    fun ThemeScreen() {
        val state by viewModel.settingsState.collectAsState()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(color = MaterialTheme.colors.background)
        ) {
            AppSelectItem(
                title = stringResource(id = R.string.app_settings_follow_the_system),
                selected = state.themeMode == AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM,
                dividerVisible = false,
                onClick = {
                    viewModel.setThemeMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                }
            )
            AppSelectItem(
                title = stringResource(id = R.string.app_settings_light_theme),
                selected = state.themeMode == AppCompatDelegate.MODE_NIGHT_NO,
                dividerVisible = false,
                onClick = {
                    viewModel.setThemeMode(AppCompatDelegate.MODE_NIGHT_NO)
                }
            )
            AppSelectItem(
                title = stringResource(id = R.string.app_settings_dark_theme),
                selected = state.themeMode == AppCompatDelegate.MODE_NIGHT_YES,
                dividerVisible = false,
                onClick = {
                    viewModel.setThemeMode(AppCompatDelegate.MODE_NIGHT_YES)
                }
            )
        }


    }

    private fun getThemeString(mode: Int) = when (mode) {
        AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM -> R.string.app_settings_follow_the_system
        AppCompatDelegate.MODE_NIGHT_NO -> R.string.app_settings_light_theme
        AppCompatDelegate.MODE_NIGHT_YES -> R.string.app_settings_dark_theme
        else -> null
    }

    @Previews.All
    @Composable
    private fun Preview() {
        ManagerTheme {
            SettingsScreen(
                context = LocalContext.current,
                settingsState = AppSettingsState(
                    cache = 100000L,
                    themeMode = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM,
                    analytics = false,
                    wifi = true,
                    passcode = true
                ),
                onThemeClick = {},
                onWifiState = {},
                onAnalytics = {}
            )
        }
    }
}