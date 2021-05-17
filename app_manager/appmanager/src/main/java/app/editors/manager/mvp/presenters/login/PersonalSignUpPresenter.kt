package app.editors.manager.mvp.presenters.login

import app.documents.core.login.LoginResponse
import app.documents.core.network.ApiContract
import app.documents.core.network.models.login.request.RequestRegister
import app.documents.core.network.models.login.response.ResponseRegisterPersonalPortal
import app.editors.manager.R
import app.editors.manager.app.App
import app.editors.manager.mvp.views.login.PersonalRegisterView
import io.reactivex.disposables.Disposable
import moxy.InjectViewState
import java.util.*

@InjectViewState
class PersonalSignUpPresenter : BaseLoginPresenter<PersonalRegisterView>() {

    companion object {
        val TAG: String = PersonalSignUpPresenter::class.java.simpleName
        private const val EMAIL_CODE = "201"
    }

    init {
        App.getApp().appComponent.inject(this)
    }

    private var disposable: Disposable? = null

    override fun cancelRequest() {
        disposable?.dispose()
    }

    override fun onDestroy() {
        super.onDestroy()
        disposable?.dispose()
    }

    fun registerPortal(email: String?) {
        networkSettings.setBaseUrl(ApiContract.PERSONAL_HOST)

        email?.let {
            disposable = App.getApp().loginComponent.loginService
                .registerPersonal(RequestRegister(email = email, language = Locale.getDefault().language))
                .subscribe({ loginResponse ->
                    when (loginResponse) {
                        is LoginResponse.Success -> {
                            checkResponse(loginResponse.response as ResponseRegisterPersonalPortal)
                        }
                        is LoginResponse.Error -> {
                            fetchError(loginResponse.error)
                        }
                    }
                }, { error ->
                    fetchError(error)
                })
        }
    }

    private fun checkResponse(response: ResponseRegisterPersonalPortal) {
        if (!response.response.isNullOrEmpty() && response.status != EMAIL_CODE) {
            viewState.onError(context.getString(R.string.errors_email_already_registered))
        } else if (!response.response.isNullOrEmpty()) {
            viewState.onError(response.response)
        } else {
            viewState.onRegisterPortal()
        }
    }

}