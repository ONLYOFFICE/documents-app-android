package app.editors.manager.ui.fragments.base

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.WebSettings
import android.webkit.WebViewClient
import androidx.core.content.ContextCompat
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import app.editors.manager.R
import app.editors.manager.databinding.FragmentStorageWebBinding
import app.editors.manager.managers.utils.FirebaseUtils
import app.editors.manager.managers.utils.StorageUtils
import app.documents.core.network.common.models.Storage
import app.editors.manager.mvp.views.base.BaseStorageSignInView
import app.editors.manager.ui.activities.main.MainActivity

abstract class BaseStorageSignInFragment: BaseAppFragment(), SwipeRefreshLayout.OnRefreshListener,
    BaseStorageSignInView {


    companion object {
        val TAG: String = BaseStorageSignInFragment::class.java.simpleName
        const val TAG_STORAGE = "TAG_MEDIA"
        const val TAG_WEB_VIEW = "TAG_WEB_VIEW"
        const val TAG_PAGE_LOAD = "TAG_PAGE_LOAD"

        const val TAG_ACCESS_TOKEN = "access_token"
        const val TAG_CODE = "code"
        const val TAG_ACCOUNT_ID = "account_id"
    }

    abstract fun getWebViewCallback(): WebViewClient

    var viewBinding: FragmentStorageWebBinding? = null

    private var url: String? = null
    private var storage: Storage? = null
    var redirectUrl: String? = null
    var isPageLoad = false

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
        viewBinding = null
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
            setOnRefreshListener(this@BaseStorageSignInFragment)
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
                cacheMode = WebSettings.LOAD_NO_CACHE
                userAgentString = getString(R.string.google_user_agent)
            }
            webViewClient = getWebViewCallback()
            clearHistory()
        }

        getArgs()
        restoreStates(savedInstanceState)
    }

    private fun getArgs() {
        arguments?.let { it ->
            storage = it.getParcelable(TAG_STORAGE)
            storage?.let { storage ->
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

    protected open fun loadWebView(url: String?) {
        viewBinding?.webStorageSwipe?.isRefreshing = true
        url?.let { viewBinding?.webStorageWebview?.loadUrl(it) }
    }


    override fun onLogin() {
        MainActivity.show(requireContext())
        requireActivity().finish()
    }

    override fun onStartLogin() {
        viewBinding?.webStorageSwipe?.isRefreshing = true
    }

    override fun onError(message: String?) {
        Log.e(TAG, "onError: $message")
        message?.let { FirebaseUtils.addCrash(it) }
    }
}