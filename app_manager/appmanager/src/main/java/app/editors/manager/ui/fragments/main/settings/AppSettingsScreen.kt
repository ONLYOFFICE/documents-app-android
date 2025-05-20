package app.editors.manager.ui.fragments.main.settings

import android.app.LocaleManager
import android.content.Context
import android.os.Build
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import app.editors.manager.R
import app.editors.manager.app.App
import app.editors.manager.ui.compose.locale.AppLocalePickerScreen
import app.editors.manager.ui.compose.passcode.PasscodeMainScreen
import app.editors.manager.viewModels.main.AppSettingsState
import app.editors.manager.viewModels.main.AppSettingsViewModel
import app.editors.manager.viewModels.main.PasscodeViewModel
import lib.compose.ui.screens.HelpAndFeedbackScreen
import lib.compose.ui.theme.ManagerTheme
import lib.compose.ui.views.AppArrowItem
import lib.compose.ui.views.AppDivider
import lib.compose.ui.views.AppHeaderItem
import lib.compose.ui.views.AppSwitchItem
import lib.toolkit.base.managers.utils.ActivitiesUtils
import lib.toolkit.base.managers.utils.StringUtils
import lib.toolkit.base.managers.utils.UiUtils
import lib.toolkit.base.managers.utils.capitalize


sealed class AppSettingsScreen(val route: String) {
    data object Main : AppSettingsScreen("main")
    data object Theme : AppSettingsScreen("theme")
    data object Passcode : AppSettingsScreen("passcode")
    data object LocalePicker : AppSettingsScreen("locale")
    data object Fonts : AppSettingsScreen("fonts")
    data object HelpAndFeedback : AppSettingsScreen("help-and-feedback")
    data object About : AppSettingsScreen("about")
}

private data class ClearCacheMessage(
    val title: String?,
    val message: String
) {

    companion object {

        fun get(context: Context): ClearCacheMessage {
            val string = context.getString(R.string.dialog_clear_cache)
            return try {
                val (title, message) = string.split("\n")
                ClearCacheMessage(title, message)
            } catch (_: Exception) {
                ClearCacheMessage(null, string)
            }
        }
    }
}

private fun getThemeString(mode: Int) = when (mode) {
    AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM -> R.string.app_settings_follow_the_system
    AppCompatDelegate.MODE_NIGHT_NO -> R.string.app_settings_light_theme
    AppCompatDelegate.MODE_NIGHT_YES -> R.string.app_settings_dark_theme
    else -> null
}

@Composable
fun AppSettingsScreenHost(
    modifier: Modifier = Modifier,
    viewModel: AppSettingsViewModel,
    navController: NavHostController,
    passcodeViewModel: PasscodeViewModel,
    onShowClearMenuItem: (Boolean) -> Unit
) {
    val settingsState = viewModel.settingsState.collectAsState()
    val context = LocalContext.current

    NavHost(navController = navController, startDestination = AppSettingsScreen.Main.route) {
        composable(AppSettingsScreen.Main.route) {
            AppSettingsScreen(
                settingsState = settingsState.value,
                onWifiState = viewModel::setWifiState,
                onScreenOnState = viewModel::setScreenOnState,
                onAnalytics = viewModel::setAnalytic,
                onCacheClear = viewModel::clearCache,
                onThemeClick = { navController.navigate(AppSettingsScreen.Theme.route) },
                onPasscodeClick = { navController.navigate(AppSettingsScreen.Passcode.route) },
                onLocaleClick = { navController.navigate(AppSettingsScreen.LocalePicker.route) },
                onFontsClick = { navController.navigate(AppSettingsScreen.Fonts.route) },
                onAboutClick = { navController.navigate(AppSettingsScreen.About.route) },
                onHelpAndFeedbackClick = { navController.navigate(AppSettingsScreen.HelpAndFeedback.route) }
            )
        }
        composable(AppSettingsScreen.Theme.route) {
            AppSettingsThemeScreen(
                currentTheme = settingsState.value.themeMode,
                onSetTheme = viewModel::setThemeMode
            )
        }
        composable(AppSettingsScreen.Passcode.route) {
            PasscodeMainScreen(
                viewModel = passcodeViewModel,
                enterPasscodeKey = false,
                onBackClick = navController::popBackStack,
                onSuccess = navController::popBackStack
            )
        }
        composable(AppSettingsScreen.LocalePicker.route) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                with(App.getApp().appComponent.appLocaleHelper) {
                    AppLocalePickerScreen(
                        locales = locales,
                        current = currentLocale,
                        onBackListener = navController::popBackStack,
                        onChangeLocale = ::changeLocale
                    )
                }
            }
        }
        composable(AppSettingsScreen.Fonts.route) {
            LaunchedEffect(settingsState.value.fonts.size) {
                onShowClearMenuItem(settingsState.value.fonts.isNotEmpty())
            }

            AppSettingsFontsScreen(
                fonts = settingsState.value.fonts,
            ) { font ->
                UiUtils.showQuestionDialog(
                    context = context,
                    title = context.getString(R.string.dialogs_question_delete_font),
                    description = context.resources.getQuantityString(
                        R.plurals.dialogs_question_message_delete,
                        1
                    ),
                    acceptListener = { viewModel.deleteFont(font) }
                )
            }
        }
        composable(AppSettingsScreen.About.route) {
            AboutScreen(
                onShowBrowser = { url ->
                    ActivitiesUtils.showBrowser(
                        context = context,
                        url = context.getString(url)
                    )
                }
            )
        }
        composable(AppSettingsScreen.HelpAndFeedback.route) {
            HelpAndFeedbackScreen {
                AppArrowItem(
                    title = R.string.whats_new_title,
                    dividerVisible = false,
                    onClick = { } // TODO: add what's new screen navigate
                )
            }
        }
    }
}

