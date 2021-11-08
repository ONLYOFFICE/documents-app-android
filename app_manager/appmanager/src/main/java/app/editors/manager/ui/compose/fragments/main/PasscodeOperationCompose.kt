package app.editors.manager.ui.compose.fragments.main

import androidx.compose.animation.*
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.editors.manager.compose.ui.theme.AppManagerTheme
import app.editors.manager.ui.compose.base.Spacer
import app.editors.manager.viewModels.main.PasscodeLockState
import app.editors.manager.viewModels.main.SetPasscodeViewModel
import lib.toolkit.base.R


sealed class KeyboardLastRow {
    class NumberItem(): KeyboardLastRow()
    class ImageItem(): KeyboardLastRow()
    class FingerprintImage(): KeyboardLastRow()
}

class PasscodeOperationCompose {

    companion object {

        private const val MAX_PASSCODE_LENGTH = 4
        private const val KEYBOARD_COLUMN_VALUE = 3

        @ExperimentalFoundationApi
        @Composable
        fun PasscodeOperation(
            viewModel: SetPasscodeViewModel,
            title: String,
            subtitle: String,
            onEnterCode: (Int) -> Unit,
            onState: ((PasscodeLockState) -> Unit)? = null
        ) {
            val keyboard = (1..9).map { it.toString() }

            val lastRow: List<KeyboardLastRow> = listOf(
                KeyboardLastRow.FingerprintImage(),
                KeyboardLastRow.NumberItem(),
                KeyboardLastRow.ImageItem()
            )

            var codeCount by remember { mutableStateOf(-1) }
            val isError by viewModel.error.observeAsState(initial = false)
            val passcodeState by viewModel.passcodeLockState.observeAsState()

            passcodeState?.let { onState?.invoke(it) }

            AppManagerTheme {
                Column(
                    modifier = Modifier
                        .background(color = colorResource(id = R.color.colorLight))
                        .padding(
                            start = dimensionResource(id = R.dimen.screen_left_right_padding),
                            end = dimensionResource(id = R.dimen.screen_left_right_padding),
                            top = 170.dp
                        )
                        .fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {


                    Text(text = title, fontSize = 20.sp)

                    Spacer(size = dimensionResource(id = R.dimen.default_margin_large))

                    if(!isError) {
                        Text(text = subtitle, fontSize = 14.sp)
                    } else {
                        Text(text = (passcodeState as PasscodeLockState.Error).errorMessage, fontSize = 14.sp, color = colorResource(id = R.color.colorLightRed))
                        codeCount = -1
                    }

                    Spacer(size = dimensionResource(id = R.dimen.default_margin_xxlarge))

                    LazyVerticalGrid(cells = GridCells.Fixed(4)) {
                        items(MAX_PASSCODE_LENGTH) {
                            if(!isError) {
                                if (it <= codeCount) {
                                    Image(
                                        painter = painterResource(id = app.editors.manager.R.drawable.passcode_filled_dot),
                                        contentDescription = "filled_dot"
                                    )
                                } else {
                                    Image(
                                        painter = painterResource(id = app.editors.manager.R.drawable.passcode_normal_dot),
                                        contentDescription = "normal_dot"
                                    )
                                }
                            } else {
                                Image(
                                    painter = painterResource(id = app.editors.manager.R.drawable.passcode_error_dot),
                                    contentDescription = "error_dot"
                                )
                            }
                        }
                    }

                    Spacer(size = dimensionResource(id = R.dimen.passcode_keyboard_margin))

                    LazyVerticalGrid(cells = GridCells.Fixed(KEYBOARD_COLUMN_VALUE)) {
                        items(keyboard.size) {
                            Button(
                                onClick = {
                                    codeCount++
                                    if (codeCount >= MAX_PASSCODE_LENGTH) {
                                        codeCount = -1
                                    }
                                    onEnterCode.invoke(it + 1)
                                },
                                colors = ButtonDefaults.textButtonColors(
                                    backgroundColor = colorResource(
                                        id = R.color.colorTransparent
                                    )
                                ),
                                enabled = !isError,
                                elevation = ButtonDefaults.elevation(0.dp, 0.dp, 0.dp),
                                modifier = Modifier
                                    .padding(bottom = dimensionResource(id = R.dimen.default_margin_xlarge))
                                    .width(68.dp)
                                    .height(58.dp)
                            ) {
                                Text(
                                    text = "${keyboard[it]}",
                                    fontSize = 20.sp,
                                    color = colorResource(id = R.color.colorBlack)
                                )
                            }
                        }
                    }

                    LazyVerticalGrid(cells = GridCells.Fixed(KEYBOARD_COLUMN_VALUE)) {
                        items(lastRow) { item ->
                            when (item) {
                                is KeyboardLastRow.FingerprintImage -> {
                                    IconButton(onClick = { /*TODO*/ }) {
                                        Icon(
                                            painter = painterResource(id = app.editors.manager.R.drawable.ic_fingerprint),
                                            contentDescription = "fingerprint_icon"
                                        )
                                    }
                                }
                                is KeyboardLastRow.ImageItem -> {
                                    IconButton(onClick = {
                                        if(codeCount > -1) codeCount-- else codeCount = -1
                                        viewModel.codeBackspace()
                                    }) {
                                        Icon(
                                            painter = painterResource(id = app.editors.manager.R.drawable.ic_backspace),
                                            contentDescription = "backspace_icon"
                                        )
                                    }
                                }
                                is KeyboardLastRow.NumberItem -> {
                                    Button(
                                        onClick = {
                                            codeCount++
                                            if (codeCount >= MAX_PASSCODE_LENGTH) {
                                                codeCount = -1
                                            }
                                            onEnterCode.invoke(0)
                                        },
                                        colors = ButtonDefaults.textButtonColors(
                                            backgroundColor = colorResource(
                                                id = R.color.colorTransparent
                                            )
                                        ),
                                        elevation = ButtonDefaults.elevation(0.dp, 0.dp, 0.dp),
                                        modifier = Modifier
                                            .padding(bottom = dimensionResource(id = R.dimen.default_margin_xlarge))
                                            .width(68.dp)
                                            .height(58.dp)
                                    ) {
                                        Text(
                                            text = "0",
                                            fontSize = 20.sp,
                                            color = colorResource(id = R.color.colorBlack)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

            }
        }
    }
}