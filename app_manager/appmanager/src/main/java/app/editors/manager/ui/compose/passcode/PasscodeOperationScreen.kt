package app.editors.manager.ui.compose.passcode

import android.content.pm.ActivityInfo
import android.os.SystemClock
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.editors.manager.R
import app.editors.manager.managers.utils.KeyStoreUtils
import app.editors.manager.managers.utils.VibrationUtils
import app.editors.manager.mvp.models.states.PasscodeLockState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import lib.compose.ui.LockScreenOrientation
import lib.compose.ui.theme.ManagerTheme
import lib.compose.ui.theme.Previews
import lib.compose.ui.theme.colorTextSecondary
import lib.compose.ui.theme.colorTextTertiary
import lib.compose.ui.views.VerticalSpacer
import lib.toolkit.base.managers.utils.UiUtils


private const val MAX_PASSCODE_LENGTH = 4

data class PasscodeOperationState(
    val mode: PasscodeOperationMode,
    val passcodeLockState: PasscodeLockState
)

sealed class PasscodeOperationMode(
    val title: Int,
    val subtitle: Int?,
    val error: Int?
) {

    data object UnlockApp : PasscodeOperationMode(
        title = R.string.app_settings_passscode_enter_full_title,
        subtitle = null,
        error = R.string.app_settings_passcode_change_disable_error
    )

    data object ChangePasscode : PasscodeOperationMode(
        title = R.string.app_settings_passcode_change_disable_title,
        subtitle = null,
        error = R.string.app_settings_passcode_change_disable_error
    )

    data object SetPasscode : PasscodeOperationMode(
        title = R.string.app_settings_passcode_enter_title,
        subtitle = R.string.app_settings_passcode_enter_subtitle,
        error = null
    )

    data object ResetPasscode : PasscodeOperationMode(
        title = R.string.app_settings_passcode_change_disable_title,
        subtitle = null,
        error = R.string.app_settings_passcode_change_disable_error
    )

    data object Confirm : PasscodeOperationMode(
        title = R.string.app_settings_passcode_confirm_title,
        subtitle = R.string.app_settings_passcode_confirm_subtitle,
        error = R.string.app_settings_passcode_confirm_error
    )
}

