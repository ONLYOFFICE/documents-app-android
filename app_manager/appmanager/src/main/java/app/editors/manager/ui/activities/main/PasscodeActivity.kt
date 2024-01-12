package app.editors.manager.ui.activities.main

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.LaunchedEffect
import app.editors.manager.R
import app.editors.manager.app.App
import app.editors.manager.managers.tools.PreferenceTool
import app.editors.manager.managers.utils.BiometricsUtils
import app.editors.manager.ui.activities.base.BaseAppActivity
import app.editors.manager.ui.compose.passcode.PasscodeMainScreen
import app.editors.manager.ui.compose.passcode.PasscodeOperationMode
import app.editors.manager.viewModels.main.PasscodeViewModel
import app.editors.manager.viewModels.main.PasscodeViewModelFactory
import lib.compose.ui.theme.ManagerTheme
import javax.inject.Inject


class PasscodeActivity : BaseAppActivity() {

    companion object {
        val TAG: String = PasscodeActivity::class.java.simpleName

        fun show(context: Context) {
            context.startActivity(Intent(context, PasscodeActivity::class.java))
        }
    }

    @Inject
    lateinit var preferenceTool: PreferenceTool

    private val passcodeViewModel by viewModels<PasscodeViewModel> {
        PasscodeViewModelFactory(preferenceTool = preferenceTool)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        App.getApp().appComponent.inject(this)

        setContent {
            ManagerTheme {
                LaunchedEffect(Unit) {
                    if (preferenceTool.isFingerprintEnable) {
                        onShowBiometric()
                    }
                }

                PasscodeMainScreen(
                    viewModel = passcodeViewModel,
                    enterPasscodeKey = true,
                    onSuccess = { mode ->
                        if (mode is PasscodeOperationMode.UnlockApp) {
                            MainActivity.show(this, true)
                            finish()
                        }
                    },
                    onFingerprintClick = ::onShowBiometric,
                    onBackClick = ::finish
                )
            }
        }
    }

    private fun onShowBiometric() {
        BiometricsUtils.biometricAuthenticate(
            promtInfo = BiometricsUtils.initBiometricDialog(
                title = getString(R.string.app_settings_passcode_fingerprint_title),
                negative = getString(lib.editors.gbase.R.string.common_cancel)
            ),
            fragment = this,
            onSuccess = {
                MainActivity.show(this, true)
                finish()
            },
            onError = ::onBiometricError
        )
    }

    private fun onBiometricError(errorMessage: String) {
        showSnackBar(errorMessage)
    }
}