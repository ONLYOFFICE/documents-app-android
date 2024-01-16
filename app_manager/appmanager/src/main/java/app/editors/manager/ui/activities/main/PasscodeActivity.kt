package app.editors.manager.ui.activities.main

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import app.editors.manager.R
import app.editors.manager.app.appComponent
import app.editors.manager.managers.utils.BiometricsUtils
import app.editors.manager.ui.activities.base.BaseAppActivity
import app.editors.manager.ui.compose.passcode.PasscodeMainScreen
import app.editors.manager.viewModels.main.PasscodeViewModel
import app.editors.manager.viewModels.main.PasscodeViewModelFactory
import lib.compose.ui.theme.ManagerTheme


class PasscodeActivity : BaseAppActivity() {

    companion object {
        val TAG: String = PasscodeActivity::class.java.simpleName

        fun show(context: Context) {
            context.startActivity(Intent(context, PasscodeActivity::class.java))
        }
    }

    private val passcodeViewModel by viewModels<PasscodeViewModel> {
        PasscodeViewModelFactory(preferenceTool = appComponent.preference)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ManagerTheme {
                PasscodeMainScreen(
                    viewModel = passcodeViewModel,
                    enterPasscodeKey = true,
                    onSuccess = ::onSuccessUnlock,
                    onFingerprintClick = ::onShowBiometric,
                )
            }
        }
    }

    override fun onStart() {
        super.onStart()
        with(appComponent.preference.passcodeLock) {
            if (fingerprintEnabled && !manyAttemptsLock) {
                onShowBiometric()
            }
        }
    }

    private fun onSuccessUnlock() {
        setResult(RESULT_OK)
        finish()
    }

    private fun onShowBiometric() {
        BiometricsUtils.biometricAuthenticate(
            promptInfo = BiometricsUtils.initBiometricDialog(
                title = getString(R.string.app_settings_passcode_fingerprint_title),
                negative = getString(lib.editors.gbase.R.string.common_cancel)
            ),
            fragmentActivity = this,
            onSuccess = ::onSuccessUnlock,
            onError = ::onBiometricError
        )
    }

    private fun onBiometricError(errorMessage: String) {
        showSnackBar(errorMessage)
    }
}