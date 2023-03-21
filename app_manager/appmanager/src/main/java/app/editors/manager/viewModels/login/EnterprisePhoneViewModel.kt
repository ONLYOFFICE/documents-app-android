package app.editors.manager.viewModels.login

import androidx.lifecycle.*
import app.documents.core.network.login.ILoginServiceProvider
import app.documents.core.network.login.LoginResponse
import app.documents.core.network.login.models.request.RequestNumber
import app.documents.core.network.login.models.request.RequestSignIn
import app.editors.manager.managers.tools.CountriesCodesTool
import app.editors.manager.managers.tools.PreferenceTool
import app.editors.manager.viewModels.base.BaseViewModel
import io.reactivex.disposables.Disposable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.*
import javax.inject.Inject

sealed class EnterprisePhoneState {
    class Success(val request: String) : EnterprisePhoneState()
    class Error(val message: String) : EnterprisePhoneState()
}

class EnterprisePhoneViewModelFactory(private val loginService: ILoginServiceProvider) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return EnterprisePhoneViewModel(loginService) as T
    }

}

class EnterprisePhoneViewModel(private val loginService: ILoginServiceProvider) : BaseViewModel() {

    @Inject
    internal lateinit var countriesCodesTool: CountriesCodesTool

    @Inject
    internal lateinit var preferenceTool: PreferenceTool


    private var disposable: Disposable? = null

    private val _stateLiveData: MutableLiveData<EnterprisePhoneState?> = MutableLiveData()
    val stateLiveData: LiveData<EnterprisePhoneState?> = _stateLiveData

    val countiesCodes: LiveData<CountriesCodesTool.Codes?> =
        liveData { emit(countriesCodesTool.getCodeByRegion(Locale.getDefault().country)) }

    override fun onCleared() {
        super.onCleared()
        disposable?.dispose()
    }

    fun setPhone(newPhone: String?, request: String) {
        val requestNumber = Json.decodeFromString<RequestSignIn>(request)
        disposable = newPhone?.let { (requestNumber as RequestNumber).copy(mobilePhone = it) }?.let {
            loginService.changeNumber(it)
                .subscribe({ response ->
                    if (response is LoginResponse.Success) {
                        preferenceTool.phoneNoise = newPhone
                        _stateLiveData.value = EnterprisePhoneState.Success(
                            Json.encodeToString(
                                (requestNumber as RequestNumber).copy(mobilePhone = newPhone)
                            )
                        )
                    } else {
                        _stateLiveData.value =
                            EnterprisePhoneState.Error((response as LoginResponse.Error).error.message ?: "")
                    }
                })
                { throwable ->
                    errorHandler.fetchError(throwable) { error ->
                        _stateLiveData.value = EnterprisePhoneState.Error(error.message)
                    }
                }
        }
    }

}