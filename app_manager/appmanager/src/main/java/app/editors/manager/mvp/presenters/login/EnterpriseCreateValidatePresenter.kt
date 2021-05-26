package app.editors.manager.mvp.presenters.login

import android.content.Context
import android.telephony.TelephonyManager
import app.documents.core.login.LoginResponse
import app.documents.core.network.ApiContract
import app.documents.core.network.models.login.request.RequestValidatePortal
import app.editors.manager.R
import app.editors.manager.app.Api
import app.editors.manager.app.App
import app.editors.manager.mvp.views.login.EnterpriseCreateValidateView
import io.reactivex.disposables.Disposable
import lib.toolkit.base.managers.utils.StringUtils.isCreateUserName
import lib.toolkit.base.managers.utils.StringUtils.isEmailValid
import moxy.InjectViewState
import retrofit2.HttpException
import java.util.*

@InjectViewState
class EnterpriseCreateValidatePresenter : BaseLoginPresenter<EnterpriseCreateValidateView>() {

    companion object {
        val TAG: String = EnterpriseCreateValidatePresenter::class.java.simpleName
        private const val INFO_PHRASE = "getinfoportal00000"
        private const val INFO_DOMAIN = ".teamlab.info"
        private const val PORTAL_LENGTH_MIN = 6
        private const val PORTAL_LENGTH_MAX = 50
    }

    init {
        App.getApp().appComponent.inject(this)
    }

    private var disposable: Disposable? = null

    private var mDomain: String? = null
    private var portalName: String = ""
    private var email: String = ""
    private var first: String = ""
    private var last: String = ""

    val domain: Unit
        get() {
            val country = region
            mDomain = mRegionDomain[country.toUpperCase(Locale.getDefault())]
            if (mDomain == null) {
                mDomain = ApiContract.DEFAULT_HOST
            }
            mDomain = ".$mDomain"
            viewState.onRegionDomain(mDomain ?: "")
        }

    override fun onDestroy() {
        super.onDestroy()
        disposable?.dispose()
    }

    fun checkPhrase(value: String?): Boolean {
        if (INFO_PHRASE.equals(value, ignoreCase = true)) {
            mDomain = INFO_DOMAIN
            viewState.onRegionDomain(mDomain ?: "")
            return true
        }
        return false
    }

    fun validatePortal(portalName: String?, email: String?, first: String?, last: String?) {
        this.portalName = portalName ?: ""
        this.email = email ?: ""
        this.first = first ?: ""
        this.last = last ?: ""

        if (portalName != null && (portalName.length < PORTAL_LENGTH_MIN || portalName.length >= PORTAL_LENGTH_MAX)) {
            viewState.onPortalNameError(context.getString(R.string.login_api_portal_name_length))
            return
        }
        if (email != null && !isEmailValid(email)) {
            viewState.onEmailNameError(context.getString(R.string.errors_email_syntax_error))
            return
        }
        if (first != null && isCreateUserName(first)) {
            viewState.onFirstNameError(context.getString(R.string.errors_first_name))
            return
        }
        if (last != null && isCreateUserName(last)) {
            viewState.onLastNameError(context.getString(R.string.errors_last_name))
            return
        }
        networkSettings.setDefault()
        validatePortalName(this.portalName)
    }

    private fun validatePortalName(portalName: String) {
        networkSettings.setBaseUrl(ApiContract.API_SUBDOMAIN + mDomain)

        viewState.onShowWaitingDialog(R.string.dialogs_wait_title)
        disposable = App.getApp().loginComponent.loginService.validatePortal(RequestValidatePortal(portalName))
            .subscribe({ loginResponse ->
                when (loginResponse) {
                    is LoginResponse.Success -> {
                        onSuccessRequest()
                    }
                    is LoginResponse.Error -> {
                        checkError(loginResponse.error)
                    }
                }
            }, { error ->
                if (isConfigConnection(error)) {
                    validatePortalName(portalName)
                } else {
                    onFailureHandle(error)
                }
            })
    }