@Composable
private fun AppSettingsScreen(
    settingsState: AppSettingsState,
    onThemeClick: () -> Unit,
    onPasscodeClick: () -> Unit,
    onLocaleClick: () -> Unit,
    onWifiState: (Boolean) -> Unit,
    onScreenOnState: (Boolean) -> Unit,
    onAnalytics: (Boolean) -> Unit,
    onCacheClear: () -> Unit,
    onFontsClick: () -> Unit,
    onHelpAndFeedbackClick: () -> Unit,
    onAboutClick: () -> Unit,
) {
    val context = LocalContext.current

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
            AppHeaderItem(title = R.string.setting_title_sreen_on)
            AppSwitchItem(
                title = R.string.setting_screen_on,
                checked = settingsState.keepScreenOn,
                onCheck = onScreenOnState,
                dividerVisible = false
            )
            AppDivider()
            AppHeaderItem(title = R.string.app_settings_security)
            AppArrowItem(
                title = R.string.app_settings_passcode,
                arrowVisible = true,
                dividerVisible = false,
                onClick = onPasscodeClick
            )
            AppDivider()
            AppHeaderItem(title = R.string.settings_title_common)
            AppArrowItem(
                title = R.string.settings_clear_cache,
                option = StringUtils.getFormattedSize(context, settingsState.cache),
                dividerVisible = false,
                enabled = settingsState.cache > 0
            ) {
                with(ClearCacheMessage.get(context)) {
                    UiUtils.showQuestionDialog(
                        context,
                        title = title.orEmpty(),
                        description = message,
                        acceptListener = onCacheClear
                    )
                }
            }
            AppArrowItem(
                title = lib.toolkit.base.R.string.settings_fonts_title,
                dividerVisible = false,
                onClick = onFontsClick,
                option = settingsState.fonts.size.toString()
            )
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                with(LocalContext.current.getSystemService(LocaleManager::class.java)) {
                    val currentAppLocale = applicationLocales.get(0) ?: systemLocales.get(0)
                    AppArrowItem(
                        title = R.string.settings_language,
                        option = currentAppLocale.displayLanguage.capitalize(currentAppLocale),
                        dividerVisible = false,
                        onClick = onLocaleClick
                    )
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
                dividerVisible = false,
                onClick = onAboutClick
            )
            AppArrowItem(
                title = lib.toolkit.base.R.string.help_and_feedback_title,
                arrowVisible = true,
                dividerVisible = false,
                onClick = onHelpAndFeedbackClick
            )
        }
    }
}

@Preview
@Composable
private fun AppSettingsScreenPreview() {
    ManagerTheme {
        AppSettingsScreen(
            settingsState = AppSettingsState(
                cache = 100000L,
                themeMode = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM,
                analytics = false,
                wifi = true,
                passcodeEnabled = true
            ),
            onThemeClick = {},
            onWifiState = {},
            onScreenOnState = {},
            onAnalytics = {},
            onPasscodeClick = {},
            onLocaleClick = {},
            onHelpAndFeedbackClick = {},
            onAboutClick = {},
            onCacheClear = {},
            onFontsClick = {}
        )
    }
}
