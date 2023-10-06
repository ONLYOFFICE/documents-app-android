package app.editors.manager.ui.fragments.storages

import android.graphics.Bitmap
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import app.documents.core.network.common.utils.DropboxUtils
import app.editors.manager.mvp.presenters.storages.DropboxSignInPresenter
import app.editors.manager.ui.fragments.base.BaseStorageSignInFragment
import lib.toolkit.base.managers.utils.StringUtils
import lib.toolkit.base.managers.utils.putArgs
import moxy.presenter.InjectPresenter

class DropboxSignInFragment : BaseStorageSignInFragment() {

    @InjectPresenter
    lateinit var presenter: DropboxSignInPresenter

    companion object {

        val TAG = OneDriveSignInFragment::class.java.simpleName

        fun newInstance(): DropboxSignInFragment = DropboxSignInFragment()
            .putArgs(TAG_STORAGE to DropboxUtils.storage)
    }

    private inner class WebViewCallback : WebViewClient() {
        override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
            viewBinding?.webStorageSwipe?.isRefreshing = true
        }

        override fun onPageFinished(view: WebView, url: String) {
            viewBinding?.webStorageSwipe?.isRefreshing = false
            if (url.startsWith(redirectUrl.toString())) {
                val parametersMap = StringUtils.getParametersFromUrl(url.split("?")[1])
                presenter.authUser(parametersMap[TAG_CODE].orEmpty())
                viewBinding?.webStorageSwipe?.isRefreshing = true
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

    override fun getWebViewCallback(): WebViewClient = WebViewCallback()

}