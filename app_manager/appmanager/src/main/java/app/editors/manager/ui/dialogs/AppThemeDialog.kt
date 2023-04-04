package app.editors.manager.ui.dialogs

import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.MaterialTheme
import androidx.compose.material.RadioButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import app.editors.manager.R

object AppThemeDialog {

    @Composable
    fun MainScreen(themeMode: Int, onClick: (Int) -> Unit) {
        val mode = remember { mutableStateOf(themeMode) }

        Column(modifier = Modifier.fillMaxWidth()) {
            RadioItem(
                title = stringResource(id = R.string.app_settings_follow_the_system),
                selected = mode.value == AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM,
                onClick = {
                    mode.value = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
                    onClick(mode.value)
                }
            )
            RadioItem(
                title = stringResource(id = R.string.app_settings_light_theme),
                selected = mode.value == AppCompatDelegate.MODE_NIGHT_NO,
                onClick = {
                    mode.value = AppCompatDelegate.MODE_NIGHT_NO
                    onClick(mode.value)
                }
            )
            RadioItem(
                title = stringResource(id = R.string.app_settings_dark_theme),
                selected = mode.value == AppCompatDelegate.MODE_NIGHT_YES,
                onClick = {
                    mode.value = AppCompatDelegate.MODE_NIGHT_YES
                    onClick(mode.value)
                }
            )
        }
    }

    @Composable
    private fun RadioItem(title: String, selected: Boolean, onClick: () -> Unit) {
        Row(
            modifier = Modifier
                .clickable { onClick() }
                .fillMaxWidth()
                .height(dimensionResource(id = lib.toolkit.base.R.dimen.item_one_line_height)),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = selected,
                onClick = onClick,
            )
            Text(
                text = title,
                style = MaterialTheme.typography.subtitle1,
                color = MaterialTheme.colors.onSurface
            )
        }
    }
}