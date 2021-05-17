package app.documents.core.login

import app.documents.core.network.models.login.request.RequestNumber
import app.documents.core.network.models.login.request.RequestRegister
import app.documents.core.network.models.login.request.RequestSignIn
import app.documents.core.network.models.login.request.RequestValidatePortal
import app.documents.core.network.models.login.response.ResponseSettings
import com.jakewharton.rxrelay2.BehaviorRelay
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import retrofit2.HttpException
import retrofit2.Response

class LoginServiceProvider(
    private val loginService: LoginService,
    private val loginErrorHandler: BehaviorRelay<LoginResponse.Error>? = null
) : ILoginServiceProvider {

    override fun serverVersion(): Observable<LoginResponse.Success> {
        return loginService.getSettings()
            .map { fetchResponse(it) }
            .map { loginResponse ->
                if (loginResponse is LoginResponse.Success) {
                    return@map LoginResponse.Success(
                        (loginResponse.response as ResponseSettings).response.communityServer ?: ""
                    )
                } else {
                    return@map LoginResponse.Success("")
                }
            }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    override fun capabilities(): Observable<LoginResponse> {
        return loginService.capabilities().map { fetchResponse(it) }
            .mergeWith(loginService.getSettings().map { fetchResponse(it) })
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    override fun validatePortal(request: RequestValidatePortal): Observable<LoginResponse> {
        return loginService.validatePortal(request).map { fetchResponse(it) }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    override fun signIn(request: RequestSignIn, smsCode: String?): Observable<LoginResponse> {
        smsCode?.let { sms ->
            return loginService.smsSignIn(request, sms).map { fetchResponse(it) }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
        } ?: run {
            return loginService.signIn(request).map { fetchResponse(it) }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
        }
    }

    override fun registerPortal(request: RequestRegister): Observable<LoginResponse> {
        return loginService.registerPortal(request).map { fetchResponse(it) }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    override fun registerPersonal(request: RequestRegister): Observable<LoginResponse> {
        return loginService.registerPersonalPortal(request).map { fetchResponse(it) }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    override fun sendSms(request: RequestSignIn): Observable<LoginResponse> {
        return loginService.sendSms(request).map { fetchResponse(it) }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    override fun changeNumber(request: RequestNumber): Observable<LoginResponse> {
        return loginService.changeNumber(request).map { fetchResponse(it) }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    override fun getUserInfo(token: String): Observable<LoginResponse> {
        return loginService.getUserInfo(token).map { fetchResponse(it) }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    private fun <T> fetchResponse(response: Response<T>): LoginResponse {
        return if (response.isSuccessful && response.body() != null) {
            LoginResponse.Success(response.body()!!)
        } else {
            val error = LoginResponse.Error(HttpException(response))
            loginErrorHandler?.accept(error)
            return error
        }
    }
}