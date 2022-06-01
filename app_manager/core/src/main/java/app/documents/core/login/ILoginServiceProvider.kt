package app.documents.core.login

import app.documents.core.network.models.login.request.*
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

    fun validatePortal(request: RequestValidatePortal): Single<LoginResponse>

    fun signIn(request: RequestSignIn, smsCode: String? = null): Single<LoginResponse>

    fun registerPortal(request: RequestRegister): Single<LoginResponse>

    fun registerPersonal(request: RequestRegister): Single<LoginResponse>

    fun sendSms(request: RequestSignIn): Single<LoginResponse>

    fun changeNumber(request: RequestNumber): Single<LoginResponse>

    fun getUserInfo(token: String): Single<LoginResponse>

    fun passwordRecovery(request: RequestPassword): Single<LoginResponse>

    fun setFirebaseToken(token: String, deviceToken: String): Single<Response<ResponseBody>>

    fun subscribe(token: String, deviceToken: String, isSubscribe: Boolean): Single<Response<ResponseBody>>
}