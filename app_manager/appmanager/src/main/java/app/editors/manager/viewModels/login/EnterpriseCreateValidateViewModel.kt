package app.editors.manager.viewModels.login

import android.telephony.TelephonyManager
import androidx.annotation.StringRes
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import app.documents.core.login.LoginResponse
import app.documents.core.network.ApiContract
import app.documents.core.network.models.login.request.RequestValidatePortal
import app.editors.manager.R
import app.editors.manager.app.App
import app.editors.manager.viewModels.base.BaseLoginViewModel
import io.reactivex.disposables.Disposable
import lib.toolkit.base.managers.tools.ServiceProvider
import lib.toolkit.base.managers.utils.StringUtils
import retrofit2.HttpException
import java.util.*
import javax.inject.Inject

sealed class CreatePortalState {
    object None: CreatePortalState()
    object Progress : CreatePortalState()
    class Success(val portalModel: PortalModel): CreatePortalState()
    class Error(@StringRes val res: Int? = null) : CreatePortalState()
}

data class PortalModel(
    val email: String?,
    val firstName: String?,
    val lastName: String?,
)

class EnterpriseCreateValidateViewModel : BaseLoginViewModel() {

    companion object {
        val TAG: String = EnterpriseCreateValidateViewModel::class.java.simpleName

        private const val INFO_PHRASE = "getinfoportal00000"
        private const val INFO_DOMAIN = ".teamlab.info"
        private const val PORTAL_LENGTH_MIN = 6
        private const val PORTAL_LENGTH_MAX = 50
    }


    @Inject
    protected lateinit var serviceProvider: ServiceProvider

    private val _stateLiveData: MutableLiveData<CreatePortalState> = MutableLiveData<CreatePortalState>()
    val stateLiveData: LiveData<CreatePortalState> = _stateLiveData

    private val _regionLiveData = MutableLiveData<String?>()
    val regionLiveData: LiveData<String?> = _regionLiveData

    private var disposable: Disposable? = null

    private var domain: String? = null

    override fun onCleared() {
        super.onCleared()
        disposable?.dispose()
    }

    fun cancelRequest() {
        disposable?.dispose()
        _stateLiveData.value = CreatePortalState.Error()
    }

    fun validatePortal(portalName: String?, email: String?, first: String?, last: String?) {
        val model = PortalModel(email = email, firstName = first, lastName = last)

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
        networkSettings.setDefault()
        validatePortalName(portalName ?: "", model)
    }

    private fun validatePortalName(portalName: String, model: PortalModel) {
        networkSettings.setBaseUrl(ApiContract.API_SUBDOMAIN + domain)

        _stateLiveData.value = CreatePortalState.Progress
        disposable = App.getApp().appComponent.loginService.validatePortal(RequestValidatePortal(portalName))
            .subscribe({ loginResponse ->
                when (loginResponse) {
                    is LoginResponse.Success -> {
                        onSuccessRequest(portalName, model)
                    }
                    is LoginResponse.Error -> {
                        checkError(loginResponse.error)
                    }
                }
            }, { error ->
                if (isConfigConnection(error)) {
                    validatePortalName(portalName, model)
                } else {
                    fetchError(error)
                }
            })
    }

    private fun onSuccessRequest(portalName: String, model: PortalModel) {
        networkSettings.setBaseUrl(portalName + domain)
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
        val country = getRegion()
        domain = regionDomain[country.uppercase(Locale.getDefault())]
        if (domain == null) {
            domain = ApiContract.DEFAULT_HOST
        }
        domain = ".$domain"
        _regionLiveData.value = domain
    }

    private fun getRegion(): String {
        try {
            val tm = serviceProvider.getTelephoneService()
            val simCountry = tm?.simCountryIso
            if (simCountry != null && simCountry.length == 2) {
                return simCountry.uppercase()
            } else if (tm?.phoneType != TelephonyManager.PHONE_TYPE_CDMA) {
                val networkCountry = tm?.networkCountryIso
                if (networkCountry != null && networkCountry.length == 2) {
                    return networkCountry.uppercase()
                }
            }
        } catch (e: Exception) {
            // No need handle
        }
        return resourcesProvider.getLocale() ?: "EU"
    }

