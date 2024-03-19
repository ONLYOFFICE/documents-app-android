package app.editors.manager.ui.fragments.login

import android.accounts.Account
import android.annotation.SuppressLint
import android.app.Activity
import android.graphics.Bitmap
import android.net.http.SslError
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.*
import app.documents.core.model.cloud.CloudAccount
import app.documents.core.model.cloud.CloudPortal
import app.documents.core.model.cloud.PortalProvider
import app.documents.core.model.cloud.Scheme
import app.documents.core.model.cloud.WebdavProvider
import app.documents.core.model.cloud.WebdavProvider.Companion.DEFAULT_NEXT_CLOUD_PATH
import app.editors.manager.R
import app.editors.manager.app.App
import app.editors.manager.databinding.NextCloudLoginLayoutBinding
import app.editors.manager.ui.activities.main.MainActivity
import app.editors.manager.ui.fragments.base.BaseAppFragment
import lib.toolkit.base.managers.utils.AccountData
import lib.toolkit.base.managers.utils.AccountUtils
import lib.toolkit.base.managers.utils.NetworkUtils.clearCookies
import java.net.MalformedURLException
import java.net.URL

class NextCloudLoginFragment : BaseAppFragment() {

    companion object {
        @JvmField
        val TAG: String = NextCloudLoginFragment::class.java.simpleName

        private const val KEY_PORTAL = "KEY_PORTAL"
        private const val LOGIN_SUFFIX = "/index.php/login/flow"
        private const val LOGIN_HEADER = "OCS-APIREQUEST"
        private const val BACK_PATTERN_1 = "apps"
        private const val BACK_PATTERN_2 = "files"

        @JvmStatic
        fun newInstance(portal: String?): NextCloudLoginFragment {
            return NextCloudLoginFragment().apply {
                arguments = Bundle().apply {
                    putString(KEY_PORTAL, portal)
                }
            }
        }
    }

    private val headers: Map<String, String> by lazy {
        hashMapOf(
            LOGIN_HEADER to "true",
            "USER_AGENT" to getString(R.string.app_name),
            "ACCEPT_LANGUAGE" to App.getLocale()
        )
    }

    private var portal: String? = null
    private var isClear = false

    private var viewBinding: NextCloudLoginLayoutBinding? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        viewBinding = NextCloudLoginLayoutBinding.inflate(inflater, container, false)
        return viewBinding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getArgs()
    }

    private fun collectState() {
        lifecycleScope.launch {
            viewModel.state.collect { state ->
                when (state) {
                    NextCloudLogin.Error -> showSnackBar(R.string.errors_unknown_error)
                    NextCloudLogin.Success -> {
                        with(requireActivity()) {
                            setResult(Activity.RESULT_OK)
                            finish()
                            MainActivity.show(this)
                        }
                    }
                    else -> Unit
                }
            }
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun getArgs() {
        val args = arguments
        if (args != null && args.containsKey(KEY_PORTAL)) {
            portal = args.getString(KEY_PORTAL)
        }
        viewBinding?.webView?.apply {
            settings.javaScriptEnabled = true
            settings.userAgentString = getString(R.string.app_name_full)
            webViewClient = WebViewCallbacks()
            webChromeClient = WebViewChromeClient()
            clearHistory()
            clearCache(true)
            loadUrl(portal + LOGIN_SUFFIX, headers)
        }
    }

    private inner class WebViewCallbacks : WebViewClient() {
        override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
            val uri = request.url
            if (uri != null) {
                if (uri.scheme != null && uri.scheme == "nc" && uri.host != null && uri.host == "login") {
                    val path = uri.path
                    if (path != null) {
                        saveUser(path)
                        return true
                    }
                }
                if (uri.toString().contains(BACK_PATTERN_1) || uri.path?.contains(BACK_PATTERN_2) == true) {
                    isClear = true
                    clearCookies()
                    viewBinding?.webView?.clearHistory()
                    viewBinding?.webView?.clearCache(true)
                    viewBinding?.webView?.loadUrl(portal + LOGIN_SUFFIX, headers)
                    return true
                }
            }
            return super.shouldOverrideUrlLoading(view, request)
        }

        override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
            if (url.contains("nc") && url.contains("login")) {
                saveUser(url.substring(url.indexOf(":") + 1))
            } else {
                viewBinding?.swipeRefresh?.isRefreshing = true
            }
        }

        override fun onPageFinished(view: WebView, url: String) {
            if (isClear) {
                viewBinding?.webView?.clearHistory()
                isClear = false
            }
            viewBinding?.swipeRefresh?.isRefreshing = false
        }

        override fun onReceivedError(view: WebView, request: WebResourceRequest, error: WebResourceError) {
            super.onReceivedError(view, request, error)
        }

        override fun onReceivedHttpError(
            view: WebView,
            request: WebResourceRequest,
            errorResponse: WebResourceResponse
        ) {
            super.onReceivedHttpError(view, request, errorResponse)
        }

        override fun onReceivedSslError(view: WebView, handler: SslErrorHandler, error: SslError) {
            super.onReceivedSslError(view, handler, error)
        }
    }

    private inner class WebViewChromeClient : WebChromeClient() {
        override fun onReceivedTitle(view: WebView, title: String) {
            setActionBarTitle(title)
        }

        override fun onJsAlert(view: WebView, url: String, message: String, result: JsResult): Boolean {
            showToast(message)
            return super.onJsAlert(view, url, message, result)
        }
    }
}