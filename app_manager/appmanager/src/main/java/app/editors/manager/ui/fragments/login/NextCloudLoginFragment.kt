package app.editors.manager.ui.fragments.login

import android.annotation.SuppressLint
import android.app.Activity
import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.JsResult
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import app.editors.manager.R
import app.editors.manager.app.App
import app.editors.manager.databinding.NextCloudLoginLayoutBinding
import app.editors.manager.ui.activities.main.MainActivity
import app.editors.manager.ui.fragments.base.BaseAppFragment
import app.editors.manager.viewModels.login.NextCloudLogin
import app.editors.manager.viewModels.login.NextCloudLoginViewModel
import kotlinx.coroutines.launch
import lib.toolkit.base.managers.utils.NetworkUtils.clearCookies
import lib.toolkit.base.managers.utils.putArgs

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
            return NextCloudLoginFragment().putArgs(KEY_PORTAL to portal)
        }
    }

    private val headers: Map<String, String> by lazy {
        hashMapOf(
            LOGIN_HEADER to "true",
            "USER_AGENT" to getString(R.string.app_name),
            "ACCEPT_LANGUAGE" to App.getLocale()
        )
    }

    private val viewModel: NextCloudLoginViewModel by viewModels()

    private var viewBinding: NextCloudLoginLayoutBinding? = null

    private val portal: String? by lazy { arguments?.getString(KEY_PORTAL) }
    private var isClear = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        viewBinding = NextCloudLoginLayoutBinding.inflate(inflater, container, false)
        return viewBinding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initWebView()
        collectState()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewBinding = null
        clearCookies()
        viewBinding?.webView?.clearCache(true)
        viewBinding?.webView?.webChromeClient = null
    }

    override fun onBackPressed(): Boolean {
        return if (viewBinding?.webView?.canGoBack() == true) {
            viewBinding?.webView?.goBack()
            true
        } else {
            requireActivity().setResult(Activity.RESULT_CANCELED)
            requireActivity().finish()
            super.onBackPressed()
        }
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
    private fun initWebView() {
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
                        viewModel.saveUser(path)
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
                viewModel.saveUser(url.substring(url.indexOf(":") + 1))
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