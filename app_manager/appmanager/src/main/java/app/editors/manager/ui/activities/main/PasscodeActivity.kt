package app.editors.manager.ui.activities.main

import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import app.editors.manager.R
import app.editors.manager.app.App
import app.editors.manager.managers.utils.BiometricsUtils
import app.editors.manager.ui.activities.base.BaseAppActivity
import app.editors.manager.ui.compose.fragments.main.PasscodeFragmentCompose
import app.editors.manager.ui.compose.fragments.main.PasscodeOperationCompose
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

class PasscodeActivity: BaseAppActivity() {

    companion object {
        val TAG = PasscodeActivity::class.java.simpleName
        const val ENTER_PASSCODE_KEY = "ENTER_PASSCODE_KEY"


        fun show(context: Context, isEnterPasscode: Boolean = false) {
            context.startActivity(Intent(context, PasscodeActivity::class.java).apply {
                putExtra(ENTER_PASSCODE_KEY, isEnterPasscode)
            })
        }
    }

    private val viewModel by viewModels<SetPasscodeViewModel>()

    private var isEnterPasscode = false

    @ExperimentalFoundationApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isEnterPasscode = intent.getBooleanExtra(ENTER_PASSCODE_KEY, false)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        App.getApp().appComponent.inject(viewModel)
        viewModel.getData()

        setContent {
            PasscodeActivity(this)
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }

    @ExperimentalFoundationApi
    @Composable
    private fun PasscodeActivity(activity: AppCompatActivity) {
        val navController = rememberNavController()
        NavHost(
            navController = navController,
            startDestination = if(!isEnterPasscode) PasscodeScreens.Common.screen else PasscodeScreens.EnterPasscode.screen,
        ) {
            composable(PasscodeScreens.EnterPasscode.screen) {
                enterPasscode(activity = activity, navController = navController)
            }
            composable(PasscodeScreens.Common.screen) {
                commonPasscode(navController = navController)
            }
            composable(PasscodeScreens.SetPasscode.screen) {
                setPasscode(navController = navController)
            }
            composable(PasscodeScreens.ConfirmPasscode.screen) {
                confirmPasscode(navController = navController)
            }
            composable(PasscodeScreens.ChangePasscode.screen) {
                changePasscode(navController = navController)
            }
            composable(PasscodeScreens.DisablePasscode.screen) {
                disablePasscode(navController = navController)
            }
        }
    }

    @ExperimentalFoundationApi
    @Composable
    private fun enterPasscode(activity: AppCompatActivity, navController: NavController) {
        viewModel.biometric.observe(activity) { isBiometric ->
            if (isBiometric) {
                BiometricsUtils.biometricAuthenticate(
                    BiometricsUtils.initBiometricDialog(
                        title = getString(R.string.app_settings_passcode_fingerprint_title),
                        negative = getString(R.string.dialogs_common_cancel_button)
                    ),
                    activity,
                    {
                        onBiometricSuccess()
                    }) { errorMessage ->
                    onBiometricError(errorMessage)
                }
            }
        }
        PasscodeOperationCompose.PasscodeOperation(
            viewModel = viewModel,
            title = getString(R.string.app_settings_passscode_enter_full_title),
            isEnterCodeFragment = true,
            onEnterCode = { codeDigit ->
                viewModel.checkConfirmPasscode(
                    codeDigit.toString(),
                    getString(R.string.app_settings_passcode_change_disable_error)
                )
            })
        { state ->
            when(state) {
                is PasscodeLockState.ConfirmPasscode -> {
                    viewModel.setNullState()
                    MainActivity.show(activity)
                    finish()
                }
                is PasscodeLockState.Error -> {
                    Handler(Looper.getMainLooper()).postDelayed({
                        viewModel.resetErrorState()
                        navController.navigate(PasscodeScreens.EnterPasscode.screen)
                    }, 1000)
                }

            }
        }
    }


    @Composable
    private fun commonPasscode(navController: NavController) {
        PasscodeFragmentCompose.PasscodeLock(
            viewModel = viewModel,
            navController = navController
        )
    }

