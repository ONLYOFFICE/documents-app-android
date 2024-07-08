package app.editors.manager.viewModels.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import app.documents.core.login.CloudLoginRepository
import app.documents.core.login.PortalResult
import app.documents.core.model.cloud.CloudPortal
import app.documents.core.model.cloud.PortalSettings
import app.documents.core.model.cloud.Scheme
import app.editors.manager.BuildConfig
import app.editors.manager.R
import app.editors.manager.app.App
import app.editors.manager.managers.utils.FirebaseUtils
import app.editors.manager.viewModels.base.BaseViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import lib.toolkit.base.managers.utils.StringUtils
import java.util.TreeSet

sealed class EnterprisePortalState {
    data object Progress : EnterprisePortalState()
    data class Success(val isHttp: Boolean) : EnterprisePortalState()
    data class Error(val message: Int? = null) : EnterprisePortalState()
}

class EnterprisePortalViewModel : BaseViewModel() {

    companion object {
        val TAG: String = EnterprisePortalViewModel::class.java.simpleName

        @Suppress("KotlinConstantConditions")
        private val BANNED_ADDRESSES: Set<String> = object : TreeSet<String>() {
            init {
                if (BuildConfig.APPLICATION_ID == "com.onlyoffice.documents") {
                    add(".r7-")
                }
            }
        }
        private const val TAG_SSH = "/#ssloff"
    }

    private val loginRepository: CloudLoginRepository
        get() = App.getApp().loginComponent.cloudLoginRepository

    private val _portalStateLiveData = MutableLiveData<EnterprisePortalState>()
    val portalStateLiveData: LiveData<EnterprisePortalState> = _portalStateLiveData

    val portals: StateFlow<Array<String>> = loginRepository.getSavedPortals()
        .map { it.toTypedArray() }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyArray())

    private var job: Job? = null

    fun cancel() {
        job?.cancel()
        _portalStateLiveData.value = EnterprisePortalState.Error(null)
    }

    fun checkPortal(portal: String) {
        val builder = StringBuilder()
        var sslState = true

        if (checkBannedAddress(portal)) {
            _portalStateLiveData.value = EnterprisePortalState.Error(R.string.errors_client_host_not_found)
            return
        }

        if (portal.endsWith(TAG_SSH)) {
            sslState = false
            builder.append(portal.replace(TAG_SSH, ""))
        } else {
            builder.append(portal)
        }

        if (builder.isEmpty()) {
            _portalStateLiveData.value = EnterprisePortalState.Error(R.string.login_enterprise_edit_error_hint)
            return
        }

        _portalStateLiveData.value = EnterprisePortalState.Progress

        job = viewModelScope.launch {
            tryCheckPortal(StringUtils.getUrlHost(portal), Scheme.Https, sslState)
        }
    }

    private suspend fun tryCheckPortal(url: String, scheme: Scheme, sslState: Boolean) {
        App.getApp().refreshLoginComponent(
            CloudPortal(
                url = url,
                scheme = scheme,
                settings = PortalSettings(isSslState = sslState)
            )
        )

        try {
            loginRepository.checkPortal(url, scheme)
                .collect { result ->
                    when (result) {
                        is PortalResult.Error -> onError(url, result.exception)
                        is PortalResult.ShouldUseHttp -> tryCheckPortal(url, Scheme.Http, sslState)
                        is PortalResult.Success -> onSuccess(result.cloudPortal)
                    }
                }
        } catch (error: IllegalArgumentException) {
            delay(500)
            _portalStateLiveData.value = EnterprisePortalState.Error(R.string.login_enterprise_edit_error_hint)
            return
        }

    }

    private fun onSuccess(portal: CloudPortal) {
        App.getApp().refreshLoginComponent(portal)
        _portalStateLiveData.value = EnterprisePortalState.Success(portal.scheme == Scheme.Http)
    }

    private fun onError(portal: String, exception: Throwable) {
        FirebaseUtils.addAnalyticsCheckPortal(
            portal,
            FirebaseUtils.AnalyticsKeys.FAILED,
            "Error: " + exception.message
        )
        _portalStateLiveData.value = EnterprisePortalState.Error(R.string.errors_sign_in_splash_error)
    }

    private fun checkBannedAddress(portal: String): Boolean {
        for (item in BANNED_ADDRESSES) {
            if (portal.contains(item)) {
                return true
            }
        }
        return false
    }
}