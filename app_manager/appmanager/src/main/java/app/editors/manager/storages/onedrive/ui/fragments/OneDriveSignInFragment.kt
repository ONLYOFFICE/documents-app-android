package app.editors.manager.storages.onedrive.ui.fragments

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
import app.editors.manager.managers.utils.StorageUtils
import app.editors.manager.mvp.models.account.Storage
import app.editors.manager.storages.base.fragment.BaseStorageSignInFragment
import app.editors.manager.storages.base.view.BaseStorageSignInView
import app.editors.manager.storages.onedrive.mvp.presenters.OneDriveSingInPresenter
import app.editors.manager.storages.onedrive.mvp.views.OneDriveSignInView
import app.editors.manager.ui.activities.main.MainActivity
import app.editors.manager.ui.fragments.base.BaseAppFragment
import lib.toolkit.base.managers.utils.NetworkUtils.isOnline
import moxy.presenter.InjectPresenter

class OneDriveSignInFragment : BaseStorageSignInFragment(), OneDriveSignInView {

    companion object {
        val TAG = OneDriveSignInFragment::class.java.simpleName

        fun newInstance(storage: Storage) = OneDriveSignInFragment().apply {
            arguments = Bundle(1).apply {
                putParcelable(TAG_STORAGE, storage)
            }
        }
    }

    @InjectPresenter
    lateinit var presenter: OneDriveSingInPresenter

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
        super.onLogin()
        viewBinding?.webStorageSwipe?.isRefreshing = false
    }

    override fun getWebViewCallback() = WebViewCallbacks()
}