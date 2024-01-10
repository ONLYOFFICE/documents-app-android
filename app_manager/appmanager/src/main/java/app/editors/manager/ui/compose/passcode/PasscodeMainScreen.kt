package app.editors.manager.ui.compose.passcode

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import app.editors.manager.managers.tools.PreferenceTool
import app.editors.manager.viewModels.main.SetPasscodeViewModel


sealed class PasscodeScreen(val route: String) {
    data object Main : PasscodeScreen("main")
    data object Unlock : PasscodeScreen("unlock")
    data object Set : PasscodeScreen("set")
    data object Reset : PasscodeScreen("reset")
    data object Change : PasscodeScreen("change")
}

@Composable
fun PasscodeMainScreen(
    preferenceTool: PreferenceTool,
    enterPasscodeKey: Boolean,
    onFingerprintClick: () -> Unit,
    onSuccess: (PasscodeOperationMode) -> Unit,
    onBackClick: () -> Unit
) {
    val navController = rememberNavController()
    val viewModel = viewModel { SetPasscodeViewModel(preferenceTool) }
    val passcodeState by viewModel.passcode.collectAsState()

    BackHandler(onBack = onBackClick)
    NavHost(
        navController = navController,
        startDestination = if (!passcodeState.isNullOrEmpty() && enterPasscodeKey)
            PasscodeScreen.Unlock.route else
            PasscodeScreen.Main.route
    ) {
        composable(route = PasscodeScreen.Main.route) {
            PasscodeSettingScreen(
                passcodeEnabled = !passcodeState.isNullOrEmpty(),
                fingerprintEnabled = preferenceTool.isFingerprintEnable,
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
                encryptedPasscode = preferenceTool.passcode,
                initialState = PasscodeOperationState(
                    mode = PasscodeOperationMode.UnlockApp,
                    fingerprintEnabled = preferenceTool.isFingerprintEnable && enterPasscodeKey
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
                encryptedPasscode = preferenceTool.passcode,
                initialState = PasscodeOperationState(mode = PasscodeOperationMode.ChangePasscode),
                onConfirmSuccess = { mode, passcode ->
                    viewModel.setPasscode(passcode)
                    onSuccess.invoke(mode)
                }
            )
        }
        composable(route = PasscodeScreen.Reset.route) {
            PasscodeOperationScreen(
                encryptedPasscode = preferenceTool.passcode,
                initialState = PasscodeOperationState(mode = PasscodeOperationMode.ResetPasscode),
                onConfirmSuccess = { mode, _ ->
                    viewModel.resetPasscode()
                    onSuccess.invoke(mode)
                }
            )
        }
    }
}