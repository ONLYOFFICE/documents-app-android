package app.editors.manager.viewModels.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import app.editors.manager.managers.tools.PreferenceTool
import app.editors.manager.managers.utils.KeyStoreUtils
import app.editors.manager.mvp.models.states.PasscodeLockState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

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

    private val _passcodeState: MutableStateFlow<PasscodeLockState> = MutableStateFlow(
        PasscodeLockState(
            passcode = preferenceTool.passcodeLock.passcode,
            fingerprintEnabled = preferenceTool.passcodeLock.fingerprintEnabled
        )
    )
    val passcodeState: StateFlow<PasscodeLockState> = _passcodeState.asStateFlow()

    fun setPasscode(passcode: String) {
        val encryptedPasscode = KeyStoreUtils.encryptData(passcode)
        preferenceTool.passcodeLock = preferenceTool.passcodeLock.copy(passcode = encryptedPasscode)
        _passcodeState.update { it.copy(passcode = encryptedPasscode) }
    }

    fun resetPasscode() {
        preferenceTool.passcodeLock = PasscodeLockState()
        _passcodeState.update { it.copy(passcode = null) }
    }

    fun onFingerprintEnable(enabled: Boolean) {
        preferenceTool.passcodeLock = preferenceTool.passcodeLock.copy(fingerprintEnabled = enabled)
        _passcodeState.update { it.copy(fingerprintEnabled = enabled) }
    }
}