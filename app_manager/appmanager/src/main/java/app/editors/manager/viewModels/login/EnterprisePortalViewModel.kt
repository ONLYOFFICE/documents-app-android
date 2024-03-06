package app.editors.manager.viewModels.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import app.documents.core.login.PortalResult
import app.documents.core.model.cloud.Scheme
import app.editors.manager.BuildConfig
import app.editors.manager.R
import app.editors.manager.app.App
import app.editors.manager.managers.utils.FirebaseUtils
import app.editors.manager.viewModels.base.BaseLoginViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.util.TreeSet

sealed class EnterprisePortalState {
    data object Progress : EnterprisePortalState()
    data class Success(val portal: String, val providers: Array<String>, val isHttp: Boolean) : EnterprisePortalState()
    data class Error(val message: Int? = null) : EnterprisePortalState()
}

class EnterprisePortalViewModel : BaseLoginViewModel() {

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

    private val _portalStateLiveData = MutableLiveData<EnterprisePortalState>()
    val portalStateLiveData: LiveData<EnterprisePortalState> = _portalStateLiveData

    private var job: Job? = null

    fun cancel() {
        job?.cancel()
        _portalStateLiveData.value = EnterprisePortalState.Error(null)
    }

    fun checkPortal(portal: String) {
        val builder = StringBuilder()

        if (checkBannedAddress(portal)) {
            _portalStateLiveData.value = EnterprisePortalState.Error(R.string.errors_client_host_not_found)
            return
        }

        if (portal.endsWith(TAG_SSH)) {
            networkSettings.setSslState(false)
            builder.append(getPortal(portal.replace(TAG_SSH, "")) ?: "")
        } else {
            builder.append(getPortal(portal))
        }

        if (builder.isEmpty()) {
            _portalStateLiveData.value = EnterprisePortalState.Error(R.string.login_enterprise_edit_error_hint)
            return
        }

        _portalStateLiveData.value = EnterprisePortalState.Progress

        job = viewModelScope.launch {
            tryCheckPortal(portal, Scheme.Https)
        }
    }

    private suspend fun tryCheckPortal(portal: String, scheme: Scheme) {
        App.getApp().refreshLoginComponent(portal, scheme)
        loginRepository.checkPortal(portal, scheme)
            .collect { result ->
                when (result) {
                    is PortalResult.Error -> onError(result.exception)
                    is PortalResult.Success -> onSuccess(portal, result)
                    is PortalResult.ShouldUseHttp -> tryCheckPortal(portal, Scheme.Http)
                }
            }
    }

    private fun onSuccess(portal: String, result: PortalResult.Success) {
        _portalStateLiveData.value =
            EnterprisePortalState.Success(portal, result.providers.toTypedArray(), result.isHttp)
    }

    private fun onError(exception: Throwable) {
        FirebaseUtils.addAnalyticsCheckPortal(
            networkSettings.getPortal(),
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