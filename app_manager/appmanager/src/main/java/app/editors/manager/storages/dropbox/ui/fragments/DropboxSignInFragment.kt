package app.editors.manager.storages.dropbox.ui.fragments

import android.graphics.Bitmap
import android.os.Bundle
import android.webkit.*
import app.editors.manager.R
import app.editors.manager.storages.dropbox.mvp.presenters.DropboxSignInPresenter
import app.editors.manager.mvp.models.account.Storage
import app.editors.manager.storages.base.fragment.BaseStorageSignInFragment
import app.editors.manager.storages.base.view.BaseStorageSignInView
import app.editors.manager.storages.onedrive.ui.fragments.OneDriveSignInFragment
import lib.toolkit.base.managers.utils.NetworkUtils
import moxy.presenter.InjectPresenter

class DropboxSignInFragment: BaseStorageSignInFragment(), BaseStorageSignInView {


    @InjectPresenter
    lateinit var presenter: DropboxSignInPresenter

    companion object {

        val TAG = DropboxSignInFragment::class.java.simpleName

        fun newInstance(storage: Storage) = DropboxSignInFragment().apply {
            arguments = Bundle(1).apply {
                putParcelable(TAG_STORAGE, storage)
            }
        }
    }

    inner class WebViewCallbacks : WebViewClient() {
        override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
            super.onPageStarted(view, url, favicon)
            if (url.startsWith(redirectUrl!!)) {
                val accessToken = url.split("=")
                val token = accessToken[1]
                presenter.getUserInfo(token.dropLast(11), accessToken[5])
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
            if (!NetworkUtils.isOnline(requireContext())) {
                showSnackBar(R.string.errors_connection_error)
            }
        }
    }

    override fun getWebViewCallback() = WebViewCallbacks()
}