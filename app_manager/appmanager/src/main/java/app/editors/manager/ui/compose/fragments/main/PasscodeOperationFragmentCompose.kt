package app.editors.manager.ui.compose.fragments.main

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.editors.manager.managers.utils.BiometricsUtils
import app.editors.manager.ui.activities.main.PasscodeActivity
import app.editors.manager.ui.compose.base.Spacer
import app.editors.manager.viewModels.main.PasscodeLockState
import app.editors.manager.viewModels.main.SetPasscodeViewModel
import lib.toolkit.base.R
import lib.toolkit.base.managers.utils.UiUtils


sealed class KeyboardLastRow {
    object NumberItem : KeyboardLastRow()
    object ImageItem : KeyboardLastRow()
    object FingerprintImage : KeyboardLastRow()
}

private const val MAX_PASSCODE_LENGTH = 4
private const val KEYBOARD_COLUMN_VALUE = 3

@Composable
fun PasscodeOperation(
    viewModel: SetPasscodeViewModel,
    title: String,
    subtitle: String = "",
    isEnterCodeFragment: Boolean = false,
    isConfirmCode: Boolean = true,
    onEnterCode: (Int) -> Unit,
    onState: ((PasscodeLockState) -> Unit)? = null,
) {
    val keyboard = (1..9)

    val lastRow: List<KeyboardLastRow> = listOf(
        KeyboardLastRow.FingerprintImage,
        KeyboardLastRow.NumberItem,
        KeyboardLastRow.ImageItem
    )

    val codeCount by viewModel.codeCount.observeAsState(-1)
    val isError by viewModel.error.observeAsState(initial = false)
    val passcodeState by viewModel.passcodeLockState.observeAsState()
    val fingerprintState by viewModel.isFingerprintEnable.observeAsState()

    passcodeState?.let { onState?.invoke(it) }

    Column(
        modifier = Modifier
            .padding(
                start = dimensionResource(id = R.dimen.screen_left_right_padding),
                end = dimensionResource(id = R.dimen.screen_left_right_padding),
                top = if (UiUtils.isTablet(LocalContext.current)) 100.dp else 170.dp
            )
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = title, style = MaterialTheme.typography.h6, color = MaterialTheme.colors.onBackground, textAlign = TextAlign.Center)

        Spacer(size = dimensionResource(id = R.dimen.default_margin_large))

        if (!isError) {
            Text(text = subtitle, style = MaterialTheme.typography.body2, color = MaterialTheme.colors.onBackground, textAlign = TextAlign.Center)
        } else {
            Text(
                text = (passcodeState as PasscodeLockState.Error).errorMessage,
                fontSize = 14.sp,
                color = MaterialTheme.colors.error,
                textAlign = TextAlign.Center
            )
            viewModel.resetCodeCount()
        }

        Spacer(size = dimensionResource(id = R.dimen.default_margin_xxlarge))

        val margins = if (UiUtils.isTablet(LocalContext.current)) 128.dp else 32.dp

        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            items(MAX_PASSCODE_LENGTH) {
                if (!isError) {
                    if (it <= codeCount) {
                        Icon(
                            painter = painterResource(id = app.editors.manager.R.drawable.passcode_filled_dot),
                            contentDescription = "filled_dot",
                            tint = MaterialTheme.colors.primary
                        )
                    } else {
                        Icon(
                            painter = painterResource(id = app.editors.manager.R.drawable.passcode_normal_dot),
                            contentDescription = "normal_dot",
                            tint = MaterialTheme.colors.onBackground
                        )
                    }
                } else {
                    Icon(
                        painter = painterResource(id = app.editors.manager.R.drawable.passcode_error_dot),
                        contentDescription = "error_dot",
                        tint = MaterialTheme.colors.error
                    )
                }
            }
        }

        Spacer(size = dimensionResource(id = R.dimen.passcode_keyboard_margin))

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp), horizontalAlignment = Alignment.CenterHorizontally
        ) {
            items(keyboard.chunked(KEYBOARD_COLUMN_VALUE)) { rowItem ->
                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    for (number in rowItem) {
                        Button(
                            onClick = {
                                onEnterCode.invoke(number)
                            },
                            colors = ButtonDefaults.textButtonColors(
                                backgroundColor = Color.Transparent
                            ),
                            enabled = !isError,
                            elevation = ButtonDefaults.elevation(0.dp, 0.dp, 0.dp),
                            modifier = Modifier
                                .padding(bottom = dimensionResource(id = R.dimen.default_margin_xlarge))
                                .width(68.dp)
                                .height(58.dp)
                        ) {
                            Text(
                                text = "$number",
                                fontSize = 20.sp,
                                color = MaterialTheme.colors.onBackground
                            )
                        }
                    }

                }
            }
        }


        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp), horizontalArrangement = Arrangement.SpaceBetween
        ) {
            items(lastRow) { item ->
                when (item) {
                    KeyboardLastRow.FingerprintImage -> {
                        if (fingerprintState == true && isEnterCodeFragment && BiometricsUtils.isFingerprintsExist(LocalContext.current as PasscodeActivity)) {
                            IconButton(onClick = { viewModel.openBiometricDialog() }, enabled = !isError) {
                                Icon(
                                    painter = painterResource(id = app.editors.manager.R.drawable.ic_fingerprint),
                                    contentDescription = "fingerprint_icon",
                                    tint = MaterialTheme.colors.primary,
                                    modifier = Modifier
                                        .width(68.dp)
                                        .height(58.dp)
                                )
                            }
                        }
                    }
                    KeyboardLastRow.ImageItem -> {
                        IconButton(
                            onClick = {
                                if (isConfirmCode) viewModel.confirmCodeBackSpace() else viewModel.codeBackspace()
                            },
                            enabled = !isError,
                        ) {
                            Icon(
                                painter = painterResource(id = app.editors.manager.R.drawable.ic_backspace),
                                contentDescription = "backspace_icon",
                                tint = MaterialTheme.colors.onBackground,
                                modifier = Modifier
                                    .width(68.dp)
                                    .height(58.dp)

                            )
                        }
                    }
                    KeyboardLastRow.NumberItem -> {
                        Button(
                            onClick = {
                                onEnterCode.invoke(0)
                            },
                            enabled = !isError,
                            colors = ButtonDefaults.textButtonColors(
                                backgroundColor = Color.Transparent
                            ),
                            elevation = ButtonDefaults.elevation(0.dp, 0.dp, 0.dp),
                            modifier = Modifier
                                .padding(
                                    bottom = dimensionResource(id = R.dimen.default_margin_xlarge),
                                    start = if (fingerprintState == false || !isEnterCodeFragment) 68.dp else 0.dp
                                )
                                .width(68.dp)
                                .height(58.dp)
                        ) {
                            Text(
                                text = "0",
                                fontSize = 20.sp,
                                color = MaterialTheme.colors.onBackground
                            )
                        }
                    }
                }
            }
        }
    }


}
