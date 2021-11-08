package app.editors.manager.ui.compose.fragments.main

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Switch
import androidx.compose.material.SwitchDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import app.editors.manager.compose.ui.theme.AppManagerTheme
import app.editors.manager.compose.ui.theme.Typography
import app.editors.manager.ui.activities.main.PasscodeScreens
import app.editors.manager.viewModels.main.SetPasscodeViewModel
import lib.toolkit.base.R
import app.editors.manager.ui.compose.base.Spacer


class PasscodeFragmentCompose {
    companion object {

        @Composable
        fun PasscodeLock(
            viewModel: SetPasscodeViewModel,
            navController: NavController
        ) {
            AppManagerTheme {
                val isEnablePasscode by viewModel.isPasscodeEnable.observeAsState(initial = false)
                val isEnableFingerprint by viewModel.isFingerprintEnable.observeAsState(initial = false)

                Column(modifier = Modifier
                    .background(color = colorResource(id = R.color.colorLight))
                    .padding(
                        start = dimensionResource(id = R.dimen.screen_left_right_padding),
                        end = dimensionResource(id = R.dimen.screen_left_right_padding)
                    )
                    .fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally) {

                    Spacer(size = dimensionResource(id = R.dimen.default_margin_large))

                    PasscodeSwitchItem(
                        isEnable = isEnablePasscode,
                        text = "Enable passcode",
                        onCheckedChange = { state ->
                            if(state) {
                                navController.navigate(PasscodeScreens.SetPasscode.screen)
                            } else {
                                navController.navigate(PasscodeScreens.DisablePasscode.screen)
                            }
                        }
                    )

                    if(isEnablePasscode) {
                        Row(horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier
                                .height(dimensionResource(id = lib.editors.gbase.R.dimen.default_one_line_height))
                                .background(color = colorResource(id = R.color.colorWhite))
                                .clickable(onClick = {
                                    navController.navigate(PasscodeScreens.ChangePasscode.screen)
                                })
                                .fillMaxSize()
                        ) {
                            Text(
                                text = "Change passcode",
                                modifier = Modifier
                                    .align(Alignment.CenterVertically)
                                    .padding(start = 16.dp),
                                style = TextStyle(
                                    color = colorResource(id = R.color.colorLink)

                                )
                            )
                        }
                    }

                    Spacer(size = dimensionResource(id = R.dimen.default_margin_large))

                    Text(
                        buildAnnotatedString {
                            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                append("Passсode Lock")
                            }
                            append(" - это код, который запрашивается, при запуске приложения.\n" +
                                    "\n" +
                                    "Обратите внимание, если вы забудете код доступа, вам понадобится удалить или переустановить приложение. Все локальные документы будут утеряны.")
                        },
                        modifier = Modifier.padding(
                            start = dimensionResource(id = R.dimen.default_margin_medium),
                            end = dimensionResource(id = R.dimen.default_margin_medium)
                        ),
                        style = Typography.subtitle2
                    )

                    if(isEnablePasscode) {

                        Spacer(size = dimensionResource(id = R.dimen.default_margin_large))

                        PasscodeSwitchItem(
                            isEnable = isEnableFingerprint,
                            text = "Use Fingerprint to unlock",
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
            Row(horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .height(dimensionResource(id = lib.editors.gbase.R.dimen.default_one_line_height))
                    .background(color = colorResource(id = R.color.colorWhite))
                    .fillMaxSize()
            ) {
                Text(
                    text = text,
                    modifier = Modifier
                        .align(Alignment.CenterVertically)
                        .padding(start = 16.dp)
                )
                Switch(
                    checked = isEnable,
                    onCheckedChange = onCheckedChange,
                    modifier = Modifier
                        .align(Alignment.CenterVertically)
                        .padding(end = 16.dp),
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = colorResource(id = R.color.colorSecondary)
                    )
                )
            }
        }
    }
}

