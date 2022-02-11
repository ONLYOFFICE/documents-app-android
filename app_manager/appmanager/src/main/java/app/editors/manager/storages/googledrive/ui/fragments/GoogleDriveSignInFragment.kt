package app.editors.manager.storages.googledrive.ui.fragments

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.*
import androidx.core.content.ContextCompat
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import app.editors.manager.R
import app.editors.manager.databinding.FragmentStorageWebBinding
import app.editors.manager.storages.googledrive.mvp.presenters.GoogleDriveSignInPresenter
import app.editors.manager.storages.googledrive.mvp.views.GoogleDriveSignInView
import app.editors.manager.managers.utils.StorageUtils
import app.editors.manager.mvp.models.account.Storage
import app.editors.manager.storages.base.fragment.BaseStorageSignInFragment
import app.editors.manager.storages.base.view.BaseStorageSignInView
import app.editors.manager.ui.activities.main.MainActivity
import app.editors.manager.ui.fragments.base.BaseAppFragment
import lib.toolkit.base.managers.utils.NetworkUtils
import lib.toolkit.base.managers.utils.StringUtils
import moxy.presenter.InjectPresenter

class GoogleDriveSignInFragment: BaseStorageSignInFragment(){

    companion object {
        val TAG = GoogleDriveSignInFragment::class.java.simpleName

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
            if (url.startsWith(redirectUrl!!)) {
                val parametersMap = StringUtils.getParametersFromUrl(url.split("#")[1])
                parametersMap[TAG_ACCESS_TOKEN]?.let { presenter.getUserInfo(it) }
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