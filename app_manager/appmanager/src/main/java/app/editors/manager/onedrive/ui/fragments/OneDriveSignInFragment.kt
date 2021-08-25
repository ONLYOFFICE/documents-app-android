package app.editors.manager.onedrive.ui.fragments

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.*
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.content.ContextCompat
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import app.editors.manager.R
import app.editors.manager.managers.utils.StorageUtils
import app.editors.manager.mvp.models.account.Storage
import app.editors.manager.onedrive.mvp.presenters.OneDriveSingInPresenter
import app.editors.manager.onedrive.mvp.views.OneDriveSignInView
import app.editors.manager.ui.activities.main.MainActivity
import app.editors.manager.ui.fragments.base.BaseAppFragment
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.Unbinder
import lib.toolkit.base.managers.utils.NetworkUtils.isOnline
import moxy.presenter.InjectPresenter
import java.net.URLEncoder

class OneDriveSignInFragment : BaseAppFragment(), SwipeRefreshLayout.OnRefreshListener,
    OneDriveSignInView {

    companion object {
        val TAG = OneDriveSignInFragment::class.java.simpleName
        private val TAG_STORAGE = "TAG_MEDIA"
        private val TAG_WEB_VIEW = "TAG_WEB_VIEW"
        private val TAG_PAGE_LOAD = "TAG_PAGE_LOAD"

        fun newInstance(storage: Storage): OneDriveSignInFragment {
            val bundle = Bundle()
            bundle.putParcelable(TAG_STORAGE, storage)
            val fileViewerFragment = OneDriveSignInFragment()
            fileViewerFragment.arguments = bundle
            return fileViewerFragment
        }
    }

    @JvmField
    @BindView(R.id.web_storage_layout)
    protected var mWebLayout: LinearLayoutCompat? = null

    @JvmField
    @BindView(R.id.web_storage_webview)
    protected var mWebView: WebView? = null

    @JvmField
    @BindView(R.id.web_storage_swipe)
    protected var mSwipeRefreshLayout: SwipeRefreshLayout? = null

    private var mUnbinder: Unbinder? = null
    private var mUrl: String? = null
    private var mStorage: Storage? = null
    private var mRedirectUrl: String? = null
    private var mIsPageLoad = false

    @InjectPresenter
    lateinit var presenter: OneDriveSingInPresenter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)
        val view: View = inflater.inflate(R.layout.fragment_storage_web, container, false)
        mUnbinder = ButterKnife.bind(this, view)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init(savedInstanceState)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(TAG_PAGE_LOAD, mIsPageLoad)

        // Save WebView state
        if (mWebView != null) {
            val bundle = Bundle()
            mWebView!!.saveState(bundle)
            outState.putBundle(TAG_WEB_VIEW, bundle)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        CookieManager.getInstance().removeAllCookies(null)
        mUnbinder?.unbind()
    }

    override fun onRefresh() {
        loadWebView(mUrl)
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun init(savedInstanceState: Bundle?) {
        setActionBarTitle(getString(R.string.storage_web_title))

        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setHomeButtonEnabled(true)
        }

        mIsPageLoad = false
        mSwipeRefreshLayout?.apply {
            setOnRefreshListener(this@OneDriveSignInFragment)
            setColorSchemeColors(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.colorSecondary
                )
            )
        }

        mWebView?.apply {
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
        val bundle = arguments
        if (bundle != null) {
            mStorage = bundle.getParcelable(TAG_STORAGE)
            mStorage?.let {
                mUrl = StorageUtils.getStorageUrl(
                    it.name,
                    it.clientId,
                    it.redirectUrl
                )
                mRedirectUrl = it.redirectUrl
            }
        }
    }

    private fun restoreStates(savedInstanceState: Bundle?) {
        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(TAG_WEB_VIEW)) {
                val bundle = savedInstanceState.getBundle(TAG_WEB_VIEW)
                mSwipeRefreshLayout?.isRefreshing = true
                if (bundle != null) {
                    mWebView?.restoreState(bundle)
                }
            }
            if (savedInstanceState.containsKey(TAG_PAGE_LOAD)) {
                mIsPageLoad = savedInstanceState.getBoolean(TAG_PAGE_LOAD)
            }
        } else {
            loadWebView(mUrl)
        }
    }

    private fun pairsToUrlQueryString(pairs: Map<String, String>): String {
        return pairs.entries.joinToString("&") {
            "${it.key}=${
                URLEncoder.encode(
                    it.value,
                    "UTF-8"
                )
            }"
        }
    }

    private fun loadWebView(url: String?) {
        mSwipeRefreshLayout!!.isRefreshing = true
        mWebView?.loadUrl(url!!)
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
            if (url.startsWith(mRedirectUrl!!)) {
                val uri = Uri.parse(url)
                val accessToken = url.split("=")
                val token = accessToken[1]
                presenter.checkOneDrive(token.removeRange(token.length - 11, token.length))
            }
        }

        override fun onPageFinished(view: WebView, url: String) {
            super.onPageFinished(view, url)
            mSwipeRefreshLayout?.isRefreshing = false
            mIsPageLoad = true
        }

        override fun onReceivedError(
            view: WebView,
            request: WebResourceRequest,
            error: WebResourceError
        ) {
            super.onReceivedError(view, request, error)
            mSwipeRefreshLayout?.isRefreshing = false
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