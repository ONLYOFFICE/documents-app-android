package app.editors.manager.ui.fragments.login

import android.webkit.WebResourceRequest
import lib.toolkit.base.managers.utils.NetworkUtils.clearCookies
import lib.toolkit.base.managers.utils.putArgs

class NextCloudWebLoginFragment : WebViewCloudLoginFragment() {
    companion object {
        @JvmField
        val TAG: String = NextCloudWebLoginFragment::class.java.simpleName

        private const val KEY_PORTAL = "KEY_PORTAL"
        private const val LOGIN_SUFFIX = "/index.php/login/flow"
        private const val BACK_PATTERN_1 = "apps"
        private const val BACK_PATTERN_2 = "files"

        @JvmStatic
        fun newInstance(portal: String?): NextCloudWebLoginFragment {
            return NextCloudWebLoginFragment().putArgs(KEY_PORTAL to portal)
        }
    }

    override val loginUrl: String by lazy {
        arguments?.getString(KEY_PORTAL).orEmpty() + LOGIN_SUFFIX
    }

    override fun handleShouldOverrideUrlLoading(request: WebResourceRequest): Boolean {
        val uri = request.url
        if (uri != null) {
            if (uri.scheme != null && uri.scheme == "nc" && uri.host != null && uri.host == "login") {
                val path = uri.path
                if (path != null) {
                    viewModel.saveNextCloudUser(path)
                    return true
                }
            }
            if (uri.toString().contains(BACK_PATTERN_1) || uri.path?.contains(BACK_PATTERN_2) == true) {
                isClear = true
                clearCookies()
                viewBinding?.webView?.clearHistory()
                viewBinding?.webView?.clearCache(true)
                viewBinding?.webView?.loadUrl(loginUrl, headers)
                return true
            }
        }
        return false
    }

    override fun handlePageStarted(url: String) {
        if (url.contains("nc") && url.contains("password")) {
            viewModel.saveNextCloudUser(url.substring(url.indexOf(":") + 1))
        } else {
            viewBinding?.swipeRefresh?.isRefreshing = true
        }
    }
}