    @ExperimentalFoundationApi
    @Composable
    private fun setPasscode(navController: NavController) {
        viewModel.resetConfirmCode()
        PasscodeOperationCompose.PasscodeOperation(
            viewModel = viewModel,
            title = getString(R.string.app_settings_passcode_enter_title),
            subtitle = getString(R.string.app_settings_passcode_enter_subtitle),
            isConfirmCode = false,
            onEnterCode = { codeDigit ->
                viewModel.checkPasscode(codeDigit.toString())
            }) { state ->
            when (state) {
                is PasscodeLockState.SetPasscode -> {
                    viewModel.setNullState()
                    Handler(Looper.getMainLooper()).postDelayed({
                        navController.navigate(PasscodeScreens.ConfirmPasscode.screen)
                    }, 300)

                }
            }
        }
    }


    @ExperimentalFoundationApi
    @Composable
    private fun confirmPasscode(navController: NavController) {
        viewModel.resetConfirmCode()
        PasscodeOperationCompose.PasscodeOperation(
            viewModel = viewModel,
            title = getString(R.string.app_settings_passcode_confirm_title),
            subtitle = getString(R.string.app_settings_passcode_confirm_subtitle),
            onEnterCode = { codeDigit ->
                viewModel.checkConfirmPasscode(codeDigit.toString(), getString(R.string.app_settings_passcode_confirm_error))
            }) { state ->
            when(state) {
                is PasscodeLockState.ConfirmPasscode -> {
                    viewModel.setPasscodeLockState(true)
                    viewModel.setPasscode()
                    viewModel.setNullState()
                    navController.navigate(PasscodeScreens.Common.screen)
                }
                is PasscodeLockState.Error -> {
                    Handler(Looper.getMainLooper()).postDelayed( {
                        viewModel.resetErrorState()
                        navController.navigate(PasscodeScreens.SetPasscode.screen)
                    }, 1000)
                }
            }
        }
    }

    @ExperimentalFoundationApi
    @Composable
    private fun changePasscode(navController: NavController) {
        viewModel.resetConfirmCode()
        PasscodeOperationCompose.PasscodeOperation(
            viewModel = viewModel,
            title = getString(R.string.app_settings_passcode_change_disable_title),
            onEnterCode = { codeDigit ->
                viewModel.checkConfirmPasscode(codeDigit.toString(), getString(R.string.app_settings_passcode_change_disable_error))
            }) { state ->
            when(state) {
                is PasscodeLockState.ConfirmPasscode -> {
                    viewModel.setNullState()
                    navController.navigate(PasscodeScreens.SetPasscode.screen)
                }
                is PasscodeLockState.Error -> {
                    Handler(Looper.getMainLooper()).postDelayed( {
                        viewModel.resetErrorState()
                        navController.navigate(PasscodeScreens.ChangePasscode.screen)
                    }, 1000)
                }
            }
        }
    }

    @ExperimentalFoundationApi
    @Composable
    private fun disablePasscode(navController: NavController) {
        viewModel.resetConfirmCode()
        PasscodeOperationCompose.PasscodeOperation(
            viewModel = viewModel,
            title = getString(R.string.app_settings_passcode_change_disable_title),
            onEnterCode = { codeDigit ->
                viewModel.checkConfirmPasscode(codeDigit.toString(), getString(R.string.app_settings_passcode_change_disable_error))
            }) { state ->
            when(state) {
                is PasscodeLockState.ConfirmPasscode -> {
                    viewModel.setPasscodeLockState(false)
                    viewModel.setFingerprintState(false)
                    viewModel.setNullState()
                    viewModel.resetConfirmCode()
                    viewModel.setPasscode()
                    navController.navigate(PasscodeScreens.Common.screen)
                }
                is PasscodeLockState.Error -> {
                    Handler(Looper.getMainLooper()).postDelayed( {
                        viewModel.resetErrorState()
                        navController.navigate(PasscodeScreens.DisablePasscode.screen)
                    }, 1000)
                }
            }
        }
    }

    private fun onBiometricSuccess() {
        MainActivity.show(this)
        finish()
    }

    private fun onBiometricError(errorMsg: String) {
        showSnackBar(errorMsg)
        viewModel.biometric.value = false
    }

}