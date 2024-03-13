package app.editors.manager.mvp.presenters.storages

import app.documents.core.network.common.Result
import app.editors.manager.app.App
import app.editors.manager.mvp.presenters.base.BasePresenter
import app.editors.manager.mvp.views.base.BaseStorageSignInView
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import moxy.presenterScope

class GoogleDriveSignInPresenter : BasePresenter<BaseStorageSignInView>() {

    private var signInJob: Job? = null

    fun signIn(code: String) {
        signInJob = presenterScope.launch {
            App.getApp().refreshLoginComponent(null)
            App.getApp().loginComponent
                .googleLoginRepository
                .signIn(code)
                .collect { result ->
                    when (result) {
                        is Result.Success -> viewState.onLogin()
                        is Result.Error -> fetchError(result.exception)
                    }
                }
        }
    }
}