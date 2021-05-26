package app.editors.manager.ui.fragments.login

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
import app.editors.manager.R
import app.editors.manager.databinding.SsoLoginLayoutBinding
import app.editors.manager.mvp.presenters.login.EnterpriseSSOPresenter
import app.editors.manager.mvp.views.login.EnterpriseSSOView
import app.editors.manager.ui.activities.main.MainActivity
import app.editors.manager.ui.fragments.base.BaseAppFragment
import lib.toolkit.base.managers.utils.NetworkUtils.clearCookies
import moxy.presenter.InjectPresenter

class SSOLoginFragment : BaseAppFragment(), EnterpriseSSOView {

    companion object {
        val TAG: String = SSOLoginFragment::class.java.simpleName

        const val KEY_URL = "KEY_URL"

        fun newInstance(url: String?): SSOLoginFragment {
            return SSOLoginFragment().apply {
                arguments = Bundle().apply {
                    putString(KEY_URL, url)
                }
            }
        }
    }

    private var url: String? = null

    @InjectPresenter
    lateinit var enterpriseSSOPresenter: EnterpriseSSOPresenter

    private var viewBinding: SsoLoginLayoutBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewBinding = SsoLoginLayoutBinding.inflate(inflater)
        return viewBinding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getArgs()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        clearCookies(requireContext())
        viewBinding?.webView?.apply {
            clearCache(true)
            webChromeClient = null
        }
        viewBinding = null
    }

    override fun onBackPressed(): Boolean {
        return if (viewBinding?.webView!!.canGoBack()) {
            viewBinding?.webView?.goBack()
            true
        } else {
            requireActivity().setResult(Activity.RESULT_CANCELED)
            requireActivity().finish()
            super.onBackPressed()
        }
    }



    override fun onSuccessLogin() {
        hideDialog()
        MainActivity.show(requireContext())
        requireActivity().finish()
    }

    override fun onError(message: String?) {
        showSnackBar(message!!)
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun getArgs() {
        val args = arguments
        if (args != null && args.containsKey(KEY_URL)) {
            url = args.getString(KEY_URL)
        }
        viewBinding?.webView?.apply {
            settings.javaScriptEnabled = true
            settings.userAgentString = getString(R.string.app_name_full)
            webViewClient = WebViewCallbacks()
            webChromeClient = WebViewChromeClient()
            clearHistory()
            clearCache(true)
            loadUrl(this@SSOLoginFragment.url ?: "")
        }
    }

    private inner class WebViewCallbacks : WebViewClient() {

        override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
            val args = url.split("=")
            if (url.contains("token")) {
                enterpriseSSOPresenter.signInWithSSO(args[1])
            } else if (url.contains("error")) {
                onError(args[1])
            }
        }

        override fun onReceivedSslError(view: WebView, handler: SslErrorHandler, error: SslError) {
            handler.proceed()
            //super.onReceivedSslError(view, handler, error);
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