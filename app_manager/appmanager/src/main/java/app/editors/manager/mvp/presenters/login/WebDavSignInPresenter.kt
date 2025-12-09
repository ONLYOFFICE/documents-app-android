package app.editors.manager.mvp.presenters.login

import app.documents.core.login.WebdavLoginResult
import app.documents.core.model.cloud.Scheme
import app.documents.core.model.cloud.WebdavProvider
import app.editors.manager.app.App
import app.editors.manager.mvp.presenters.base.BasePresenter
import app.editors.manager.mvp.views.login.WebDavSignInView
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import lib.toolkit.base.managers.utils.StringUtils
import moxy.InjectViewState
import moxy.presenterScope
import java.net.MalformedURLException

@InjectViewState
class WebDavSignInPresenter : BasePresenter<WebDavSignInView>() {

    private var signInJob: Job? = null

    init {
        App.getApp().appComponent.inject(this)
    }

    override fun cancelRequest() {
        super.cancelRequest()
        signInJob?.cancel()
    }

    fun checkPortal(provider: WebdavProvider, url: String, login: String, password: String) {
        signInJob = presenterScope.launch {
            viewState.onDialogWaiting()
            App.getApp().loginComponent.webdavLoginRepository
                .signIn(
                    provider = provider,
                    url = url.takeIf(StringUtils::hasScheme) ?: (Scheme.Https.value + url),
                    login = login,
                    password = password
                )
                .collect { result ->
                    when (result) {
                        WebdavLoginResult.Success -> {
                            viewState.onLogin()
                        }
                        is WebdavLoginResult.NextCloudLogin -> {
                            viewState.onNextCloudLogin(result.url)
                        }
                        is WebdavLoginResult.OwnCloudLogin -> {
                            viewState.onOwnCloudLogin(url, result.config)
                        }
                        is WebdavLoginResult.Error -> {
                            viewState.onDialogClose()
                            if (result.exception is MalformedURLException) {
                                viewState.onUrlError()
                            } else {
                                fetchError(result.exception)
                            }
                        }
                    }
                }
        }
    }
}