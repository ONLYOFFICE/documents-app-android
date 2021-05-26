package app.editors.manager.mvp.presenters.login

import app.documents.core.login.LoginResponse
import app.documents.core.network.ApiContract
import app.documents.core.network.models.login.Capabilities
import app.documents.core.network.models.login.response.ResponseCapabilities
import app.documents.core.network.models.login.response.ResponseSettings
import app.editors.manager.R
import app.editors.manager.app.App
import app.editors.manager.managers.utils.FirebaseUtils
import app.editors.manager.mvp.views.login.EnterprisePortalView
import io.reactivex.disposables.Disposable
import moxy.InjectViewState
import java.util.*

@InjectViewState
class EnterprisePortalPresenter : BaseLoginPresenter<EnterprisePortalView>() {

    companion object {
        val TAG: String = EnterprisePortalPresenter::class.java.simpleName
        private val BANNED_ADDRESSES: Set<String> = object : TreeSet<String>() {
            init {
                add(".r7-")
            }
        }
        private const val TAG_SSH = "/#ssloff"
    }

    init {
        App.getApp().appComponent.inject(this)
    }

    private var mDisposable: Disposable? = null

    override fun onDestroy() {
        mDisposable?.dispose()
    }

    override fun cancelRequest() {
        super.cancelRequest()
        mDisposable?.dispose()
    }

    fun checkPortal(portal: String) {
        networkSettings.setDefault()

        val builder = StringBuilder()

        if (checkBannedAddress(portal)) {
            viewState.onError(context.getString(R.string.errors_client_host_not_found))
            return
        }

        if (portal.endsWith(TAG_SSH)) {
            networkSettings.setSslState(false)
            builder.append(getPortal(portal.replace(TAG_SSH, "")) ?: "")
        } else {
            builder.append(getPortal(portal))
        }

        if (builder.isEmpty()) {
            viewState.onPortalSyntax(context.getString(R.string.login_enterprise_edit_error_hint))
            return
        }

        viewState.onShowDialog()
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
        val service = App.getApp().loginComponent.loginService
        mDisposable = service.capabilities()
            .subscribe({ response ->
                if (response is LoginResponse.Success) {
                    if (response.response is ResponseCapabilities) {
                        val capability = (response.response as ResponseCapabilities).response
                        setSettings(capability)
                        if (networkSettings.getScheme() == ApiContract.SCHEME_HTTPS) {
                            viewState.onSuccessPortal(networkSettings.getPortal(), capability.providers.toTypedArray())
                        } else {
                            viewState.onHttpPortal(networkSettings.getPortal(), capability.providers.toTypedArray())
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
            onFailureHandle(throwable)
        }
    }

}