package app.editors.manager.viewModels.base

import android.accounts.Account
import android.os.Build
import android.webkit.URLUtil
import app.documents.core.network.common.contracts.ApiContract
import app.documents.core.storage.preference.NetworkSettings
import io.reactivex.disposables.Disposable
import kotlinx.coroutines.Job
import lib.toolkit.base.managers.utils.StringUtils
import java.net.ConnectException
import java.net.SocketTimeoutException
import javax.inject.Inject
import javax.net.ssl.SSLHandshakeException
import javax.net.ssl.SSLPeerUnverifiedException

abstract class BaseLoginViewModel: BaseViewModel() {

    @Inject
    protected lateinit var networkSettings: NetworkSettings

    protected var account: Account? = null
    private var disposable: Disposable? = null
    private var goggleJob: Job? = null


    protected open fun isConfigConnection(t: Throwable?): Boolean {
        if (t is SSLHandshakeException && !networkSettings.getCipher() && networkSettings.getScheme() == ApiContract.SCHEME_HTTPS && Build.VERSION.SDK_INT == Build.VERSION_CODES.N) {
            networkSettings.setCipher(true)
            return true
        } else if ((t is ConnectException || t is SocketTimeoutException || t is SSLHandshakeException ||
                    t is SSLPeerUnverifiedException) && networkSettings.getScheme() == ApiContract.SCHEME_HTTPS
        ) {
            networkSettings.setCipher(false)
            networkSettings.setScheme(ApiContract.SCHEME_HTTP)
            return true
        }
        return false
    }

    protected fun getPortal(url: String): String? {
        if (StringUtils.isValidUrl(url)) {
            networkSettings.setScheme(if (URLUtil.isHttpsUrl(url)) ApiContract.SCHEME_HTTPS else ApiContract.SCHEME_HTTP)
            return StringUtils.getUrlWithoutScheme(url)
        } else {
            val concatUrl = networkSettings.getScheme() + url
            if (StringUtils.isValidUrl(concatUrl)) {
                return url
            }
        }
        return null
    }
}