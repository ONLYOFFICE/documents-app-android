package app.documents.core.login

import app.documents.core.model.login.AllSettings
import app.documents.core.model.login.Capabilities
import app.documents.core.model.login.Settings
import app.documents.core.model.login.Token
import app.documents.core.model.login.User
import app.documents.core.model.login.request.RequestSignIn
import app.documents.core.model.login.response.ResponseValidatePortal
import app.documents.core.network.common.Result
import kotlinx.coroutines.flow.Flow

interface LoginRepository {

    suspend fun getServerVersion(): Flow<String>

    suspend fun getCapabilities(): Flow<Capabilities>

    suspend fun getSettings(): Flow<Settings>

    suspend fun getAllSettings(): Flow<AllSettings>

    suspend fun validatePortal(portalName: String): Flow<ResponseValidatePortal>

    suspend fun signIn(request: RequestSignIn, smsCode: String? = null): Flow<Token>

    suspend fun getUserInfo(token: String): Flow<User>

    suspend fun setFirebaseToken(token: String, deviceToken: String): Flow<Result<Unit>>

    suspend fun subscribe(token: String, deviceToken: String, isSubscribe: Boolean): Flow<Result<Unit>>

    //    fun registerPortal(request: RequestRegister): Single<LoginResponse>
    //
    //    fun registerPersonal(request: RequestRegister): Single<LoginResponse>
    //
    //    fun sendSms(request: RequestSignIn): Single<LoginResponse>
    //
    //    fun changeNumber(request: RequestNumber): Single<LoginResponse>
    //
    //    fun passwordRecovery(request: RequestPassword): Single<LoginResponse>
}