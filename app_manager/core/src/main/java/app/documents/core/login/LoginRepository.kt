package app.documents.core.login

import app.documents.core.model.cloud.CloudAccount
import app.documents.core.model.cloud.Scheme
import app.documents.core.model.login.request.RequestNumber
import app.documents.core.model.login.response.ResponseRegisterPortal
import app.documents.core.network.common.Result
import kotlinx.coroutines.flow.Flow
import lib.toolkit.base.managers.utils.AccountData

sealed class LoginResult {

    data class Success(val cloudAccount: CloudAccount) : LoginResult()
    data class Tfa(val key: String) : LoginResult()
    data class Sms(val phoneNoise: String) : LoginResult()
    data class Error(val exception: Throwable) : LoginResult()
}

sealed class PortalResult {

    data class Success(val providers: List<String>, val isHttp: Boolean) : PortalResult()
    data class Error(val exception: Throwable) : PortalResult()
    data object ShouldUseHttp : PortalResult()
}

sealed class CheckLoginResult {

    data class Error(val exception: Throwable) : CheckLoginResult()
    data object NeedLogin : CheckLoginResult()
    data object AlreadyUse : CheckLoginResult()
    data object Success : CheckLoginResult()
}

interface LoginRepository {

    fun getAccountData(accountName: String): AccountData

    fun setAccountData(accountName: String, updateAccountData: (AccountData) -> AccountData)

    suspend fun signInByEmail(email: String, password: String, code: String?): Flow<LoginResult>

    suspend fun signInWithProvider(accessToken: String?, provider: String): Flow<LoginResult>

    suspend fun signInWithSSO(accessToken: String): Flow<Result<CloudAccount>>

    suspend fun registerPortal(
        portalName: String,
        email: String,
        firstName: String,
        lastName: String,
        password: String,
        recaptchaResponse: String
    ): Flow<ResponseRegisterPortal>

    suspend fun registerPersonal(email: String, language: String): Flow<Result<*>>

    suspend fun unsubscribePush(account: CloudAccount)

    suspend fun checkPortal(portal: String, scheme: Scheme): Flow<PortalResult>

    suspend fun logOut(cloudAccount: CloudAccount? = null): Flow<Result<*>>

    suspend fun logOut(accountId: String): Flow<Result<*>>

    suspend fun deleteAccounts(vararg accountIds: String): Flow<Result<List<CloudAccount>>>

    suspend fun checkLogin(account: CloudAccount): Flow<CheckLoginResult>

    suspend fun checkLogin(accountId: String): Flow<CheckLoginResult>

    suspend fun sendSms(userName: String, password: String, accessToken: String, provider: String): Flow<Result<*>>

    suspend fun passwordRecovery(portal: String, email: String): Flow<Result<*>>

    fun getSavedPortals(): Flow<List<String>>

    suspend fun changeNumber(requestNumber: RequestNumber): Flow<Result<*>>

    suspend fun validatePortal(portalName: String): Flow<Result<*>>

    suspend fun registerDevice(portalUrl: String, token: String, deviceToken: String)
}