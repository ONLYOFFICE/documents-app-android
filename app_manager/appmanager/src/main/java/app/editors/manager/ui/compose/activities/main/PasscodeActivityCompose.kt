package app.editors.manager.ui.compose.activities.main

import android.os.Handler
import android.os.Looper
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import app.editors.manager.R
import app.editors.manager.managers.tools.PreferenceTool
import app.editors.manager.managers.utils.BiometricsUtils
import app.editors.manager.ui.activities.main.MainActivity
import app.editors.manager.ui.activities.main.PasscodeActivity
import app.editors.manager.ui.compose.fragments.main.PasscodeLock
import app.editors.manager.ui.compose.fragments.main.PasscodeOperation
import app.editors.manager.viewModels.base.SetPasscodeViewModelFactory
import app.editors.manager.viewModels.main.PasscodeLockState
import app.editors.manager.viewModels.main.SetPasscodeViewModel


enum class PasscodeScreens(val screen: String){
    Common("common"),
    SetPasscode("set"),
    ConfirmPasscode("confirm"),
    DisablePasscode("disable"),
    EnterPasscode("enter"),
    ChangePasscode("change")
}

@Composable
fun PasscodeActivity(
    preferenceTool: PreferenceTool,
    backPressed: () -> Unit
) {

    val navController = rememberNavController()
    val activity = LocalContext.current as PasscodeActivity
    val isEnterPasscode = activity.isEnterPasscode

    val viewModel: SetPasscodeViewModel = viewModel( factory = SetPasscodeViewModelFactory(preferenceTool = preferenceTool) )

    viewModel.getData()

    NavHost(
        navController = navController,
        startDestination = if(!isEnterPasscode) PasscodeScreens.Common.screen else PasscodeScreens.EnterPasscode.screen,
    ) {
        composable(PasscodeScreens.EnterPasscode.screen) {
            EnterPasscode(navController = navController, viewModel)
        }
        composable(PasscodeScreens.Common.screen) {
            CommonPasscode(navController = navController, viewModel, backPressed)
        }
        composable(PasscodeScreens.SetPasscode.screen) {
            SetPasscode(navController = navController, viewModel)
        }
        composable(PasscodeScreens.ConfirmPasscode.screen) {
            ConfirmPasscode(navController = navController, viewModel)
        }
        composable(PasscodeScreens.ChangePasscode.screen) {
            ChangePasscode(navController = navController, viewModel)
        }
        composable(PasscodeScreens.DisablePasscode.screen) {
            DisablePasscode(navController = navController, viewModel)
        }
    }
}

@Composable
private fun EnterPasscode(
    navController: NavController,
    viewModel: SetPasscodeViewModel
) {

    val fingerprintTitle = stringResource(R.string.app_settings_passcode_fingerprint_title)
    val fingerprintCancelButton = stringResource(R.string.dialogs_common_cancel_button)
    val errorMessage = stringResource(R.string.app_settings_passcode_change_disable_error)
    val context = LocalContext.current as PasscodeActivity

    viewModel.biometric.observe(context) { isBiometric ->
        if (isBiometric) {
            BiometricsUtils.biometricAuthenticate(
                BiometricsUtils.initBiometricDialog(
                    title = fingerprintTitle,
                    negative = fingerprintCancelButton
                ),
                context,
                {
                    MainActivity.show(context, true)
                    context.finish()
                }) { errorMessage ->
                context.onBiometricError(errorMessage)
                viewModel.biometric.value = false
            }
        }
    }

    LaunchedEffect(key1 = null, block = {
        if (viewModel.isFingerprintEnable.value == true) {
            viewModel.biometric.value = true
        }
    })

    PasscodeOperation(
        viewModel = viewModel,
        title = stringResource(R.string.app_settings_passscode_enter_full_title),
        isEnterCodeFragment = true,
        onEnterCode = { codeDigit ->
            viewModel.checkConfirmPasscode(
                codeDigit.toString(),
                errorMessage
            )
        })
    { state ->
        when(state) {
            is PasscodeLockState.ConfirmPasscode -> {
                viewModel.setNullState()
                MainActivity.show(context, true)
                context.finish()
            }
            is PasscodeLockState.Error -> {
                Handler(Looper.getMainLooper()).postDelayed({
                    viewModel.resetErrorState()
                    navController.navigate(PasscodeScreens.EnterPasscode.screen) {
                        navController.graph.startDestinationRoute?.let {
                            popUpTo(it) {
                                saveState = true
                            }
                        }
                        launchSingleTop = true
                    }
                }, 1000)
            }

            else -> {
                // Stub
            }
        }
    }
}


@Composable
private fun CommonPasscode(
    navController: NavController,
    viewModel: SetPasscodeViewModel,
    backPressed: () -> Unit
) {
    PasscodeLock(
        viewModel = viewModel,
        navController = navController,
        backPressed = backPressed
    )
}

