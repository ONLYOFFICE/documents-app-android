package app.editors.manager.ui.fragments.login

import android.webkit.WebResourceRequest
import app.documents.core.model.login.OidcConfiguration
import kotlinx.serialization.json.Json
import lib.toolkit.base.managers.utils.StringUtils
import lib.toolkit.base.managers.utils.putArgs
import kotlin.text.orEmpty

class OwnCloudWebLoginFragment : WebViewCloudLoginFragment() {
    companion object {
        @JvmField
        val TAG: String = OwnCloudWebLoginFragment::class.java.simpleName

        private const val KEY_AUTH_URL = "KEY_PORTAL"
        private const val KEY_CONFIGURATION = "KEY_CONFIGURATION"
        const val TAG_CODE = "code"

        @JvmStatic
        fun newInstance(authUrl: String, config: String): OwnCloudWebLoginFragment {
            return OwnCloudWebLoginFragment().putArgs(
                KEY_CONFIGURATION to config,
                KEY_AUTH_URL to authUrl
            )
        }
    }

    private val config: OidcConfiguration? by lazy {
        arguments?.getString(KEY_CONFIGURATION)?.let { json ->
            Json.decodeFromString<OidcConfiguration>(json)
        }
    }

    override val loginUrl: String by lazy {
        arguments?.getString(KEY_AUTH_URL).orEmpty()
    }

    override fun handleShouldOverrideUrlLoading(request: WebResourceRequest): Boolean = false

    override fun handlePageStarted(url: String) {
        if (url.contains("#$TAG_CODE")) {
            config?.let {
                val parametersMap = StringUtils.getParametersFromUrl(url.split("#")[1])
                val code = parametersMap[TAG_CODE].orEmpty()
                viewModel.saveOwnCloudUser(code = code, configuration = it)
            }
        } else {
            viewBinding?.swipeRefresh?.isRefreshing = true
        }
    }
}