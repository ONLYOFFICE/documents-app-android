package app.editors.manager.viewModels.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import app.documents.core.model.login.request.RequestNumber
import app.documents.core.model.login.request.RequestSignIn
import app.documents.core.network.common.NetworkResult
import app.editors.manager.app.App
import app.editors.manager.managers.tools.CountriesCodesTool
import app.editors.manager.managers.tools.PreferenceTool
import app.editors.manager.viewModels.base.BaseViewModel
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.Locale
import javax.inject.Inject

sealed class EnterprisePhoneState {
    class Success(val request: String) : EnterprisePhoneState()
    class Error(val message: String) : EnterprisePhoneState()
}

class EnterprisePhoneViewModel : BaseViewModel() {

    @Inject
    internal lateinit var countriesCodesTool: CountriesCodesTool

    @Inject
    internal lateinit var preferenceTool: PreferenceTool

    private val _stateLiveData: MutableLiveData<EnterprisePhoneState?> = MutableLiveData()
    val stateLiveData: LiveData<EnterprisePhoneState?> = _stateLiveData

    val countiesCodes: LiveData<CountriesCodesTool.Codes?> =
        liveData { emit(countriesCodesTool.getCodeByRegion(Locale.getDefault().country)) }

    fun setPhone(newPhone: String, request: String) {
        val requestNumber = with(Json.decodeFromString<RequestSignIn>(request)) {
            RequestNumber(
                mobilePhone = newPhone,
                userName = userName,
                password = password,
                provider = provider,
                accessToken = accessToken
            )
        }

        viewModelScope.launch {
            App.getApp().loginComponent.cloudLoginRepository.changeNumber(requestNumber)
                .collect { result ->
                    when (result) {
                        is NetworkResult.Success -> {
                            preferenceTool.phoneNoise = newPhone
                            _stateLiveData.value = EnterprisePhoneState.Success(Json.encodeToString(requestNumber))
                        }
                        is NetworkResult.Error -> {
                            errorHandler.fetchError(result.exception) { error ->
                                _stateLiveData.value = EnterprisePhoneState.Error(error.message)
                            }
                        }
                        is NetworkResult.Loading -> Unit
                    }
                }
        }
    }
}