@Composable
private fun SetPasscode(
    navController: NavController,
    viewModel: SetPasscodeViewModel
) {
    viewModel.resetConfirmCode()

    PasscodeOperation(
        viewModel = viewModel,
        title = stringResource(R.string.app_settings_passcode_enter_title),
        subtitle = stringResource(R.string.app_settings_passcode_enter_subtitle),
        isConfirmCode = false,
        onEnterCode = { codeDigit ->
            viewModel.checkPasscode(codeDigit.toString())
        }) { state ->
        when (state) {
            is PasscodeLockState.SetPasscode -> {
                viewModel.setNullState()
                Handler(Looper.getMainLooper()).postDelayed({
                    navController.navigate(PasscodeScreens.ConfirmPasscode.screen) {
                        navController.graph.startDestinationRoute?.let {
                            popUpTo(it) {
                                saveState = true
                            }
                        }
                        launchSingleTop = true
                    }
                }, 300)

            }
            else -> {
                // Stub
            }
        }
    }
}


@Composable
private fun ConfirmPasscode(
    navController: NavController,
    viewModel: SetPasscodeViewModel
) {
    viewModel.resetConfirmCode()
    val errorMessage = stringResource(R.string.app_settings_passcode_confirm_error)

    PasscodeOperation(
        viewModel = viewModel,
        title = stringResource(R.string.app_settings_passcode_confirm_title),
        subtitle = stringResource(R.string.app_settings_passcode_confirm_subtitle),
        onEnterCode = { codeDigit ->
            viewModel.checkConfirmPasscode(codeDigit.toString(), errorMessage)
        }) { state ->
        when(state) {
            is PasscodeLockState.ConfirmPasscode -> {
                viewModel.setPasscodeLockState(true)
                viewModel.setPasscode()
                viewModel.setNullState()
                navController.navigate(PasscodeScreens.Common.screen) {
                    navController.graph.startDestinationRoute?.let {
                        popUpTo(it) {
                            saveState = true
                        }
                    }
                    launchSingleTop = true
                }
            }
            is PasscodeLockState.Error -> {
                Handler(Looper.getMainLooper()).postDelayed( {
                    viewModel.resetErrorState()
                    navController.navigate(PasscodeScreens.SetPasscode.screen) {
                        navController.graph.startDestinationRoute?.let {
                            popUpTo(it) {
                                saveState = true
                            }
                        }
                        launchSingleTop = true
                    }
                }, 1000)
            }
            else -> {
                // Stub
            }
        }
    }
}

@Composable
private fun ChangePasscode(
    navController: NavController,
    viewModel: SetPasscodeViewModel
) {
    viewModel.resetConfirmCode()
    val errorMessage = stringResource(R.string.app_settings_passcode_change_disable_error)

    PasscodeOperation(
        viewModel = viewModel,
        title = stringResource(R.string.app_settings_passcode_change_disable_title),
        onEnterCode = { codeDigit ->
            viewModel.checkConfirmPasscode(codeDigit.toString(), errorMessage)
        }) { state ->
        when(state) {
            is PasscodeLockState.ConfirmPasscode -> {
                viewModel.setNullState()
                navController.navigate(PasscodeScreens.SetPasscode.screen) {
                    navController.graph.startDestinationRoute?.let {
                        popUpTo(it) {
                            saveState = true
                        }
                    }
                    launchSingleTop = true
                }
            }
            is PasscodeLockState.Error -> {
                Handler(Looper.getMainLooper()).postDelayed( {
                    viewModel.resetErrorState()
                    navController.navigate(PasscodeScreens.ChangePasscode.screen) {
                        navController.graph.startDestinationRoute?.let {
                            popUpTo(it) {
                                saveState = true
                            }
                        }
                        launchSingleTop = true
                    }
                }, 1000)
            }
            else -> {
                // Stub
            }
        }
    }
}

@Composable
private fun DisablePasscode(
    navController: NavController,
    viewModel: SetPasscodeViewModel
) {
    viewModel.resetConfirmCode()
    val errorMessage = stringResource(R.string.app_settings_passcode_change_disable_error)

    PasscodeOperation(
        viewModel = viewModel,
        title = stringResource(R.string.app_settings_passcode_change_disable_title),
        onEnterCode = { codeDigit ->
            viewModel.checkConfirmPasscode(codeDigit.toString(), errorMessage)
        }) { state ->
        when(state) {
            is PasscodeLockState.ConfirmPasscode -> {
                viewModel.setPasscodeLockState(false)
                viewModel.setFingerprintState(false)
                viewModel.setNullState()
                viewModel.resetConfirmCode()
                viewModel.setPasscode()
                navController.navigate(PasscodeScreens.Common.screen) {
                    navController.graph.startDestinationRoute?.let {
                        popUpTo(it) {
                            saveState = true
                        }
                    }
                    launchSingleTop = true
                }
            }
            is PasscodeLockState.Error -> {
                Handler(Looper.getMainLooper()).postDelayed( {
                    viewModel.resetErrorState()
                    navController.navigate(PasscodeScreens.DisablePasscode.screen) {
                        navController.graph.startDestinationRoute?.let {
                            popUpTo(it) {
                                saveState = true
                            }
                        }
                        launchSingleTop = true
                    }
                }, 1000)
            }
            else -> {
                // Stub
            }
        }
    }
}