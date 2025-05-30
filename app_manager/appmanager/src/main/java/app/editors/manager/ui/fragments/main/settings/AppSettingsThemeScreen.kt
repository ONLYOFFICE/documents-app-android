package app.editors.manager.ui.fragments.main.settings

import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import app.editors.manager.R
import lib.compose.ui.views.AppSelectItem


@Composable
fun AppSettingsThemeScreen(
    currentTheme: Int,
    onSetTheme: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = MaterialTheme.colors.background)
    ) {
        AppSelectItem(
            title = stringResource(id = R.string.app_settings_follow_the_system),
            selected = currentTheme == AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM,
            dividerVisible = false,
            onClick = {
                onSetTheme(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
            }
        )
        AppSelectItem(
            title = stringResource(id = R.string.app_settings_light_theme),
            selected = currentTheme == AppCompatDelegate.MODE_NIGHT_NO,
            dividerVisible = false,
            onClick = {
                onSetTheme(AppCompatDelegate.MODE_NIGHT_NO)
            }
        )
        AppSelectItem(
            title = stringResource(id = R.string.app_settings_dark_theme),
            selected = currentTheme == AppCompatDelegate.MODE_NIGHT_YES,
            dividerVisible = false,
            onClick = {
                onSetTheme(AppCompatDelegate.MODE_NIGHT_YES)
            }
        )
    }
}