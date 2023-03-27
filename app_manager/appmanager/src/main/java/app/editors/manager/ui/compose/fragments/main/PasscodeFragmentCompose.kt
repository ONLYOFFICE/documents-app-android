package app.editors.manager.ui.compose.fragments.main

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.navigation.NavController
import app.editors.manager.managers.utils.BiometricsUtils
import app.editors.manager.ui.compose.activities.main.PasscodeScreens
import app.editors.manager.viewModels.main.SetPasscodeViewModel
import lib.compose.ui.views.AppScaffold
import lib.compose.ui.views.TopAppBar
import lib.compose.ui.views.VerticalSpacer
import lib.toolkit.base.R

@Composable
fun PasscodeLock(
    viewModel: SetPasscodeViewModel,
    navController: NavController,
    backPressed: () -> Unit
) {
    val isEnablePasscode by viewModel.isPasscodeEnable.observeAsState(initial = false)
    val isEnableFingerprint by viewModel.isFingerprintEnable.observeAsState(initial = false)


    AppScaffold(topBar = {
        TopAppBar(
            title = app.editors.manager.R.string.app_settings_passcode,
            backListener = backPressed
        )
    }) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .padding(
                    start = dimensionResource(id = R.dimen.screen_left_right_padding),
                    end = dimensionResource(id = R.dimen.screen_left_right_padding)
                )
                .fillMaxSize()
        ) {
            VerticalSpacer(height = R.dimen.default_margin_large)
            PasscodeSwitchItem(
                isEnable = isEnablePasscode,
                text = stringResource(id = app.editors.manager.R.string.app_Settings_passcode_enable),
                onCheckedChange = { state ->
                    if (state) {
                        navController.navigate(PasscodeScreens.SetPasscode.screen) {
                            navController.graph.startDestinationRoute?.let {
                                popUpTo(it) {
                                    saveState = false
                                }
                            }
                            launchSingleTop = true
                        }
                    } else {
                        navController.navigate(PasscodeScreens.DisablePasscode.screen) {
                            navController.graph.startDestinationRoute?.let {
                                popUpTo(it) {
                                    saveState = false
                                }
                            }
                            launchSingleTop = true
                        }
                    }
                }
            )

            if (isEnablePasscode) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .height(dimensionResource(id = R.dimen.item_one_line_height))
                        .clickable(onClick = {
                            navController.navigate(PasscodeScreens.ChangePasscode.screen)
                        })
                        .fillMaxSize()
                ) {
                    Text(
                        text = stringResource(id = app.editors.manager.R.string.app_settings_passcode_change),
                        modifier = Modifier
                            .align(Alignment.CenterVertically)
                            .padding(start = dimensionResource(id = R.dimen.default_margin_medium)),
                        style = TextStyle(
                            color = colorResource(id = R.color.colorLink)

                        )
                    )
                }
            }
            VerticalSpacer(height = R.dimen.default_margin_large)
            Text(
                text = buildAnnotatedString {
                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                        append(stringResource(id = app.editors.manager.R.string.app_settings_passcode))
                    }
                    append(stringResource(id = app.editors.manager.R.string.app_settings_passcode_description))
                },
                modifier = Modifier.padding(
                    start = dimensionResource(id = R.dimen.default_margin_medium),
                    end = dimensionResource(id = R.dimen.default_margin_medium)
                ),
                style = MaterialTheme.typography.subtitle2,
                color = MaterialTheme.colors.onBackground
            )
            if (isEnablePasscode && BiometricsUtils.isFingerprintsExist(LocalContext.current)) {
                VerticalSpacer(R.dimen.default_margin_large)
                PasscodeSwitchItem(
                    isEnable = isEnableFingerprint,
                    text = stringResource(id = app.editors.manager.R.string.app_settings_passcode_fingerprint),
                    onCheckedChange = {
                        viewModel.setFingerprintState(!isEnableFingerprint)
                    }
                )
            }
        }
    }
}

@Composable
fun PasscodeSwitchItem(isEnable: Boolean, text: String, onCheckedChange: (Boolean) -> Unit) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .height(dimensionResource(id = R.dimen.item_onehalf_line_height))
            .background(color = MaterialTheme.colors.surface)
            .fillMaxSize()
    ) {
        Text(
            text = text,
            modifier = Modifier
                .align(Alignment.CenterVertically)
                .padding(start = dimensionResource(id = R.dimen.default_margin_medium))
                .fillMaxWidth(0.75f),
            color = MaterialTheme.colors.onSurface
        )
        Switch(
            checked = isEnable,
            onCheckedChange = onCheckedChange,
            modifier = Modifier
                .align(Alignment.CenterVertically)
                .padding(end = dimensionResource(id = R.dimen.default_margin_medium)),
            colors = SwitchDefaults.colors(
                checkedThumbColor = colorResource(id = R.color.colorSecondary)
            )
        )
    }
}

