package app.editors.manager.onedrive.ui.fragments

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.*
import androidx.core.content.ContextCompat
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import app.editors.manager.R
import app.editors.manager.databinding.FragmentStorageWebBinding
import app.editors.manager.managers.utils.StorageUtils
import app.editors.manager.mvp.models.account.Storage
import app.editors.manager.onedrive.mvp.presenters.OneDriveSingInPresenter
import app.editors.manager.onedrive.mvp.views.OneDriveSignInView
import app.editors.manager.ui.activities.main.MainActivity
import app.editors.manager.ui.fragments.base.BaseAppFragment
import lib.toolkit.base.managers.utils.NetworkUtils.isOnline
import moxy.presenter.InjectPresenter

class OneDriveSignInFragment : BaseAppFragment(), SwipeRefreshLayout.OnRefreshListener,
    OneDriveSignInView {

    companion object {
        val TAG = OneDriveSignInFragment::class.java.simpleName
        private val TAG_STORAGE = "TAG_MEDIA"
        private val TAG_WEB_VIEW = "TAG_WEB_VIEW"
        private val TAG_PAGE_LOAD = "TAG_PAGE_LOAD"

        fun newInstance(storage: Storage) = OneDriveSignInFragment().apply {
            arguments = Bundle(1).apply {
                putParcelable(TAG_STORAGE, storage)
            }
        }
    }

    var viewBinding: FragmentStorageWebBinding? = null

    private var url: String? = null
    private var storage: Storage? = null
    private var redirectUrl: String? = null
    private var isPageLoad = false

    @InjectPresenter
    lateinit var presenter: OneDriveSingInPresenter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        viewBinding = FragmentStorageWebBinding.inflate(inflater)
        return viewBinding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init(savedInstanceState)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(TAG_PAGE_LOAD, isPageLoad)

        viewBinding?.webStorageWebview?.let {
            val bundle = Bundle()
            it.saveState(bundle)
            outState.putBundle(TAG_WEB_VIEW, bundle)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        CookieManager.getInstance().removeAllCookies(null)
    }

    override fun onRefresh() {
        loadWebView(url)
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun init(savedInstanceState: Bundle?) {
        setActionBarTitle(getString(R.string.storage_web_title))

        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setHomeButtonEnabled(true)
        }

        isPageLoad = false
        viewBinding?.webStorageSwipe?.apply {
            setOnRefreshListener(this@OneDriveSignInFragment)
            setColorSchemeColors(
                ContextCompat.getColor(
                    requireContext(),
                    lib.toolkit.base.R.color.colorSecondary
                )
            )
        }

        viewBinding?.webStorageWebview?.apply {
            settings.apply {
                javaScriptEnabled = true
                setAppCacheEnabled(false)
                cacheMode = WebSettings.LOAD_NO_CACHE
                userAgentString = getString(R.string.google_user_agent)
            }
            webViewClient = WebViewCallbacks()
            clearHistory()
        }

        getArgs()
        restoreStates(savedInstanceState)
    }

    private fun getArgs() {
        arguments?.let { it ->
            storage = it.getParcelable(TAG_STORAGE)
            storage?.let {storage ->
                url = StorageUtils.getStorageUrl(
                    storage.name,
                    storage.clientId,
                    storage.redirectUrl
                )
                redirectUrl = storage.redirectUrl
            }
        }
    }

    private fun restoreStates(savedInstanceState: Bundle?) {
        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(TAG_WEB_VIEW)) {
                val bundle = savedInstanceState.getBundle(TAG_WEB_VIEW)
                viewBinding?.webStorageSwipe?.isRefreshing = true
                if (bundle != null) {
                    viewBinding?.webStorageWebview?.restoreState(bundle)
                }
            }
            if (savedInstanceState.containsKey(TAG_PAGE_LOAD)) {
                isPageLoad = savedInstanceState.getBoolean(TAG_PAGE_LOAD)
            }
        } else {
            loadWebView(url)
        }
    }

    private fun loadWebView(url: String?) {
        viewBinding?.webStorageSwipe?.isRefreshing = true
        viewBinding?.webStorageWebview?.loadUrl(url!!)
    }

    /*
     * WebView callback class
     * Example token response:
     *       https://service.teamlab.info/oauth2.aspx?code=4/AAAJYg3drTzabIIAPiYq_FEieoyhj7FqOjON8k0l3kEN3v5Qc3xmA_Hqp3TxSa5aiwSSToMJefTDDZcrJJLfguQ#
     *       https://login.live.com/err.srf?lc=1049#error=invalid_request&error_description=The+provided+value+for+the+input+parameter+'redirect_uri'+is+not+valid.+The+expected+value+is+'https://login.live.com/oauth20_desktop.srf'+or+a+URL+which+matches+the+redirect+URI+registered+for+this+client+application.
     *       https://login.live.com/oauth20_authorize.srf?client_id=000000004413039F&redirect_uri=https://service.teamlab.info/oauth2.aspx&response_type=code&scope=wl.signin%20wl.skydrive_update%20wl.offline_access
     * */
    inner class WebViewCallbacks : WebViewClient() {
        override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
            super.onPageStarted(view, url, favicon)
            if (url.startsWith(redirectUrl!!)) {
                val response = url.split("=")
                val code = response[1]
                presenter.getToken(code)
                //presenter.checkOneDrive(token.removeRange(token.length - 11, token.length))
            }
        }

        override fun onPageFinished(view: WebView, url: String) {
            super.onPageFinished(view, url)
            viewBinding?.webStorageSwipe?.isRefreshing = false
            isPageLoad = true
        }

        override fun onReceivedError(
            view: WebView,
            request: WebResourceRequest,
            error: WebResourceError
        ) {
            super.onReceivedError(view, request, error)
            viewBinding?.webStorageSwipe?.isRefreshing = false
            if (!isOnline(requireContext())) {
                showSnackBar(R.string.errors_connection_error)
            }
        }
    }

    override fun onLogin() {
        MainActivity.show(requireContext())
        requireActivity().finish()
    }

    override fun onError(message: String?) {

    }
}