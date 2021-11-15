package app.editors.manager.viewModels.main

import androidx.lifecycle.*
import app.editors.manager.managers.tools.PreferenceTool
import app.editors.manager.managers.utils.KeyStoreUtils
import lib.toolkit.base.managers.utils.SingleLiveEvent

sealed class PasscodeLockState() {
    class SetPasscode(): PasscodeLockState()
    class ConfirmPasscode(): PasscodeLockState()
    data class Error(val errorMessage: String): PasscodeLockState()
}


class SetPasscodeViewModel(private val preferencesTool: PreferenceTool): ViewModel(){

    companion object {
        private const val MAX_PASSCODE_LENGTH = 4
    }
    private val _passcode: MutableLiveData<String> = MutableLiveData()
    private val passcode: LiveData<String> = _passcode

    private val _passcodeLockState: MutableLiveData<PasscodeLockState> = MutableLiveData()
    val passcodeLockState: LiveData<PasscodeLockState> = _passcodeLockState

    private val _errorMessage: MutableLiveData<String> = MutableLiveData()
    val errorMessage: LiveData<String> = _errorMessage

    private val _isFingerprintEnable = MutableLiveData<Boolean>()
    val isFingerprintEnable: LiveData<Boolean> = _isFingerprintEnable

    private val _isPasscodeEnable = MutableLiveData<Boolean>()
    val isPasscodeEnable: LiveData<Boolean> = _isPasscodeEnable

    private val _codeCount = MutableLiveData(-1)
    val codeCount: LiveData<Int> = _codeCount

    val biometric = SingleLiveEvent<Boolean>()
    val error = SingleLiveEvent<Boolean>()

    private var code: String = ""
    private var confirmCode = ""

    fun getData() {
        if(preferencesTool.passcode?.isEmpty() == true)
            preferencesTool.passcode = KeyStoreUtils.encryptData("")
        _passcode.value = preferencesTool.passcode?.let { KeyStoreUtils.decryptData(it) }
        _isPasscodeEnable.value = preferencesTool.isPasscodeLockEnable
        _isFingerprintEnable.value = preferencesTool.isFingerprintEnable
    }

    fun setPasscode() {
        preferencesTool.passcode = KeyStoreUtils.encryptData(confirmCode)
        _passcode.value = KeyStoreUtils.decryptData(preferencesTool.passcode!!)
    }

    fun setError(isError: Boolean) {
        error.value = isError
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
        incrementCodeCount()
        if(code.length < MAX_PASSCODE_LENGTH) {
            code += digit
            if(code.length == MAX_PASSCODE_LENGTH) {
                _passcodeLockState.value = PasscodeLockState.SetPasscode()
            }
        }
    }

    fun checkConfirmPasscode(digit: String, error: String) {
        incrementCodeCount()
        if(confirmCode.length < MAX_PASSCODE_LENGTH) {
            confirmCode += digit
            if(confirmCode.length == MAX_PASSCODE_LENGTH ) {
                if(confirmCode == if(code.isEmpty()) passcode.value else code) {
                    code = ""
                    _passcodeLockState.value = PasscodeLockState.ConfirmPasscode()
                } else {
                    setError(true)
                    code = ""
                    confirmCode = ""
                    _passcodeLockState.value = PasscodeLockState.Error(error)
                }
            }
        }
    }

    fun codeBackspace() {
        code = code.dropLast(1)
        decrementCodeCount()
    }

    fun confirmCodeBackSpace () {
        confirmCode = confirmCode.dropLast(1)
        decrementCodeCount()
    }

    fun setNullState() {
        _passcodeLockState.value = null
    }

    fun openBiometricDialog() {
        biometric.value = true
    }

    private fun incrementCodeCount() {
        _codeCount.value = codeCount.value?.plus(1)
        if(codeCount.value!! >= MAX_PASSCODE_LENGTH)
            resetCodeCount()
    }

    private fun decrementCodeCount () {
        if(codeCount.value!! > -1) {
            _codeCount.value = codeCount.value?.minus(1)
        } else
            resetCodeCount()
    }

    fun resetCodeCount() {
        _codeCount.value = -1
    }

    fun resetConfirmCode() {
        confirmCode = ""
        resetCodeCount()
    }

    fun resetErrorState() {
        setError(false)
        setNullState()
    }
}