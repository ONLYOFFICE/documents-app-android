package app.editors.manager.viewModels.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import app.editors.manager.managers.tools.PreferenceTool
import app.editors.manager.managers.utils.KeyStoreUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class PasscodeState(
    val passcode: String?,
    val fingerprintEnabled: Boolean
)

class PasscodeViewModelFactory(private val preferenceTool: PreferenceTool) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return if (modelClass.isAssignableFrom(PasscodeViewModel::class.java)) {
            PasscodeViewModel(preferenceTool) as T
        } else {
            throw IllegalArgumentException("ViewModel Not Found")
        }
    }
}

class PasscodeViewModel(private val preferenceTool: PreferenceTool) : ViewModel() {

    private val _passcodeState: MutableStateFlow<PasscodeState> = MutableStateFlow(
        PasscodeState(
            passcode = preferenceTool.passcode,
            fingerprintEnabled = preferenceTool.isFingerprintEnable
        )
    )
    val passcodeState: StateFlow<PasscodeState> = _passcodeState.asStateFlow()

    fun setPasscode(passcode: String) {
        val encryptedPasscode = KeyStoreUtils.encryptData(passcode)
        preferenceTool.passcode = encryptedPasscode
        _passcodeState.update { it.copy(passcode = encryptedPasscode) }
    }

    fun resetPasscode() {
        preferenceTool.passcode = null
        _passcodeState.update { it.copy(passcode = null) }
    }

    fun onFingerprintEnable(enabled: Boolean) {
        preferenceTool.isFingerprintEnable = enabled
        _passcodeState.update { it.copy(fingerprintEnabled = enabled) }
    }
}