    private val regionDomain: Map<String, String> by lazy {
        mapOf(
            "EU" to "onlyoffice.eu",
            "AX" to "onlyoffice.eu",
            "AF" to "onlyoffice.sg",
            "AL" to "onlyoffice.eu",
            "DZ" to "onlyoffice.eu",
            "AS" to "onlyoffice.sg",
            "AD" to "onlyoffice.eu",
            "AO" to "onlyoffice.eu",
            "AI" to "onlyoffice.com",
            "AQ" to "onlyoffice.sg",
            "AG" to "onlyoffice.com",
            "AR" to "onlyoffice.com",
            "AM" to "onlyoffice.eu",
            "AW" to "onlyoffice.com",
            "AU" to "onlyoffice.sg",
            "AT" to "onlyoffice.eu",
            "AZ" to "onlyoffice.eu",
            "BS" to "onlyoffice.com",
            "BH" to "onlyoffice.eu",
            "BD" to "onlyoffice.sg",
            "BB" to "onlyoffice.com",
            "BY" to "onlyoffice.eu",
            "BE" to "onlyoffice.eu",
            "BZ" to "onlyoffice.com",
            "BJ" to "onlyoffice.eu",
            "BM" to "onlyoffice.com",
            "BT" to "onlyoffice.sg",
            "BO" to "onlyoffice.com",
            "BA" to "onlyoffice.eu",
            "BW" to "onlyoffice.eu",
            "BV" to "onlyoffice.com",
            "BR" to "onlyoffice.com",
            "IO" to "onlyoffice.sg",
            "BN" to "onlyoffice.sg",
            "BG" to "onlyoffice.eu",
            "BF" to "onlyoffice.eu",
            "BI" to "onlyoffice.eu",
            "KH" to "onlyoffice.sg",
            "CM" to "onlyoffice.eu",
            "CA" to "onlyoffice.com",
            "CV" to "onlyoffice.eu",
            "KY" to "onlyoffice.com",
            "CF" to "onlyoffice.eu",
            "TD" to "onlyoffice.eu",
            "CL" to "onlyoffice.com",
            "CN" to "onlyoffice.sg",
            "CX" to "onlyoffice.sg",
            "CC" to "onlyoffice.sg",
            "CO" to "onlyoffice.com",
            "KM" to "onlyoffice.eu",
            "CD" to "onlyoffice.eu",
            "CG" to "onlyoffice.eu",
            "CK" to "onlyoffice.sg",
            "CR" to "onlyoffice.com",
            "CI" to "onlyoffice.com",
            "HR" to "onlyoffice.eu",
            "CU" to "onlyoffice.com",
            "CY" to "onlyoffice.eu",
            "CZ" to "onlyoffice.eu",
            "DK" to "onlyoffice.eu",
            "DJ" to "onlyoffice.eu",
            "DM" to "onlyoffice.com",
            "DO" to "onlyoffice.com",
            "EC" to "onlyoffice.com",
            "EG" to "onlyoffice.eu",
            "SV" to "onlyoffice.com",
            "GQ" to "onlyoffice.eu",
            "ER" to "onlyoffice.eu",
            "EE" to "onlyoffice.eu",
            "ET" to "onlyoffice.eu",
            "FK" to "onlyoffice.com",
            "FO" to "onlyoffice.eu",
            "FJ" to "onlyoffice.sg",
            "FI" to "onlyoffice.eu",
            "FR" to "onlyoffice.eu",
            "GF" to "onlyoffice.com",
            "PF" to "onlyoffice.sg",
            "TF" to "onlyoffice.eu",
            "GA" to "onlyoffice.eu",
            "GM" to "onlyoffice.eu",
            "GE" to "onlyoffice.eu",
            "DE" to "onlyoffice.eu",
            "GH" to "onlyoffice.eu",
            "GI" to "onlyoffice.eu",
            "GR" to "onlyoffice.eu",
            "GL" to "onlyoffice.eu",
            "GD" to "onlyoffice.com",
            "GP" to "onlyoffice.com",
            "GU" to "onlyoffice.sg",
            "GT" to "onlyoffice.com",
            "GN" to "onlyoffice.eu",
            "GW" to "onlyoffice.eu",
            "GY" to "onlyoffice.com",
            "HT" to "onlyoffice.com",
            "HM" to "onlyoffice.com",
            "HN" to "onlyoffice.com",
            "HK" to "onlyoffice.sg",
            "HU" to "onlyoffice.eu",
            "IS" to "onlyoffice.eu",
            "IN" to "onlyoffice.sg",
            "ID" to "onlyoffice.sg",
            "IR" to "onlyoffice.eu",
            "IQ" to "onlyoffice.eu",
            "IE" to "onlyoffice.eu",
            "IL" to "onlyoffice.eu",
            "IT" to "onlyoffice.eu",
            "JM" to "onlyoffice.com",
            "JP" to "onlyoffice.sg",
            "JO" to "onlyoffice.eu",
            "KZ" to "onlyoffice.eu",
            "KE" to "onlyoffice.eu",
            "KI" to "onlyoffice.sg",
            "KP" to "onlyoffice.sg",
            "KR" to "onlyoffice.sg",
            "KW" to "onlyoffice.eu",
            "KG" to "onlyoffice.sg",
            "LA" to "onlyoffice.sg",
            "LV" to "onlyoffice.eu",
            "LB" to "onlyoffice.eu",
            "LS" to "onlyoffice.eu",
            "LR" to "onlyoffice.eu",
            "LY" to "onlyoffice.eu",
            "LI" to "onlyoffice.eu",
            "LT" to "onlyoffice.eu",
            "LU" to "onlyoffice.eu",
            "MO" to "onlyoffice.sg",
            "MK" to "onlyoffice.eu",
            "MG" to "onlyoffice.eu",
            "MW" to "onlyoffice.eu",
            "MY" to "onlyoffice.sg",
            "MV" to "onlyoffice.sg",
            "ML" to "onlyoffice.eu",
            "MT" to "onlyoffice.eu",
            "MH" to "onlyoffice.sg",
            "MQ" to "onlyoffice.com",
            "MR" to "onlyoffice.eu",
            "MU" to "onlyoffice.eu",
            "YT" to "onlyoffice.eu",
            "MX" to "onlyoffice.com",
            "FM" to "onlyoffice.sg",
            "MD" to "onlyoffice.eu",
            "MC" to "onlyoffice.eu",
            "MN" to "onlyoffice.sg",
            "MS" to "onlyoffice.com",
            "MA" to "onlyoffice.eu",
            "MZ" to "onlyoffice.eu",
            "MM" to "onlyoffice.sg",
            "NA" to "onlyoffice.eu",
            "NR" to "onlyoffice.sg",
            "NP" to "onlyoffice.sg",
            "NL" to "onlyoffice.eu",
            "AN" to "onlyoffice.com",
            "NC" to "onlyoffice.sg",
            "NZ" to "onlyoffice.sg",
            "NI" to "onlyoffice.com",
            "NE" to "onlyoffice.eu",
            "NG" to "onlyoffice.eu",
            "NU" to "onlyoffice.sg",
            "NF" to "onlyoffice.sg",
            "MP" to "onlyoffice.sg",
            "NO" to "onlyoffice.eu",
            "OM" to "onlyoffice.eu",
            "PK" to "onlyoffice.sg",
            "PW" to "onlyoffice.sg",
            "PS" to "onlyoffice.eu",
            "PA" to "onlyoffice.com",
            "PG" to "onlyoffice.sg",
            "PY" to "onlyoffice.com",
            "PE" to "onlyoffice.com",
            "PH" to "onlyoffice.sg",
            "PN" to "onlyoffice.com",
            "PL" to "onlyoffice.eu",
            "PT" to "onlyoffice.eu",
            "PR" to "onlyoffice.com",
            "QA" to "onlyoffice.eu",
            "RE" to "onlyoffice.eu",
            "RO" to "onlyoffice.eu",
            "RU" to "onlyoffice.eu",
            "RW" to "onlyoffice.eu",
            "SH" to "onlyoffice.eu",
            "KN" to "onlyoffice.com",
            "LC" to "onlyoffice.com",
            "PM" to "onlyoffice.com",
            "VC" to "onlyoffice.com",
            "WS" to "onlyoffice.sg",
            "SM" to "onlyoffice.eu",
            "ST" to "onlyoffice.eu",
            "SA" to "onlyoffice.eu",
            "SN" to "onlyoffice.eu",
            "CS" to "onlyoffice.com",
            "RS" to "onlyoffice.com",
            "SC" to "onlyoffice.eu",
            "SL" to "onlyoffice.eu",
            "SG" to "onlyoffice.sg",
            "SK" to "onlyoffice.eu",
            "SI" to "onlyoffice.eu",
            "SB" to "onlyoffice.sg",
            "SO" to "onlyoffice.eu",
            "ZA" to "onlyoffice.eu",
            "GS" to "onlyoffice.com",
            "ES" to "onlyoffice.eu",
            "LK" to "onlyoffice.sg",
            "SD" to "onlyoffice.eu",
            "SR" to "onlyoffice.com",
            "SJ" to "onlyoffice.eu",
            "SZ" to "onlyoffice.eu",
            "SE" to "onlyoffice.eu",
            "CH" to "onlyoffice.eu",
            "SY" to "onlyoffice.eu",
            "TW" to "onlyoffice.sg",
            "TJ" to "onlyoffice.eu",
            "TZ" to "onlyoffice.eu",
            "TH" to "onlyoffice.sg",
            "TL" to "onlyoffice.sg",
            "TG" to "onlyoffice.eu",
            "TK" to "onlyoffice.sg",
            "TO" to "onlyoffice.sg",
            "TT" to "onlyoffice.com",
            "TN" to "onlyoffice.eu",
            "TR" to "onlyoffice.eu",
            "TM" to "onlyoffice.eu",
            "TC" to "onlyoffice.com",
            "TV" to "onlyoffice.sg",
            "UG" to "onlyoffice.eu",
            "UA" to "onlyoffice.eu",
            "AE" to "onlyoffice.eu",
            "GB" to "onlyoffice.eu",
            "US" to "onlyoffice.com",
            "UM" to "onlyoffice.com",
            "UY" to "onlyoffice.com",
            "UZ" to "onlyoffice.eu",
            "VU" to "onlyoffice.sg",
            "VA" to "onlyoffice.eu",
            "VE" to "onlyoffice.com",
            "VN" to "onlyoffice.sg",
            "VG" to "onlyoffice.com",
            "VI" to "onlyoffice.com",
            "WF" to "onlyoffice.sg",
            "EH" to "onlyoffice.eu",
            "YE" to "onlyoffice.eu",
            "ZM" to "onlyoffice.eu",
            "ZW" to "onlyoffice.eu"
        )
    }

}