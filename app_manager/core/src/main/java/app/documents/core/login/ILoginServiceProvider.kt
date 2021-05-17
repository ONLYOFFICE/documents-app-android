package app.documents.core.login

import app.documents.core.network.models.login.request.RequestNumber
import app.documents.core.network.models.login.request.RequestRegister
import app.documents.core.network.models.login.request.RequestSignIn
import app.documents.core.network.models.login.request.RequestValidatePortal
import io.reactivex.Observable


sealed class LoginResponse {
    class Success(val response: Any) : LoginResponse()
    class Error(val error: Throwable) : LoginResponse()
}

interface ILoginServiceProvider {

    fun serverVersion(): Observable<LoginResponse.Success>

    fun capabilities(): Observable<LoginResponse>

    fun validatePortal(request: RequestValidatePortal): Observable<LoginResponse>

    fun signIn(request: RequestSignIn, smsCode: String? = null): Observable<LoginResponse>

    fun registerPortal(request: RequestRegister): Observable<LoginResponse>

    fun registerPersonal(request: RequestRegister): Observable<LoginResponse>

    fun sendSms(request: RequestSignIn): Observable<LoginResponse>

    fun changeNumber(request: RequestNumber): Observable<LoginResponse>

    fun getUserInfo(token: String): Observable<LoginResponse>

}