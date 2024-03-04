package app.documents.core.login

import app.documents.core.model.cloud.CloudAccount
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

interface LoginRepository {

    fun getAccountData(accountName: String): AccountData

    fun setAccountData(accountName: String, updateAccountData: (AccountData) -> AccountData)

    suspend fun signInByEmail(email: String, password: String, code: String?): Flow<LoginResult>

    suspend fun signInWithProvider(accessToken: String?, provider: String): Flow<LoginResult>

    suspend fun signInWithSSO(accessToken: String): Flow<Result<CloudAccount>>

    suspend fun signInWithToken(accessToken: String): Flow<Result<*>>

    suspend fun registerPortal(
        portalName: String,
        email: String,
        firstName: String,
        lastName: String,
        password: String,
        recaptchaResponse: String
    ): Flow<ResponseRegisterPortal>

    suspend fun switchAccount(account: CloudAccount): Flow<Result<*>>

    suspend fun unsubscribePush(account: CloudAccount)
}