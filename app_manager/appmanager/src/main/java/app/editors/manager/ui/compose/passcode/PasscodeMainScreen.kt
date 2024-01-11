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
    onSuccess: (PasscodeOperationMode) ->  Unit = {},
    onBackClick: () -> Unit
) {
    val navController = rememberNavController()
    val passcodeState by viewModel.passcodeState.collectAsState()

    BackHandler(onBack = onBackClick)
    NavHost(
        navController = navController,
        startDestination = if (!passcodeState.passcode.isNullOrEmpty() && enterPasscodeKey)
            PasscodeScreen.Unlock.route else
            PasscodeScreen.Main.route
    ) {
        composable(route = PasscodeScreen.Main.route) {
            PasscodeSettingScreen(
                passcodeEnabled = !passcodeState.passcode.isNullOrEmpty(),
                fingerprintEnabled = passcodeState.fingerprintEnabled,
                onPasscodeEnable = { enabled ->
                    if (enabled) {
                        navController.navigate(PasscodeScreen.Set.route)
                    } else {
                        navController.navigate(PasscodeScreen.Reset.route)
                    }
                },
                onFingerprintEnable = viewModel::onFingerprintEnable,
                onChangePassword = { navController.navigate(PasscodeScreen.Change.route) },
                onBackClick = onBackClick,
            )
        }
        composable(route = PasscodeScreen.Unlock.route) {
            PasscodeOperationScreen(
                encryptedPasscode = passcodeState.passcode,
                initialState = PasscodeOperationState(
                    mode = PasscodeOperationMode.UnlockApp,
                    fingerprintEnabled = passcodeState.fingerprintEnabled && enterPasscodeKey
                ),
                onConfirmSuccess = { mode, _ -> onSuccess.invoke(mode) },
                onFingerprintClick = onFingerprintClick
            )
        }
        composable(route = PasscodeScreen.Set.route) {
            PasscodeOperationScreen(
                initialState = PasscodeOperationState(mode = PasscodeOperationMode.SetPasscode),
                onConfirmSuccess = { mode, passcode ->
                    viewModel.setPasscode(passcode)
                    onSuccess.invoke(mode)
                }
            )
        }
        composable(route = PasscodeScreen.Change.route) {
            PasscodeOperationScreen(
                encryptedPasscode = passcodeState.passcode,
                initialState = PasscodeOperationState(mode = PasscodeOperationMode.ChangePasscode),
                onConfirmSuccess = { mode, passcode ->
                    viewModel.setPasscode(passcode)
                    onSuccess.invoke(mode)
                }
            )
        }
        composable(route = PasscodeScreen.Reset.route) {
            PasscodeOperationScreen(
                encryptedPasscode = passcodeState.passcode,
                initialState = PasscodeOperationState(mode = PasscodeOperationMode.ResetPasscode),
                onConfirmSuccess = { mode, _ ->
                    viewModel.resetPasscode()
                    onSuccess.invoke(mode)
                }
            )
        }
    }
}