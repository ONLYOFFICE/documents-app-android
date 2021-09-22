package app.editors.manager.ui.fragments.login

import android.accounts.Account
import android.annotation.SuppressLint
import android.app.Activity
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.http.SslError
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.*
import app.documents.core.account.CloudAccount
import app.documents.core.webdav.WebDavApi
import app.editors.manager.R
import app.editors.manager.app.App
import app.editors.manager.app.appComponent
import app.editors.manager.databinding.NextCloudLoginLayoutBinding
import app.editors.manager.ui.activities.main.MainActivity
import app.editors.manager.ui.fragments.base.BaseAppFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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

    private var portla: String? = null
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

    override fun onDestroyView() {
        super.onDestroyView()
        viewBinding = null
        clearCookies(requireContext())
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

    @SuppressLint("SetJavaScriptEnabled")
    private fun getArgs() {
        val args = arguments
        if (args != null && args.containsKey(KEY_PORTAL)) {
            portla = args.getString(KEY_PORTAL)
        }
        viewBinding?.webView?.apply {
            settings.javaScriptEnabled = true
            settings.userAgentString = getString(R.string.app_name_full)
            webViewClient = WebViewCallbacks()
            webChromeClient = WebViewChromeClient()
            clearHistory()
            clearCache(true)
            loadUrl(portla + LOGIN_SUFFIX, headers)
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
                    clearCookies(requireContext())
                    viewBinding?.webView?.clearHistory()
                    viewBinding?.webView?.clearCache(true)
                    viewBinding?.webView?.loadUrl(portla + LOGIN_SUFFIX, headers)
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

        override fun onReceivedIcon(view: WebView, icon: Bitmap) {
            val actionBar = requireActivity().actionBar
            actionBar?.setIcon(BitmapDrawable(resources, icon))
        }

        override fun onJsAlert(view: WebView, url: String, message: String, result: JsResult): Boolean {
            showToast(message)
            return super.onJsAlert(view, url, message, result)
        }
    }

    private fun saveUser(path: String) {
        val args = path.split("&").toTypedArray()
        if (args.size == 3) {
            val portal = args[0].substring(args[0].indexOf(":") + 1)
            val login = args[1].substring(args[1].indexOf(":") + 1)
            val password = args[2].substring(args[2].indexOf(":") + 1)

            try {
                val url = URL(portal)
                val builder = StringBuilder()
                    .append(url.host)
                if (url.path != null && url.path.isNotEmpty()) {
                    builder.append(url.path)
                    if (url.port == -1) {
                        builder.append("/")
                    }
                }
                if (url.port != -1) {
                    builder.append(":").append(url.port)
                }

                val cloudAccount = createCloudAccount(url, login)
                val account = Account(cloudAccount.id, getString(R.string.account_type))

                val accountData = AccountData(
                    portal = cloudAccount.portal ?: "",
                    scheme = cloudAccount.scheme ?: "",
                    displayName = login,
                    userId = cloudAccount.id,
                    provider = cloudAccount.webDavProvider ?: "",
                    webDav = cloudAccount.webDavPath,
                    email = login,
                )

                if (!AccountUtils.addAccount(requireContext(), account, password, accountData)) {
                    AccountUtils.setPassword(requireContext(), account, password)
                    AccountUtils.setAccountData(requireContext(), account, accountData)

                }
                addAccount(cloudAccount)
            } catch (e: MalformedURLException) {
                e.printStackTrace()
            }
        }
    }

    private fun createCloudAccount(url: URL, login: String) = CloudAccount(
        id = "$login@${url.host}",
        isWebDav = true,
        webDavProvider = WebDavApi.Providers.NextCloud.name,
        scheme = url.protocol + "://",
        login = login,
        name = login,
        portal = url.host,
        webDavPath = if (url.path != null && url.path.isNotEmpty()) {
            url.path + WebDavApi.Providers.NextCloud.path + login + "/"
        } else {
            WebDavApi.Providers.NextCloud.path + login + "/"
        }
    )

    private fun addAccount(cloudAccount: CloudAccount) {
        val accountDao = requireContext().appComponent.accountsDao
        CoroutineScope(Dispatchers.Default).launch {
            accountDao.getAccountOnline()?.let {
                accountDao.addAccount(it.copy(isOnline = false))
            }
            accountDao.addAccount(cloudAccount.copy(isOnline = true))
            withContext(Dispatchers.Main) {
                login()
            }
        }
    }

    private fun login() {
        MainActivity.show(requireContext())
        requireActivity().setResult(Activity.RESULT_OK)
        requireActivity().finish()
    }

}