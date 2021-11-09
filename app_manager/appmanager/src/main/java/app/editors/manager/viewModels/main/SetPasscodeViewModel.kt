package app.editors.manager.viewModels.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import app.editors.manager.managers.tools.PreferenceTool
import app.editors.manager.managers.utils.KeyStoreUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import lib.toolkit.base.managers.utils.SingleLiveEvent
import javax.inject.Inject

sealed class PasscodeLockState() {
    class SetPasscode(): PasscodeLockState()
    class ConfirmPasscode(): PasscodeLockState()
    data class Error(val errorMessage: String): PasscodeLockState()
}


class SetPasscodeViewModel: ViewModel(){

    companion object {
        const val KEY_PASSCODE = "AndroidKeyStore"
    }

    @Inject
    lateinit var preferencesTool: PreferenceTool

    private val _passcode: MutableLiveData<String> = MutableLiveData()
    val passcode: LiveData<String> = _passcode

    private val _passcodeLockState: MutableLiveData<PasscodeLockState> = MutableLiveData()
    val passcodeLockState: LiveData<PasscodeLockState> = _passcodeLockState

    private val _error: MutableLiveData<Boolean> = MutableLiveData()
    val error: LiveData<Boolean> = _error

    private val _errorMessage: MutableLiveData<String> = MutableLiveData()
    val errorMessage: LiveData<String> = _errorMessage

    private val _isFingerprintEnable = MutableLiveData<Boolean>()
    val isFingerprintEnable: LiveData<Boolean> = _isFingerprintEnable

    private val _isPasscodeEnable = MutableLiveData<Boolean>()
    val isPasscodeEnable: LiveData<Boolean> = _isPasscodeEnable

    val biometric = SingleLiveEvent<Boolean>()

    private var code: String = ""

    fun getData() {
        KeyStoreUtils.init()
        CoroutineScope(Dispatchers.Main).launch {
            if(preferencesTool.passcode?.isEmpty() == true)
                preferencesTool.passcode = KeyStoreUtils.encryptData("")
            _passcode.value = preferencesTool.passcode?.let { KeyStoreUtils.decryptData(it) }
        }
        _isPasscodeEnable.value = preferencesTool.isPasscodeLockEnable
        _isFingerprintEnable.value = preferencesTool.isFingerprintEnable
    }

    fun setPasscode(code: String) {
        preferencesTool.passcode = KeyStoreUtils.encryptData(code)
        _passcode.value = KeyStoreUtils.decryptData(preferencesTool.passcode!!)
    }

    fun setError(isError: Boolean) {
        _error.value = isError
    }

    fun setFingerprintState(isEnable: Boolean) {
        preferencesTool.isFingerprintEnable = isEnable
        _isFingerprintEnable.value = preferencesTool.isFingerprintEnable
    }

    fun setPasscodeLockState(isEnable: Boolean) {
        preferencesTool.isPasscodeLockEnable = isEnable
        _isPasscodeEnable.value = preferencesTool.isPasscodeLockEnable
    }

    fun checkPasscode(digit: String) {
        if(code.length < 4) {
            code += digit
            if(code.length == 4) {
                setPasscode(code)
                code = ""
                _passcodeLockState.value = PasscodeLockState.SetPasscode()
            }
        }
    }

    fun checkConfirmPasscode(digit: String, error: String) {
        if(code.length < 4) {
            code += digit
            if(code.length == 4 ) {
                if(code == passcode.value) {
                    setPasscode(code)
                    code = ""
                    _passcodeLockState.value = PasscodeLockState.ConfirmPasscode()
                } else {
                    setError(true)
                    code = ""
                    _passcodeLockState.value = PasscodeLockState.Error(error)
                }
            }
        }
    }

    fun codeBackspace() {
        code = code.dropLast(1)
    }


    fun setNullState() {
        _passcodeLockState.value = null
    }

    fun setCode(value: String) {
        code = value
    }

    fun openBiometricDialog() {
        biometric.value = true
    }

}