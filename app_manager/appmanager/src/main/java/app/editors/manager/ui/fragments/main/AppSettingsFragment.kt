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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.fragment.app.viewModels
import app.editors.manager.R
import app.editors.manager.app.appComponent
import app.editors.manager.databinding.FragmentAppSettingsLayoutBinding
import app.editors.manager.ui.activities.main.*
import app.editors.manager.ui.activities.main.AboutActivity
import app.editors.manager.ui.activities.main.IMainActivity
import app.editors.manager.ui.activities.main.PasscodeActivity
import app.editors.manager.ui.dialogs.AppThemeDialog
import app.editors.manager.ui.fragments.base.BaseAppFragment
import app.editors.manager.viewModels.main.AppSettingsState
import app.editors.manager.viewModels.main.AppSettingsViewModel
import kotlinx.coroutines.flow.collectLatest
import lib.compose.ui.theme.ManagerTheme
import lib.compose.ui.views.AppArrowItem
import lib.compose.ui.views.AppDivider
import lib.compose.ui.views.AppHeaderItem
import lib.compose.ui.views.AppSwitchItem
import lib.toolkit.base.managers.tools.ThemePreferencesTools
import lib.toolkit.base.managers.utils.ActivitiesUtils
import lib.toolkit.base.managers.utils.StringUtils
import lib.toolkit.base.ui.activities.base.BaseActivity
import lib.toolkit.base.managers.utils.capitalize
import lib.toolkit.base.ui.dialogs.common.CommonDialog.Dialogs

private data class ClearCacheMessage(
    val title: String?,
    val message: String
)

class AppSettingsFragment : BaseAppFragment() {

    companion object {
        val TAG: String = AppSettingsFragment::class.java.simpleName

        fun newInstance(): AppSettingsFragment = AppSettingsFragment()

        private const val TAG_DIALOG_CLEAR_CACHE = "TAG_DIALOG_CLEAR_CACHE"
        private const val TAG_DIALOG_RATE_FEEDBACK = "TAG_DIALOG_RATE_FEEDBACK"
    }


    private val viewModel by viewModels<AppSettingsViewModel>()
    private val themePrefs by lazy { ThemePreferencesTools(requireContext()) }

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
        initToolbar()
        (view as ComposeView).setContent {
            val settingsState by viewModel.settingsState.collectAsState()
            LaunchedEffect(Unit) {
                viewModel.getData()
                viewModel.setThemeMode(themePrefs.mode)
                viewModel.message.collectLatest { message ->
                    showSnackBar(message)
                }
            }

            ManagerTheme {
                SettingsScreen(requireContext(), settingsState, viewModel::setWifiState, viewModel::setAnalytic)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.getData()
        viewModel.setThemeMode(themePrefs.mode)
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

    @Preview(widthDp = 360, heightDp = 640)
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
                onWifiState = {},
                onAnalytics = {}
            )
        }
    }

    private fun getThemeString(mode: Int) = when (mode) {
        AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM -> R.string.app_settings_follow_the_system
        AppCompatDelegate.MODE_NIGHT_NO -> R.string.app_settings_light_theme
        AppCompatDelegate.MODE_NIGHT_YES -> R.string.app_settings_dark_theme
        else -> null
    }

    private fun showThemeDialog() {
        var chosenMode = 0
        (requireActivity() as? BaseActivity)?.getCustomDialog(
            title = getString(R.string.app_settings_color_theme),
            acceptTitle = getString(R.string.dialogs_common_ok_button),
            cancelTitle = getString(R.string.dialogs_common_cancel_button),
            view = ComposeView(requireContext()).apply {
                setContent {
                    ManagerTheme {
                        AppThemeDialog.MainScreen(themeMode = themePrefs.mode) { mode -> chosenMode = mode }
                    }
                }
            },
            acceptListener = {
                themePrefs.mode = chosenMode
                viewModel.setThemeMode(chosenMode)
                AppCompatDelegate.setDefaultNightMode(chosenMode)
            }
        )?.show()
    }
}

@Composable
fun AppSettingsItem(title: Int, arrowVisible: Boolean = false, option: String? = null, onClick: () -> Unit) {
    AppArrowItem(
        title = title,
        option = option,
        arrowVisible = arrowVisible,
        dividerVisible = false,
        onClick = onClick
    )
}