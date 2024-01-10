package app.editors.manager.ui.compose.passcode

import androidx.compose.animation.Animatable
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
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
import androidx.compose.ui.draw.alpha
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import lib.compose.ui.theme.ManagerTheme
import lib.compose.ui.theme.Previews
import lib.compose.ui.theme.colorTextSecondary
import lib.compose.ui.theme.colorTextTertiary
import lib.compose.ui.views.VerticalSpacer
import lib.toolkit.base.R
import lib.toolkit.base.managers.utils.UiUtils

data class PasscodeOperationState(
    val mode: PasscodeOperationMode,
    val fingerprintEnabled: Boolean
)

sealed class PasscodeOperationMode(
    val title: Int,
    val subtitle: Int?,
    val error: Int?,
    val confirmPassword: String?
) {
    data class UnlockApp(private val password: String) : PasscodeOperationMode(
        title = app.editors.manager.R.string.app_settings_passscode_enter_full_title,
        subtitle = null,
        error = app.editors.manager.R.string.app_settings_passcode_change_disable_error,
        confirmPassword = password
    )

    data class SetPasscode(private val password: String) : PasscodeOperationMode(
        title = app.editors.manager.R.string.app_settings_passcode_enter_title,
        subtitle = app.editors.manager.R.string.app_settings_passcode_enter_subtitle,
        error = null,
        confirmPassword = password
    )

    data object ChangePasscode : PasscodeOperationMode(
        title = app.editors.manager.R.string.app_settings_passcode_confirm_title,
        subtitle = app.editors.manager.R.string.app_settings_passcode_confirm_subtitle,
        error = app.editors.manager.R.string.app_settings_passcode_confirm_error,
        confirmPassword = null
    )


}

private const val MAX_PASSCODE_LENGTH = 4

@Composable
fun PasscodeOperationScreen(
    state: PasscodeOperationState,
    onFingerprintClick: () -> Unit,
    onConfirmSuccess: (PasscodeOperationMode) -> Unit
) {
    val error = remember { mutableStateOf(false) }
    val enteredCode = remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()
    var buttonPressLocking by remember { mutableStateOf(false) }

    fun onButtonPress(number: Int) {
        coroutineScope.launch {
            if (buttonPressLocking) {
                return@launch
            }

            if (error.value) {
                error.value = false
            }

            if (enteredCode.value.length < MAX_PASSCODE_LENGTH) {
                enteredCode.value += "$number"
            }

            if (enteredCode.value.length == MAX_PASSCODE_LENGTH) {
                if (state.mode.confirmPassword != null) {
                    enteredCode.value = ""
                    error.value = state.mode.confirmPassword != enteredCode.value
                    buttonPressLocking = true
                    delay(2000L)
                    buttonPressLocking = false
                    error.value = false
                }
            }
        }
    }

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
            Text(
                text = stringResource(id = state.mode.title),
                style = MaterialTheme.typography.h6,
                color = MaterialTheme.colors.onSurface,
                textAlign = TextAlign.Center
            )

            VerticalSpacer(height = R.dimen.default_margin_large)

            if (!error.value && state.mode.subtitle != null) {
                Text(
                    text = stringResource(id = state.mode.subtitle),
                    style = MaterialTheme.typography.body1,
                    color = MaterialTheme.colors.colorTextSecondary,
                    textAlign = TextAlign.Center
                )
            }

            AnimatedContent(targetState = error.value, label = "") {
                Text(
                    modifier = Modifier.alpha(if (it) 1f else 0f),
                    text = state.mode.error?.let { stringResource(id = it) }.orEmpty(),
                    style = MaterialTheme.typography.body1,
                    color = MaterialTheme.colors.error,
                    textAlign = TextAlign.Center
                )
            }
            VerticalSpacer(height = R.dimen.default_margin_xxlarge)

            IndicatorsRowBlock(
                errorState = error,
                enteredCodeState = enteredCode
            )

            VerticalSpacer(height = R.dimen.default_margin_xxxlarge)
            LazyVerticalGrid(
                modifier = Modifier.padding(horizontal = 16.dp),
                columns = GridCells.Fixed(3),
                userScrollEnabled = false,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                items(IntRange(1, 9).toList()) { number ->
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
                    KeyboardButton(
                        visible = true,
                        onClick = onFingerprintClick
                    ) {
                        Icon(
                            painter = painterResource(id = app.editors.manager.R.drawable.ic_fingerprint),
                            contentDescription = "fingerprint_icon",
                            tint = MaterialTheme.colors.primary,
                            modifier = Modifier.size(48.dp)
                        )
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
                            enteredCode.value = enteredCode.value.dropLast(1)
                            error.value = false
                        }
                    ) {
                        Icon(
                            imageVector = ImageVector.vectorResource(id = app.editors.manager.R.drawable.ic_backspace),
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
private fun IndicatorsRowBlock(enteredCodeState: MutableState<String>, errorState: MutableState<Boolean>) {
    LazyRow(
        modifier = Modifier.fillMaxWidth(0.33f),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        items(MAX_PASSCODE_LENGTH) {
            val initialColor = MaterialTheme.colors.colorTextTertiary
            val enteredColor = MaterialTheme.colors.primary
            val errorColor = MaterialTheme.colors.error
            val colorAnimation = remember { Animatable(initialColor) }

            LaunchedEffect(enteredCodeState.value, errorState.value) {
                colorAnimation.animateTo(
                    initialVelocity = initialColor,
                    targetValue = when {
                        errorState.value -> errorColor
                        it < enteredCodeState.value.length -> enteredColor
                        else -> initialColor
                    },
                    animationSpec = tween(easing = LinearEasing)
                )
            }

            Icon(
                imageVector = ImageVector.vectorResource(id = app.editors.manager.R.drawable.ic_passcode_dot),
                contentDescription = "filled_dot_$it",
                tint = colorAnimation.value
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
                    state = PasscodeOperationState(
                        PasscodeOperationMode.UnlockApp("1234"),
                        false
                    ),
                    onFingerprintClick = {},
                    onConfirmSuccess = {}
                )
            }
        }
    }
}
