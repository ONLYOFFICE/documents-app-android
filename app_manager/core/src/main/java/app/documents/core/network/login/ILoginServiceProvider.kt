package app.documents.core.network.login

import io.reactivex.Observable
import io.reactivex.Single
import okhttp3.ResponseBody
import retrofit2.Response


sealed class LoginResponse {
    class Success(val response: Any) : LoginResponse()
    class Error(val error: Throwable) : LoginResponse()
}

interface ILoginServiceProvider {

    fun serverVersion(): Single<LoginResponse.Success>

    fun capabilities(): Observable<LoginResponse>

    fun validatePortal(request: app.documents.core.network.login.models.request.RequestValidatePortal): Single<LoginResponse>

    fun signIn(request: app.documents.core.network.login.models.request.RequestSignIn, smsCode: String? = null): Single<LoginResponse>

    fun registerPortal(request: app.documents.core.network.login.models.request.RequestRegister): Single<LoginResponse>

    fun registerPersonal(request: app.documents.core.network.login.models.request.RequestRegister): Single<LoginResponse>

    fun sendSms(request: app.documents.core.network.login.models.request.RequestSignIn): Single<LoginResponse>

    fun changeNumber(request: app.documents.core.network.login.models.request.RequestNumber): Single<LoginResponse>

    fun getUserInfo(token: String): Single<LoginResponse>

    fun passwordRecovery(request: app.documents.core.network.login.models.request.RequestPassword): Single<LoginResponse>

    fun setFirebaseToken(token: String, deviceToken: String): Single<Response<ResponseBody>>

    fun subscribe(token: String, deviceToken: String, isSubscribe: Boolean): Single<Response<ResponseBody>>
}