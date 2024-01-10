package app.editors.manager.viewModels.main

import androidx.lifecycle.ViewModel
import app.editors.manager.managers.tools.PreferenceTool
import app.editors.manager.managers.utils.KeyStoreUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow


class SetPasscodeViewModel(private val preferencesTool: PreferenceTool): ViewModel(){

    private val _passcode: MutableStateFlow<String?> = MutableStateFlow(preferencesTool.passcode)
    val passcode: StateFlow<String?> = _passcode.asStateFlow()

    fun setPasscode(passcode: String) {
        preferencesTool.passcode = KeyStoreUtils.encryptData(passcode)
        _passcode.value = preferencesTool.passcode
    }

    fun resetPasscode() {
        preferencesTool.passcode = null
        _passcode.value = null
    }

    fun onFingerprintEnable(enabled: Boolean) {
        preferencesTool.isFingerprintEnable = enabled
    }
}