package app.editors.manager.viewModels.login

import androidx.annotation.StringRes
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import app.documents.core.model.cloud.CloudPortal
import app.documents.core.network.common.NetworkResult
import app.documents.core.network.common.contracts.ApiContract
import app.editors.manager.R
import app.editors.manager.app.App
import app.editors.manager.viewModels.base.BaseViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import lib.toolkit.base.managers.utils.StringUtils
import retrofit2.HttpException

sealed class CreatePortalState {
    data object None : CreatePortalState()
    data object Progress : CreatePortalState()
    class Success(val portalModel: PortalModel) : CreatePortalState()
    class Error(@StringRes val res: Int? = null) : CreatePortalState()
}

data class PortalModel(
    val portalName: String?,
    val email: String?,
    val firstName: String?,
    val lastName: String?,
)

class EnterpriseCreateValidateViewModel : BaseViewModel() {

    companion object {
        val TAG: String = EnterpriseCreateValidateViewModel::class.java.simpleName

        private const val INFO_PHRASE = "getinfoportal00000"
        private const val INFO_DOMAIN = ".teamlab.info"
        private const val PORTAL_LENGTH_MIN = 6
        private const val PORTAL_LENGTH_MAX = 50
    }

    private val _stateLiveData: MutableLiveData<CreatePortalState> = MutableLiveData<CreatePortalState>()
    val stateLiveData: LiveData<CreatePortalState> = _stateLiveData

    private val _regionLiveData = MutableLiveData<String?>()
    val regionLiveData: LiveData<String?> = _regionLiveData

    private var domain: String? = null

    private var job: Job? = null

    init {
        App.getApp().appComponent.inject(this)
    }

    fun cancelRequest() {
        job?.cancel()
        _stateLiveData.value = CreatePortalState.Error()
    }

    fun validatePortal(portalName: String?, email: String?, first: String?, last: String?) {
        val model = PortalModel(portalName = portalName, email = email, firstName = first, lastName = last)

        if (portalName != null && (portalName.length < PORTAL_LENGTH_MIN || portalName.length >= PORTAL_LENGTH_MAX)) {
            _stateLiveData.value = CreatePortalState.Error(R.string.login_api_portal_name_length)
            return
        }
        if (email != null && !StringUtils.isEmailValid(email)) {
            _stateLiveData.value = CreatePortalState.Error(R.string.errors_email_syntax_error)
            return
        }
        if (first != null && StringUtils.isCreateUserName(first)) {
            _stateLiveData.value = CreatePortalState.Error(R.string.errors_first_name)
            return
        }
        if (last != null && StringUtils.isCreateUserName(last)) {
            _stateLiveData.value = CreatePortalState.Error(R.string.errors_last_name)
            return
        }
        validatePortalName(portalName ?: "", model)
    }

    private fun validatePortalName(portalName: String, model: PortalModel) {
        App.getApp().refreshLoginComponent(CloudPortal(url = ApiContract.API_SUBDOMAIN + domain))

        _stateLiveData.value = CreatePortalState.Progress
        job = viewModelScope.launch {
            App.getApp().loginComponent.cloudLoginRepository.validatePortal(portalName)
                .collect { result ->
                    when (result) {
                        is NetworkResult.Error -> checkError(result.exception)
                        is NetworkResult.Success -> onSuccessRequest(portalName, model)
                        is NetworkResult.Loading -> Unit
                    }
                }
        }
    }

    private fun onSuccessRequest(portalName: String, model: PortalModel) {
        App.getApp().refreshLoginComponent(CloudPortal(url = portalName + domain))
        _stateLiveData.value = CreatePortalState.Success(model)
        _stateLiveData.value = CreatePortalState.None
    }

    private fun checkError(error: Throwable) {
        if (error is HttpException && error.response()?.errorBody()?.string()
                ?.contains(ApiContract.Errors.PORTAL_EXIST) == true
        ) {
            _stateLiveData.value = CreatePortalState.Error(R.string.errors_client_portal_exist)
        } else {
            fetchError(error)
        }
    }


    fun checkPhrase(value: String?): Boolean {
        if (INFO_PHRASE.equals(value, ignoreCase = true)) {
            domain = INFO_DOMAIN
            _regionLiveData.value = domain
            return true
        }
        return false
    }

    fun getDomain() {
        domain = ".${ApiContract.DEFAULT_HOST}"
        _regionLiveData.value = domain
    }

}