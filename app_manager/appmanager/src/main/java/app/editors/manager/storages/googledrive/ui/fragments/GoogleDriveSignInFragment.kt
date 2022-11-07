package app.editors.manager.storages.googledrive.ui.fragments

import android.graphics.Bitmap
import android.os.Bundle
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import app.editors.manager.R
import app.editors.manager.mvp.models.account.Storage
import app.editors.manager.storages.base.fragment.BaseStorageSignInFragment
import app.editors.manager.storages.googledrive.mvp.presenters.GoogleDriveSignInPresenter
import lib.toolkit.base.managers.utils.NetworkUtils
import lib.toolkit.base.managers.utils.StringUtils
import moxy.presenter.InjectPresenter

class GoogleDriveSignInFragment: BaseStorageSignInFragment(){

    companion object {
        val TAG: String = GoogleDriveSignInFragment::class.java.simpleName

        fun newInstance(storage: Storage) = GoogleDriveSignInFragment().apply {
            arguments = Bundle(1).apply {
                putParcelable(TAG_STORAGE, storage)
            }
        }
    }

    @InjectPresenter
    lateinit var presenter: GoogleDriveSignInPresenter

    inner class WebViewCallbacks : WebViewClient() {
        override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
            super.onPageStarted(view, url, favicon)
            if (url.startsWith(redirectUrl.toString())) {
                val parametersMap = StringUtils.getParametersFromUrl(url.split("?")[1])
                parametersMap[TAG_CODE]?.let { presenter.getUserInfo(it) }
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
            if (context == null) {
                return
            }
            super.onReceivedError(view, request, error)
            viewBinding?.webStorageSwipe?.isRefreshing = false
            if (!NetworkUtils.isOnline(requireContext())) {
                showSnackBar(R.string.errors_connection_error)
            }
        }
    }

    override fun loadWebView(url: String?) {
        viewBinding?.webStorageSwipe?.isRefreshing = true
        url?.let { viewBinding?.webStorageWebview?.loadUrl(it) }
    }

    override fun getWebViewCallback() = WebViewCallbacks()

}