@Composable
fun PasscodeOperationScreen(
    initialState: PasscodeOperationState,
    encryptedPasscode: String? = null,
    onFingerprintClick: () -> Unit = {},
    onConfirmFailed: () -> Unit = {},
    onConfirmSuccess: (String) -> Unit,
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var state by remember { mutableStateOf(initialState) }
    val subtitleState = remember { mutableStateOf(initialState.mode.subtitle?.let(context::getString).orEmpty()) }
    val enteredCode = remember { mutableStateOf("") }
    var tmpCode by remember { mutableStateOf("") }
    val errorState = remember { mutableStateOf(false) }
    var buttonPressLocking by remember { mutableStateOf(false) }
    val vibration = remember { VibrationUtils.getService(context) }

    fun checkPasscode(enterCode: String, confirmingCode: String?, onFailure: () -> Unit = {}, onSuccess: () -> Unit) {
        coroutineScope.launch {
            if (enterCode != confirmingCode) {
                vibration.cancel()
                vibration.vibrate(VibrationUtils.errorEffect)

                buttonPressLocking = true
                subtitleState.value = state.mode.error?.let(context::getString).orEmpty()
                errorState.value = true

                delay(2000L)
                buttonPressLocking = false
                subtitleState.value = state.mode.subtitle?.let(context::getString).orEmpty()
                errorState.value = false

                enteredCode.value = ""
                onFailure.invoke()
            } else {
                onSuccess.invoke()
            }
        }
    }

    fun onEnterFinish() {
        when (state.mode) {
            PasscodeOperationMode.ChangePasscode -> {
                val savedPasscode = encryptedPasscode?.let(KeyStoreUtils::decryptData)
                checkPasscode(
                    enterCode = enteredCode.value,
                    confirmingCode = savedPasscode,
                    onFailure = onConfirmFailed,
                    onSuccess = {
                        enteredCode.value = ""
                        state = state.copy(mode = PasscodeOperationMode.SetPasscode)
                    }
                )
            }
            PasscodeOperationMode.SetPasscode -> {
                tmpCode = enteredCode.value
                enteredCode.value = ""
                state = state.copy(mode = PasscodeOperationMode.Confirm)
            }
            PasscodeOperationMode.UnlockApp, PasscodeOperationMode.ResetPasscode -> {
                val savedPasscode = encryptedPasscode?.let(KeyStoreUtils::decryptData)
                checkPasscode(
                    enterCode = enteredCode.value,
                    confirmingCode = savedPasscode,
                    onFailure = onConfirmFailed,
                    onSuccess = { onConfirmSuccess.invoke(enteredCode.value) }
                )
            }
            PasscodeOperationMode.Confirm -> {
                checkPasscode(
                    enterCode = enteredCode.value,
                    confirmingCode = tmpCode,
                    onFailure = {
                        when (initialState.mode) {
                            is PasscodeOperationMode.ChangePasscode -> {
                                tmpCode = ""
                                enteredCode.value = ""
                                state = state.copy(mode = PasscodeOperationMode.SetPasscode)
                            }
                            is PasscodeOperationMode.SetPasscode -> {
                                tmpCode = ""
                                enteredCode.value = ""
                                state = state.copy(mode = PasscodeOperationMode.SetPasscode)
                            }
                            else -> {}
                        }
                    },
                    onSuccess = { onConfirmSuccess.invoke(enteredCode.value) }
                )
            }
        }
    }

    fun onButtonPress(number: Int) {
        coroutineScope.launch {
            if (buttonPressLocking) {
                return@launch
            }

            vibration.cancel()
            vibration.vibrate(VibrationUtils.clickEffect)

            if (errorState.value) {
                errorState.value = false
            }

            if (enteredCode.value.length < MAX_PASSCODE_LENGTH) {
                enteredCode.value += "$number"
            }

            if (enteredCode.value.length == MAX_PASSCODE_LENGTH) {
                onEnterFinish()
            }
        }
    }

    LaunchedEffect(state.mode.subtitle) {
        subtitleState.value = state.mode.subtitle?.let(context::getString).orEmpty()
    }

    LaunchedEffect(initialState.passcodeLockState.attemptsUnlockTime) {
        if (initialState.passcodeLockState.manyAttemptsLock) {
            coroutineScope.launch {
                val timestamp = initialState.passcodeLockState.attemptsUnlockTime ?: 0
                buttonPressLocking = true
                while (true) {
                    val timeLeft = ((timestamp - SystemClock.elapsedRealtime()) / 1000).toInt()
                    subtitleState.value = when {
                        timeLeft < 1 -> break
                        timeLeft > 59 -> {
                            context.resources.getQuantityString(
                                R.plurals.app_settings_passcode_many_attempt_minutes,
                                timeLeft / 60,
                                timeLeft / 60
                            )
                        }
                        else -> {
                            context.resources.getQuantityString(
                                R.plurals.app_settings_passcode_many_attempt_seconds,
                                timeLeft,
                                timeLeft
                            )
                        }
                    }
                    delay(1000)
                }
                subtitleState.value = initialState.mode.subtitle?.let(context::getString).orEmpty()
                buttonPressLocking = false
            }
        }
    }

    LockScreenOrientation(orientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .background(MaterialTheme.colors.background)
            .fillMaxSize()
    ) {
        Column(
            modifier = Modifier.run {
                if (UiUtils.isTablet(LocalContext.current))
                    width(320.dp) else
                    fillMaxWidth()
            },
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AnimatedContent(
                targetState = state.mode.title,
                transitionSpec = { fadeIn().togetherWith(fadeOut()) },
                label = "animated_text_title"
            ) { title ->
                Text(
                    text = stringResource(id = title),
                    style = MaterialTheme.typography.h6,
                    color = MaterialTheme.colors.onSurface,
                    textAlign = TextAlign.Center
                )
            }

            VerticalSpacer(height = lib.toolkit.base.R.dimen.default_margin_large)

            SubtitleBlock(textState = subtitleState, errorState = errorState)
            VerticalSpacer(height = lib.toolkit.base.R.dimen.default_margin_xxlarge)

            IndicatorsRowBlock(
                errorState = errorState,
                enteredCodeState = enteredCode
            )

            VerticalSpacer(height = lib.toolkit.base.R.dimen.default_margin_xxxlarge)
            LazyVerticalGrid(
                modifier = Modifier.padding(horizontal = 16.dp),
                columns = GridCells.Fixed(3),
                userScrollEnabled = false,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                items((1..9).toList()) { number ->
                    KeyboardButton(
                        visible = true,
                        onClick = { onButtonPress(number) }
                    ) {
                        Text(
                            text = "$number",
                            fontWeight = FontWeight.W400,
                            fontSize = 34.sp,
                            color = MaterialTheme.colors.onBackground
                        )
                    }
                }
                item {
                    if (state.passcodeLockState.fingerprintEnabled) {
                        KeyboardButton(
                            visible = true,
                            onClick = {
                                if (!buttonPressLocking) {
                                    onFingerprintClick.invoke()
                                }
                            }
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_fingerprint),
                                contentDescription = "fingerprint_icon",
                                tint = MaterialTheme.colors.primary,
                                modifier = Modifier.size(48.dp)
                            )
                        }
                    }
                }
                item {
                    KeyboardButton(
                        visible = true,
                        onClick = { onButtonPress(0) }
                    ) {
                        Text(
                            text = "0",
                            fontWeight = FontWeight.W400,
                            fontSize = 34.sp,
                            color = MaterialTheme.colors.onBackground
                        )
                    }
                }
                item {
                    KeyboardButton(
                        visible = true,
                        onClick = {
                            if (!buttonPressLocking) {
                                enteredCode.value = enteredCode.value.dropLast(1)
                                errorState.value = false
                            }
                        }
                    ) {
                        Icon(
                            imageVector = ImageVector.vectorResource(id = R.drawable.ic_backspace),
                            contentDescription = "backspace_icon",
                            tint = MaterialTheme.colors.colorTextSecondary,
                            modifier = Modifier.size(48.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SubtitleBlock(textState: MutableState<String>, errorState: MutableState<Boolean>) {
    AnimatedContent(
        targetState = textState.value,
        transitionSpec = { fadeIn().togetherWith(fadeOut()) },
        label = "animated_text_subtitle"
    ) { text ->
        val color = if (errorState.value) MaterialTheme.colors.error else MaterialTheme.colors.colorTextSecondary
        Text(
            text = text,
            style = MaterialTheme.typography.body1,
            color = color,
            textAlign = TextAlign.Center,
            minLines = 2
        )
    }
}

@Composable
private fun IndicatorsRowBlock(enteredCodeState: MutableState<String>, errorState: MutableState<Boolean>) {
    LazyRow(
        modifier = Modifier.fillMaxWidth(0.33f),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        items(MAX_PASSCODE_LENGTH) {
            val dotColor = when {
                errorState.value -> MaterialTheme.colors.error
                it < enteredCodeState.value.length -> MaterialTheme.colors.primary
                else -> MaterialTheme.colors.colorTextTertiary
            }

            Icon(
                imageVector = ImageVector.vectorResource(id = R.drawable.ic_passcode_dot),
                contentDescription = "filled_dot_$it",
                tint = dotColor
            )
        }
    }
}

@Composable
private fun KeyboardButton(
    visible: Boolean,
    onClick: () -> Unit,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = Modifier.aspectRatio(1.5f),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .clip(CircleShape)
                .aspectRatio(1f)
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center,
            content = content
        )
    }
}

@Preview
@Previews.Tablet
@Composable
private fun Preview() {
    ManagerTheme {
        Scaffold {
            Surface(modifier = Modifier.padding(it)) {
                PasscodeOperationScreen(
                    encryptedPasscode = null,
                    initialState = PasscodeOperationState(PasscodeOperationMode.ChangePasscode, PasscodeLockState()),
                    onFingerprintClick = {},
                    onConfirmSuccess = {}
                )
            }
        }
    }
}
