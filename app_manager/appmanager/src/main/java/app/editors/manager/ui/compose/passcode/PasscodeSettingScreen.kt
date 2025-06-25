package app.editors.manager.ui.compose.passcode

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import app.editors.manager.managers.utils.BiometricsUtils
import app.editors.manager.mvp.models.states.PasscodeLockState
import lib.compose.ui.theme.colorTextSecondary
import lib.compose.ui.views.AppDivider
import lib.compose.ui.views.AppSwitchItem
import lib.compose.ui.views.VerticalSpacer
import lib.toolkit.base.R

@Composable
fun PasscodeSettingScreen(
    passcodeLockState: PasscodeLockState,
    onPasscodeEnable: (Boolean) -> Unit,
    onFingerprintEnable: (Boolean) -> Unit,
    onChangePassword: () -> Unit
) {
    Surface(color = MaterialTheme.colors.background) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxSize()
        ) {
            AppSwitchItem(
                title = app.editors.manager.R.string.app_Settings_passcode_enable,
                checked = passcodeLockState.enabled,
                dividerVisible = passcodeLockState.enabled,
                onCheck = onPasscodeEnable
            )
            if (passcodeLockState.enabled) {
                Row(
                    modifier = Modifier
                        .height(dimensionResource(id = R.dimen.item_one_line_height))
                        .clickable(onClick = onChangePassword)
                        .padding(horizontal = 16.dp)
                        .fillMaxSize()
                ) {
                    Text(
                        text = stringResource(id = app.editors.manager.R.string.app_settings_passcode_change),
                        modifier = Modifier.align(Alignment.CenterVertically),
                        style = MaterialTheme.typography.body1.copy(
                            color = colorResource(id = R.color.colorPrimary)
                        )
                    )
                }
            }
            AppDivider()
            VerticalSpacer(height = R.dimen.default_margin_large)
            Text(
                text = buildAnnotatedString {
                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                        append(stringResource(id = app.editors.manager.R.string.app_settings_passcode))
                    }
                    append(" ")
                    append(stringResource(id = app.editors.manager.R.string.app_settings_passcode_description))
                },
                modifier = Modifier.padding(
                    start = dimensionResource(id = R.dimen.default_margin_large),
                    end = dimensionResource(id = R.dimen.default_margin_large)
                ),
                style = MaterialTheme.typography.body2,
                color = MaterialTheme.colors.colorTextSecondary
            )
            if (passcodeLockState.enabled && BiometricsUtils.isFingerprintsExist(LocalContext.current)) {
                VerticalSpacer(R.dimen.default_margin_large)
                AppDivider()
                AppSwitchItem(
                    title = app.editors.manager.R.string.app_settings_passcode_fingerprint,
                    checked = passcodeLockState.fingerprintEnabled,
                    onCheck = onFingerprintEnable
                )
            }
        }
    }
}