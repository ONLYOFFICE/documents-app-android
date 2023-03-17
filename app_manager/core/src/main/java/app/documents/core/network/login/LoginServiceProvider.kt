package app.documents.core.network.login

import app.documents.core.network.login.models.RequestDeviceToken
import app.documents.core.network.login.models.RequestPushSubscribe
import app.documents.core.network.login.models.request.*
import app.documents.core.network.login.models.response.ResponseSettings
import com.jakewharton.rxrelay2.BehaviorRelay
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import okhttp3.ResponseBody
import retrofit2.HttpException
import retrofit2.Response

class LoginServiceProvider(
    private val loginService: LoginService,
    private val loginErrorHandler: BehaviorRelay<LoginResponse.Error>? = null
) : ILoginServiceProvider {

    override fun serverVersion(): Single<LoginResponse.Success> {
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
            .mergeWith(loginService.getAllSettings().map { fetchResponse(it) })
            .toObservable()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    override fun validatePortal(request: RequestValidatePortal): Single<LoginResponse> {
        return loginService.validatePortal(request).map { fetchResponse(it) }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    override fun signIn(request: RequestSignIn, smsCode: String?): Single<LoginResponse> {
        smsCode?.let { sms ->
            return loginService.smsSignIn(request.copy(code = smsCode), sms).map { fetchResponse(it) }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
        } ?: run {
            return loginService.signIn(request).map { fetchResponse(it) }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
        }
    }

    override fun registerPortal(request: RequestRegister): Single<LoginResponse> {
        return loginService.registerPortal(
            recaptchaResponse = request.recaptchaResponse,
            portalName = request.portalName,
            firstName = request.firstName,
            lastName = request.lastName,
            email = request.email,
            password = request.password
        ).map { fetchResponse(it) }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    override fun registerPersonal(request: RequestRegister): Single<LoginResponse> {
        return loginService.registerPersonalPortal(request).map { fetchResponse(it) }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    override fun sendSms(request: RequestSignIn): Single<LoginResponse> {
        return loginService.sendSms(request).map { fetchResponse(it) }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    override fun changeNumber(request: RequestNumber): Single<LoginResponse> {
        return loginService.changeNumber(request).map { fetchResponse(it) }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    override fun getUserInfo(token: String): Single<LoginResponse> {
        return loginService.getUserInfo(token).map { fetchResponse(it) }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    override fun passwordRecovery(request: RequestPassword): Single<LoginResponse> {
        return loginService.forgotPassword(request).map { fetchResponse(it) }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    override fun setFirebaseToken(token: String, deviceToken: String): Single<Response<ResponseBody>> {
        return loginService.registerDevice(token,
            RequestDeviceToken(deviceToken)
        )
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    override fun subscribe(token: String, deviceToken: String, isSubscribe: Boolean): Single<Response<ResponseBody>> {
        return loginService.subscribe(token,
            RequestPushSubscribe(deviceToken, isSubscribe)
        )
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