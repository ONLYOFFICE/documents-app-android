package app.editors.manager.viewModels.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import app.documents.core.login.LoginResponse
import app.documents.core.network.ApiContract
import app.documents.core.network.models.login.Capabilities
import app.documents.core.network.models.login.response.ResponseCapabilities
import app.documents.core.network.models.login.response.ResponseSettings
import app.editors.manager.BuildConfig
import app.editors.manager.R
import app.editors.manager.app.App
import app.editors.manager.managers.utils.FirebaseUtils
import app.editors.manager.viewModels.base.BaseLoginViewModel
import io.reactivex.disposables.Disposable
import java.util.*

sealed class EnterprisePortalState {
    object Progress : EnterprisePortalState()
    class Success(val portal: String, val providers: Array<String>, val isHttp: Boolean) : EnterprisePortalState()
    class Error(val message: String? = null) : EnterprisePortalState()
}

class EnterprisePortalViewModel: BaseLoginViewModel() {

    companion object {
        val TAG: String = EnterprisePortalViewModel::class.java.simpleName
        private val BANNED_ADDRESSES: Set<String> = object : TreeSet<String>() {
            init {
                if (BuildConfig.APPLICATION_ID == "com.onlyoffice.documents") {
                    add(".r7-")
                }
            }
        }
        private const val TAG_SSH = "/#ssloff"
    }

    private val _portalStateLiveData = MutableLiveData<EnterprisePortalState>()
    val portalStateLiveData: LiveData<EnterprisePortalState> = _portalStateLiveData

    private var disposable: Disposable? = null

    override fun onCleared() {
        super.onCleared()
        disposable?.dispose()
    }

    fun cancel() {
        disposable?.dispose()
        _portalStateLiveData.value = EnterprisePortalState.Error(null)
    }

    fun checkPortal(portal: String) {
        networkSettings.setDefault()

        val builder = StringBuilder()

        if (checkBannedAddress(portal)) {
            _portalStateLiveData.value =
                EnterprisePortalState.Error(resourcesProvider.getString(R.string.errors_client_host_not_found))
            return
        }

        if (portal.endsWith(TAG_SSH)) {
            networkSettings.setSslState(false)
            builder.append(getPortal(portal.replace(TAG_SSH, "")) ?: "")
        } else {
            builder.append(getPortal(portal))
        }

        if (builder.isEmpty()) {
            _portalStateLiveData.value =
                EnterprisePortalState.Error(resourcesProvider.getString(R.string.login_enterprise_edit_error_hint))
            return
        }

        _portalStateLiveData.value =
            EnterprisePortalState.Progress
        networkSettings.setBaseUrl(builder.toString())
        portalCapabilities()

    }

    private fun checkBannedAddress(portal: String): Boolean {
        for (item in BANNED_ADDRESSES) {
            if (portal.contains(item)) {
                return true
            }
        }
        return false
    }

    private fun portalCapabilities() {
        val service = App.getApp().appComponent.loginService
        disposable = service.capabilities()
            .subscribe({ response ->
                if (response is LoginResponse.Success) {
                    if (response.response is ResponseCapabilities) {
                        val capability = (response.response as ResponseCapabilities).response
                        setSettings(capability)
                        if (networkSettings.getScheme() == ApiContract.SCHEME_HTTPS) {
                            _portalStateLiveData.value = EnterprisePortalState.Success(
                                networkSettings.getPortal(),
                                capability.providers.toTypedArray(),
                                false
                            )
                        } else {
                            _portalStateLiveData.value = EnterprisePortalState.Success(
                                networkSettings.getPortal(),
                                capability.providers.toTypedArray(),
                                true
                            )
                        }
                    } else {
                        networkSettings.serverVersion =
                            (response.response as ResponseSettings).response.communityServer ?: ""
                    }
                } else {
                    fetchError((response as LoginResponse.Error).error)
                }
            }) { throwable: Throwable -> checkError(throwable) }
    }

    private fun setSettings(capabilities: Capabilities) {
        networkSettings.ldap = capabilities.ldapEnabled
        networkSettings.ssoUrl = capabilities.ssoUrl
        networkSettings.ssoLabel = capabilities.ssoLabel
    }

    private fun checkError(throwable: Throwable) {
        if (isConfigConnection(throwable)) {
            portalCapabilities()
        } else {
            FirebaseUtils.addAnalyticsCheckPortal(
                networkSettings.getPortal(),
                FirebaseUtils.AnalyticsKeys.FAILED,
                "Error: " + throwable.message
            )
            fetchError(throwable)
        }
    }

}