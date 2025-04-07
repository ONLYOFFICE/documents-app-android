package app.editors.manager.ui.compose.passcode

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import app.editors.manager.viewModels.main.PasscodeViewModel


private sealed class PasscodeScreen(val route: String) {
    data object Main : PasscodeScreen("main")
    data object Unlock : PasscodeScreen("unlock")
    data object Set : PasscodeScreen("set")
    data object Reset : PasscodeScreen("reset")
    data object Change : PasscodeScreen("change")
}

@Composable
fun PasscodeMainScreen(
    viewModel: PasscodeViewModel,
    enterPasscodeKey: Boolean,
    onFingerprintClick: () -> Unit = {},
    onSuccess: () -> Unit = {},
    onBackClick: () -> Unit = {}
) {
    val navController = rememberNavController()
    val passcodeLockState by viewModel.passcodeState.collectAsState()

    BackHandler(onBack = onBackClick)
    NavHost(
        navController = navController,
        startDestination = if (passcodeLockState.enabled && enterPasscodeKey)
            PasscodeScreen.Unlock.route else
            PasscodeScreen.Main.route
    ) {
        composable(route = PasscodeScreen.Main.route) {
            PasscodeSettingScreen(
                passcodeLockState = passcodeLockState,
                onPasscodeEnable = { enabled ->
                    if (enabled) {
                        navController.navigate(PasscodeScreen.Set.route)
                    } else {
                        navController.navigate(PasscodeScreen.Reset.route)
                    }
                },
                onFingerprintEnable = viewModel::onFingerprintEnable,
                onChangePassword = { navController.navigate(PasscodeScreen.Change.route) }
            )
        }
        composable(route = PasscodeScreen.Unlock.route) {
            PasscodeOperationScreen(
                encryptedPasscode = passcodeLockState.passcode,
                initialState = PasscodeOperationState(
                    mode = PasscodeOperationMode.UnlockApp,
                    passcodeLockState = passcodeLockState
                ),
                onConfirmSuccess = {
                    viewModel.onDisablingReset()
                    onSuccess.invoke()
                },
                onConfirmFailed = viewModel::onFailedConfirm,
                onFingerprintClick = onFingerprintClick
            )
        }
        composable(route = PasscodeScreen.Set.route) {
            PasscodeOperationScreen(
                initialState = PasscodeOperationState(
                    mode = PasscodeOperationMode.SetPasscode,
                    passcodeLockState = passcodeLockState
                ),
                onConfirmSuccess = { passcode ->
                    viewModel.setPasscode(passcode)
                    onSuccess.invoke()
                }
            )
        }
        composable(route = PasscodeScreen.Change.route) {
            PasscodeOperationScreen(
                encryptedPasscode = passcodeLockState.passcode,
                initialState = PasscodeOperationState(
                    mode = PasscodeOperationMode.ChangePasscode,
                    passcodeLockState = passcodeLockState
                ),
                onConfirmFailed = viewModel::onFailedConfirm,
                onConfirmSuccess = { passcode ->
                    viewModel.onDisablingReset()
                    viewModel.setPasscode(passcode)
                    onSuccess.invoke()
                }
            )
        }
        composable(route = PasscodeScreen.Reset.route) {
            PasscodeOperationScreen(
                encryptedPasscode = passcodeLockState.passcode,
                initialState = PasscodeOperationState(
                    mode = PasscodeOperationMode.ResetPasscode,
                    passcodeLockState = passcodeLockState
                ),
                onConfirmFailed = viewModel::onFailedConfirm,
                onConfirmSuccess = {
                    viewModel.onDisablingReset()
                    viewModel.resetPasscode()
                    onSuccess.invoke()
                }
            )
        }
    }
}