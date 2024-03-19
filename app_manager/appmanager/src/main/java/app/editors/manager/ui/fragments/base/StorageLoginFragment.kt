package app.editors.manager.ui.fragments.base

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import app.documents.core.network.common.contracts.ApiContract
import app.documents.core.network.common.models.Storage
import app.editors.manager.R
import app.editors.manager.app.App
import app.editors.manager.databinding.FragmentStorageWebBinding
import app.editors.manager.managers.utils.StorageUtils
import app.editors.manager.ui.activities.main.MainActivity
import app.editors.manager.viewModels.login.StorageLoginState
import app.editors.manager.viewModels.login.StorageLoginViewModel
import app.editors.manager.viewModels.login.StorageLoginViewModelFactory
import kotlinx.coroutines.launch
import lib.toolkit.base.managers.utils.StringUtils
import lib.toolkit.base.managers.utils.getParcelableExt
import lib.toolkit.base.managers.utils.putArgs
import lib.toolkit.base.ui.dialogs.common.CommonDialog

class StorageLoginFragment : BaseAppFragment(), SwipeRefreshLayout.OnRefreshListener {

    companion object {

        val TAG: String = StorageLoginFragment::class.java.simpleName
        const val TAG_STORAGE = "TAG_MEDIA"
        const val TAG_WEB_VIEW = "TAG_WEB_VIEW"
        const val TAG_PAGE_LOAD = "TAG_PAGE_LOAD"
        const val TAG_WAITING_DIALOG = "TAG_WAITING_DIALOG"
        const val TAG_CODE = "code"

        fun newInstance(storage: Storage): StorageLoginFragment = StorageLoginFragment()
            .putArgs(TAG_STORAGE to storage)
    }

    private var viewBinding: FragmentStorageWebBinding? = null

    private val viewModel: StorageLoginViewModel by viewModels {
        App.getApp().refreshLoginComponent(null)
        StorageLoginViewModelFactory(
            when (storage?.name) {
                ApiContract.Storage.DROPBOX -> App.getApp().loginComponent.dropboxLoginRepository
                ApiContract.Storage.GOOGLEDRIVE -> App.getApp().loginComponent.googleLoginRepository
                ApiContract.Storage.ONEDRIVE -> App.getApp().loginComponent.onedriveLoginRepository
                else -> error("invalid storage name")
            }
        )
    }

    private val storage: Storage? by lazy { arguments?.getParcelableExt(TAG_STORAGE, Storage::class.java) }
    private val url: String? by lazy { storage?.run { StorageUtils.getStorageUrl(name, clientId, redirectUrl) } }
    private val redirectUrl: String? by lazy { storage?.redirectUrl }
    private var isPageLoad = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return FragmentStorageWebBinding.inflate(inflater)
            .also { viewBinding = it }
            .root
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

    override fun onCancelClick(dialogs: CommonDialog.Dialogs?, tag: String?) {
        super.onCancelClick(dialogs, tag)
        if (tag == TAG_WAITING_DIALOG) {
            viewModel.cancel()
        }
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
            setOnRefreshListener(this@StorageLoginFragment)
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
            webViewClient = StorageWebViewClient()
            clearHistory()
        }

        restoreStates(savedInstanceState)
        lifecycleScope.launch {
            viewModel.state.collect { state ->
                when (state) {
                    is StorageLoginState.Error -> showSnackBar(R.string.errors_unknown_error)
                    StorageLoginState.Progress -> showProgressDialog()
                    StorageLoginState.Success -> onLogin()
                    StorageLoginState.None -> hideDialog()
                }
            }
        }
    }

    private fun loadWebView(url: String?) {
        viewBinding?.webStorageSwipe?.isRefreshing = true
        url?.let { viewBinding?.webStorageWebview?.loadUrl(it) }
    }

    private fun showProgressDialog() {
        showWaitingDialog(
            getString(R.string.dialogs_wait_title_storage),
            getString(R.string.dialogs_common_cancel_button),
            TAG_WAITING_DIALOG
        )
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

    private fun onLogin() {
        viewBinding?.webStorageSwipe?.isRefreshing = false
        MainActivity.show(requireContext())
        requireActivity().finish()
    }

    private inner class StorageWebViewClient : WebViewClient() {

        override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
            viewBinding?.webStorageSwipe?.isRefreshing = true
        }

        override fun onPageFinished(view: WebView, url: String) {
            viewBinding?.webStorageSwipe?.isRefreshing = false
            if (url.startsWith(redirectUrl.toString())) {
                val parametersMap = StringUtils.getParametersFromUrl(url.split("?")[1])
                viewModel.signIn(parametersMap[TAG_CODE].orEmpty())
            }
        }

        override fun onReceivedError(
            view: WebView,
            request: WebResourceRequest,
            error: WebResourceError
        ) {
            super.onReceivedError(view, request, error)
            viewBinding?.webStorageSwipe?.isRefreshing = false
        }
    }
}