    private fun onSuccessRequest() {
        networkSettings.setBaseUrl(portalName + mDomain)
        viewState.onValidatePortalSuccess(email, first, last)
    }

    private fun checkError(error: Throwable) {
        if (error is HttpException && error.response()?.errorBody()?.string()
                ?.contains(ApiContract.Errors.PORTAL_EXIST) == true
        ) {
            viewState.onError(context.getString(R.string.errors_client_portal_exist))
        } else {
            fetchError(error)
        }
    }

    // No need handle
    private val region: String
        get() {
            try {
                val tm = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
                val simCountry = tm.simCountryIso
                if (simCountry != null && simCountry.length == 2) {
                    return simCountry.toUpperCase(Locale.US)
                } else if (tm.phoneType != TelephonyManager.PHONE_TYPE_CDMA) {
                    val networkCountry = tm.networkCountryIso
                    if (networkCountry != null && networkCountry.length == 2) {
                        return networkCountry.toUpperCase(Locale.US)
                    }
                }
            } catch (e: Exception) {
                // No need handle
            }
            return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                context.resources.configuration.locales[0].country
            } else {
                context.resources.configuration.locale.country
            }
        }

    private val mRegionDomain: Map<String, String> = object : LinkedHashMap<String, String>() {
        init {
            put("EU", "onlyoffice.eu")
            put("AX", "onlyoffice.eu")
            put("AF", "onlyoffice.sg")
            put("AL", "onlyoffice.eu")
            put("DZ", "onlyoffice.eu")
            put("AS", "onlyoffice.sg")
            put("AD", "onlyoffice.eu")
            put("AO", "onlyoffice.eu")
            put("AI", "onlyoffice.com")
            put("AQ", "onlyoffice.sg")
            put("AG", "onlyoffice.com")
            put("AR", "onlyoffice.com")
            put("AM", "onlyoffice.eu")
            put("AW", "onlyoffice.com")
            put("AU", "onlyoffice.sg")
            put("AT", "onlyoffice.eu")
            put("AZ", "onlyoffice.eu")
            put("BS", "onlyoffice.com")
            put("BH", "onlyoffice.eu")
            put("BD", "onlyoffice.sg")
            put("BB", "onlyoffice.com")
            put("BY", "onlyoffice.eu")
            put("BE", "onlyoffice.eu")
            put("BZ", "onlyoffice.com")
            put("BJ", "onlyoffice.eu")
            put("BM", "onlyoffice.com")
            put("BT", "onlyoffice.sg")
            put("BO", "onlyoffice.com")
            put("BA", "onlyoffice.eu")
            put("BW", "onlyoffice.eu")
            put("BV", "onlyoffice.com")
            put("BR", "onlyoffice.com")
            put("IO", "onlyoffice.sg")
            put("BN", "onlyoffice.sg")
            put("BG", "onlyoffice.eu")
            put("BF", "onlyoffice.eu")
            put("BI", "onlyoffice.eu")
            put("KH", "onlyoffice.sg")
            put("CM", "onlyoffice.eu")
            put("CA", "onlyoffice.com")
            put("CV", "onlyoffice.eu")
            put("KY", "onlyoffice.com")
            put("CF", "onlyoffice.eu")
            put("TD", "onlyoffice.eu")
            put("CL", "onlyoffice.com")
            put("CN", "onlyoffice.sg")
            put("CX", "onlyoffice.sg")
            put("CC", "onlyoffice.sg")
            put("CO", "onlyoffice.com")
            put("KM", "onlyoffice.eu")
            put("CD", "onlyoffice.eu")
            put("CG", "onlyoffice.eu")
            put("CK", "onlyoffice.sg")
            put("CR", "onlyoffice.com")
            put("CI", "onlyoffice.com")
            put("HR", "onlyoffice.eu")
            put("CU", "onlyoffice.com")
            put("CY", "onlyoffice.eu")
            put("CZ", "onlyoffice.eu")
            put("DK", "onlyoffice.eu")
            put("DJ", "onlyoffice.eu")
            put("DM", "onlyoffice.com")
            put("DO", "onlyoffice.com")
            put("EC", "onlyoffice.com")
            put("EG", "onlyoffice.eu")
            put("SV", "onlyoffice.com")
            put("GQ", "onlyoffice.eu")
            put("ER", "onlyoffice.eu")
            put("EE", "onlyoffice.eu")
            put("ET", "onlyoffice.eu")
            put("FK", "onlyoffice.com")
            put("FO", "onlyoffice.eu")
            put("FJ", "onlyoffice.sg")
            put("FI", "onlyoffice.eu")
            put("FR", "onlyoffice.eu")
            put("GF", "onlyoffice.com")
            put("PF", "onlyoffice.sg")
            put("TF", "onlyoffice.eu")
            put("GA", "onlyoffice.eu")
            put("GM", "onlyoffice.eu")
            put("GE", "onlyoffice.eu")
            put("DE", "onlyoffice.eu")
            put("GH", "onlyoffice.eu")
            put("GI", "onlyoffice.eu")
            put("GR", "onlyoffice.eu")
            put("GL", "onlyoffice.eu")
            put("GD", "onlyoffice.com")
            put("GP", "onlyoffice.com")
            put("GU", "onlyoffice.sg")
            put("GT", "onlyoffice.com")
            put("GN", "onlyoffice.eu")
            put("GW", "onlyoffice.eu")
            put("GY", "onlyoffice.com")
            put("HT", "onlyoffice.com")
            put("HM", "onlyoffice.com")
            put("HN", "onlyoffice.com")
            put("HK", "onlyoffice.sg")
            put("HU", "onlyoffice.eu")
            put("IS", "onlyoffice.eu")
            put("IN", "onlyoffice.sg")
            put("ID", "onlyoffice.sg")
            put("IR", "onlyoffice.eu")
            put("IQ", "onlyoffice.eu")
            put("IE", "onlyoffice.eu")
            put("IL", "onlyoffice.eu")
            put("IT", "onlyoffice.eu")
            put("JM", "onlyoffice.com")
            put("JP", "onlyoffice.sg")
            put("JO", "onlyoffice.eu")
            put("KZ", "onlyoffice.eu")
            put("KE", "onlyoffice.eu")
            put("KI", "onlyoffice.sg")
            put("KP", "onlyoffice.sg")
            put("KR", "onlyoffice.sg")
            put("KW", "onlyoffice.eu")
            put("KG", "onlyoffice.sg")
            put("LA", "onlyoffice.sg")
            put("LV", "onlyoffice.eu")
            put("LB", "onlyoffice.eu")
            put("LS", "onlyoffice.eu")
            put("LR", "onlyoffice.eu")
            put("LY", "onlyoffice.eu")
            put("LI", "onlyoffice.eu")
            put("LT", "onlyoffice.eu")
            put("LU", "onlyoffice.eu")
            put("MO", "onlyoffice.sg")
            put("MK", "onlyoffice.eu")
            put("MG", "onlyoffice.eu")
            put("MW", "onlyoffice.eu")
            put("MY", "onlyoffice.sg")
            put("MV", "onlyoffice.sg")
            put("ML", "onlyoffice.eu")
            put("MT", "onlyoffice.eu")
            put("MH", "onlyoffice.sg")
            put("MQ", "onlyoffice.com")
            put("MR", "onlyoffice.eu")
            put("MU", "onlyoffice.eu")
            put("YT", "onlyoffice.eu")
            put("MX", "onlyoffice.com")
            put("FM", "onlyoffice.sg")
            put("MD", "onlyoffice.eu")
            put("MC", "onlyoffice.eu")
            put("MN", "onlyoffice.sg")
            put("MS", "onlyoffice.com")
            put("MA", "onlyoffice.eu")
            put("MZ", "onlyoffice.eu")
            put("MM", "onlyoffice.sg")
            put("NA", "onlyoffice.eu")
            put("NR", "onlyoffice.sg")
            put("NP", "onlyoffice.sg")
            put("NL", "onlyoffice.eu")
            put("AN", "onlyoffice.com")
            put("NC", "onlyoffice.sg")
            put("NZ", "onlyoffice.sg")
            put("NI", "onlyoffice.com")
            put("NE", "onlyoffice.eu")
            put("NG", "onlyoffice.eu")
            put("NU", "onlyoffice.sg")
            put("NF", "onlyoffice.sg")
            put("MP", "onlyoffice.sg")
            put("NO", "onlyoffice.eu")
            put("OM", "onlyoffice.eu")
            put("PK", "onlyoffice.sg")
            put("PW", "onlyoffice.sg")
            put("PS", "onlyoffice.eu")
            put("PA", "onlyoffice.com")
            put("PG", "onlyoffice.sg")
            put("PY", "onlyoffice.com")
            put("PE", "onlyoffice.com")
            put("PH", "onlyoffice.sg")
            put("PN", "onlyoffice.com")
            put("PL", "onlyoffice.eu")
            put("PT", "onlyoffice.eu")
            put("PR", "onlyoffice.com")
            put("QA", "onlyoffice.eu")
            put("RE", "onlyoffice.eu")
            put("RO", "onlyoffice.eu")
            put("RU", "onlyoffice.eu")
            put("RW", "onlyoffice.eu")
            put("SH", "onlyoffice.eu")
            put("KN", "onlyoffice.com")
            put("LC", "onlyoffice.com")
            put("PM", "onlyoffice.com")
            put("VC", "onlyoffice.com")
            put("WS", "onlyoffice.sg")
            put("SM", "onlyoffice.eu")
            put("ST", "onlyoffice.eu")
            put("SA", "onlyoffice.eu")
            put("SN", "onlyoffice.eu")
            put("CS", "onlyoffice.com")
            put("RS", "onlyoffice.com")
            put("SC", "onlyoffice.eu")
            put("SL", "onlyoffice.eu")
            put("SG", "onlyoffice.sg")
            put("SK", "onlyoffice.eu")
            put("SI", "onlyoffice.eu")
            put("SB", "onlyoffice.sg")
            put("SO", "onlyoffice.eu")
            put("ZA", "onlyoffice.eu")
            put("GS", "onlyoffice.com")
            put("ES", "onlyoffice.eu")
            put("LK", "onlyoffice.sg")
            put("SD", "onlyoffice.eu")
            put("SR", "onlyoffice.com")
            put("SJ", "onlyoffice.eu")
            put("SZ", "onlyoffice.eu")
            put("SE", "onlyoffice.eu")
            put("CH", "onlyoffice.eu")
            put("SY", "onlyoffice.eu")
            put("TW", "onlyoffice.sg")
            put("TJ", "onlyoffice.eu")
            put("TZ", "onlyoffice.eu")
            put("TH", "onlyoffice.sg")
            put("TL", "onlyoffice.sg")
            put("TG", "onlyoffice.eu")
            put("TK", "onlyoffice.sg")
            put("TO", "onlyoffice.sg")
            put("TT", "onlyoffice.com")
            put("TN", "onlyoffice.eu")
            put("TR", "onlyoffice.eu")
            put("TM", "onlyoffice.eu")
            put("TC", "onlyoffice.com")
            put("TV", "onlyoffice.sg")
            put("UG", "onlyoffice.eu")
            put("UA", "onlyoffice.eu")
            put("AE", "onlyoffice.eu")
            put("GB", "onlyoffice.eu")
            put("US", "onlyoffice.com")
            put("UM", "onlyoffice.com")
            put("UY", "onlyoffice.com")
            put("UZ", "onlyoffice.eu")
            put("VU", "onlyoffice.sg")
            put("VA", "onlyoffice.eu")
            put("VE", "onlyoffice.com")
            put("VN", "onlyoffice.sg")
            put("VG", "onlyoffice.com")
            put("VI", "onlyoffice.com")
            put("WF", "onlyoffice.sg")
            put("EH", "onlyoffice.eu")
            put("YE", "onlyoffice.eu")
            put("ZM", "onlyoffice.eu")
            put("ZW", "onlyoffice.eu")
        }
    }
}