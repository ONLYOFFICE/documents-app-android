package app.editors.manager.ui.activities.main

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import app.editors.manager.R
import app.editors.manager.app.App
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

        fun show(context: Context) {
            context.startActivity(Intent(context, PasscodeActivity::class.java))
        }
    }

    private val viewModel by viewModels<SetPasscodeViewModel>()

    @ExperimentalFoundationApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        App.getApp().appComponent.inject(viewModel)

        viewModel.getData()

        setContent {
            PasscodeActivity()
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }

    @ExperimentalFoundationApi
    @Composable
    private fun PasscodeActivity() {
        val navController = rememberNavController()
        NavHost(
            navController = navController,
            startDestination = PasscodeScreens.Common.screen,
        ) {
            composable(PasscodeScreens.Common.screen) {
                PasscodeFragmentCompose.PasscodeLock(
                    viewModel = viewModel,
                    navController = navController
                )
            }
            composable(PasscodeScreens.SetPasscode.screen) {
                PasscodeOperationCompose.PasscodeOperation(viewModel,getString(R.string.app_settings_passcode_enter_title), getString(R.string.app_settings_passcode_enter_subtitle), { codeDigit ->
                    viewModel.checkPasscode(codeDigit.toString())
                }) { state ->
                    when(state) {
                        is PasscodeLockState.SetPasscode -> {
                            viewModel.setNullState()
                            Handler(Looper.getMainLooper()).postDelayed({
                                navController.navigate(PasscodeScreens.ConfirmPasscode.screen)
                            }, 300)

                        }
                    }
                }
            }
            composable(PasscodeScreens.ConfirmPasscode.screen) {
                PasscodeOperationCompose.PasscodeOperation(viewModel, getString(R.string.app_settings_passcode_confirm_title), getString(R.string.app_settings_passcode_confirm_subtitle),
                    { codeDigit ->
                        viewModel.checkConfirmPasscode(codeDigit.toString(), getString(R.string.app_settings_passcode_confirm_error))
                    }) { state ->
                    when(state) {
                        is PasscodeLockState.ConfirmPasscode -> {
                            viewModel.setPasscodeLockState(true)
                            viewModel.setNullState()
                            finish()
                        }
                        is PasscodeLockState.Error -> {
                            viewModel.setPasscodeLockState(false)
                            viewModel.setPasscode("")
                            Handler(Looper.getMainLooper()).postDelayed( {
                                viewModel.setNullState()
                                viewModel.setError(false)
                                navController.popBackStack()
                            }, 1500)
                        }
                    }
                }
            }
            composable(PasscodeScreens.ChangePasscode.screen) {
                PasscodeOperationCompose.PasscodeOperation(viewModel, getString(R.string.app_settings_passcode_change_disable_title), "", { codeDigit ->
                    viewModel.checkConfirmPasscode(codeDigit.toString(), getString(R.string.app_settings_passcode_change_disable_error))
                }) { state ->
                    when(state) {
                        is PasscodeLockState.ConfirmPasscode -> {
                            viewModel.setNullState()
                            navController.navigate(PasscodeScreens.SetPasscode.screen)
                        }
                        is PasscodeLockState.Error -> {
                            Handler(Looper.getMainLooper()).postDelayed( {
                                viewModel.setNullState()
                                navController.navigate(PasscodeScreens.ChangePasscode.screen)
                            }, 1500)
                        }
                    }
                }
            }
            composable(PasscodeScreens.DisablePasscode.screen) {
                PasscodeOperationCompose.PasscodeOperation(viewModel,getString(R.string.app_settings_passcode_change_disable_title), "", { codeDigit ->
                    viewModel.checkConfirmPasscode(codeDigit.toString(), getString(R.string.app_settings_passcode_change_disable_error))
                }) { state ->
                    when(state) {
                        is PasscodeLockState.ConfirmPasscode -> {
                            viewModel.setPasscodeLockState(false)
                            viewModel.setPasscode("")
                            viewModel.setNullState()
                            finish()
                        }
                        is PasscodeLockState.Error -> {
                            Handler(Looper.getMainLooper()).postDelayed( {
                                viewModel.setNullState()
                                viewModel.setError(false)
                                navController.navigate(PasscodeScreens.DisablePasscode.screen)
                            }, 1500)
                        }
                    }
                }
            }
        }
    }

}