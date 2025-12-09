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
import app.editors.manager.databinding.WebViewCloudLoginLayoutBinding
import app.editors.manager.ui.activities.main.MainActivity
import app.editors.manager.ui.fragments.base.BaseAppFragment
import app.editors.manager.viewModels.login.WebViewCloudLogin
import app.editors.manager.viewModels.login.WebViewCloudLoginViewModel
import kotlinx.coroutines.launch
import lib.toolkit.base.managers.utils.NetworkUtils.clearCookies

abstract class WebViewCloudLoginFragment : BaseAppFragment() {
    companion object {
        private const val LOGIN_HEADER = "OCS-APIREQUEST"
    }

    protected val headers: Map<String, String> by lazy {
        hashMapOf(
            LOGIN_HEADER to "true",
            "USER_AGENT" to getString(R.string.app_name),
            "ACCEPT_LANGUAGE" to App.getLocale()
        )
    }

    protected val viewModel: WebViewCloudLoginViewModel by viewModels { WebViewCloudLoginViewModel.Factory }
    protected var viewBinding: WebViewCloudLoginLayoutBinding? = null

    protected var isClear = false
    protected abstract val loginUrl: String

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewBinding = WebViewCloudLoginLayoutBinding.inflate(inflater, container, false)
        return viewBinding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initWebView()
        collectState()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewBinding?.apply {
            webView.clearCache(true)
            webView.webChromeClient = null
        }
        clearCookies()
        viewBinding = null
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
                    WebViewCloudLogin.Error -> showSnackBar(R.string.errors_unknown_error)
                    WebViewCloudLogin.Success -> {
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
            loadUrl(loginUrl, headers)
        }
    }

    protected abstract fun handleShouldOverrideUrlLoading(request: WebResourceRequest): Boolean
    protected abstract fun handlePageStarted(url: String)

    private inner class WebViewCallbacks : WebViewClient() {
        override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
            return if (handleShouldOverrideUrlLoading(request)) {
                true
            } else {
                super.shouldOverrideUrlLoading(view, request)
            }
        }

        override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
            handlePageStarted(url)
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

        override fun onJsAlert(
            view: WebView,
            url: String,
            message: String,
            result: JsResult
        ): Boolean {
            showToast(message)
            return super.onJsAlert(view, url, message, result)
        }
    }
}