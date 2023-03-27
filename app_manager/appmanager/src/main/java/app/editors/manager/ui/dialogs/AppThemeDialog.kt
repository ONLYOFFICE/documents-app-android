package app.editors.manager.ui.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.editors.manager.R
import lib.compose.ui.theme.ManagerTheme
import lib.toolkit.base.managers.tools.ThemePreferencesTools
import lib.toolkit.base.managers.utils.UiUtils
import lib.toolkit.base.ui.dialogs.base.BaseDialog

class AppThemeDialog : BaseDialog() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return ComposeView(requireContext())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val preferences = ThemePreferencesTools(requireContext())
        (view as ComposeView).setContent {
            ManagerTheme {
                MainScreen(themeMode = preferences.mode, cancel = { dismiss() }, ok = { mode ->
                    preferences.mode = mode
                    AppCompatDelegate.setDefaultNightMode(mode)
                    dismiss()
                })
            }
        }
    }

}

@Composable
private fun MainScreen(themeMode: Int, cancel: () -> Unit, ok: (mode: Int) -> Unit) {
    val context = LocalContext.current
    val mode = remember {
        mutableStateOf(themeMode)
    }
    Surface(shape = MaterialTheme.shapes.medium) {
        Box(modifier = Modifier
            .fillMaxWidth(if (UiUtils.isTablet(context)) 0.7f else 0.9f)
            .wrapContentHeight()) {
            Column(modifier = Modifier
                .padding(start = 24.dp, top = 16.dp, end = 24.dp, bottom = 16.dp)
                .fillMaxWidth()) {
                Text(text = stringResource(id = R.string.app_settings_color_theme),
                    Modifier.padding(bottom = 16.dp),
                    style = MaterialTheme.typography.subtitle1)
                RadioItem(title = stringResource(id = R.string.app_settings_follow_the_system),
                    selected = mode.value == AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM,
                    onClick = { mode.value = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM })
                RadioItem(title = stringResource(id = R.string.app_settings_light_theme),
                    selected = mode.value == AppCompatDelegate.MODE_NIGHT_NO,
                    onClick = { mode.value = AppCompatDelegate.MODE_NIGHT_NO })
                RadioItem(title = stringResource(id = R.string.app_settings_dark_theme),
                    selected = mode.value == AppCompatDelegate.MODE_NIGHT_YES,
                    onClick = { mode.value = AppCompatDelegate.MODE_NIGHT_YES })
                Row(modifier = Modifier
                    .padding(top = 24.dp)
                    .align(Alignment.End),
                    horizontalArrangement = Arrangement.Start) {
                    TextButton(onClick = cancel) {
                        Text(text = stringResource(id = R.string.dialogs_common_cancel_button))
                    }
                    TextButton(onClick = { ok(mode.value) }) {
                        Text(text = stringResource(id = R.string.dialogs_common_ok_button))
                    }
                }
            }
        }
    }
}

@Composable
private fun RadioItem(title: String, selected: Boolean, onClick: () -> Unit) {
    Row(modifier = Modifier
        .clickable { onClick() }
        .fillMaxWidth()
        .height(dimensionResource(id = lib.toolkit.base.R.dimen.item_one_line_height))) {
        RadioButton(selected = selected,
            onClick = onClick,
            modifier = Modifier.align(Alignment.CenterVertically))
        Text(text = title, modifier = Modifier
            .padding(start = 26.dp)
            .align(Alignment.CenterVertically), style = MaterialTheme.typography.subtitle1)
    }
}