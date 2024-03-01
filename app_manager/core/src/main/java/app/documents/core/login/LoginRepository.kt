package app.documents.core.login

import app.documents.core.model.login.response.ResponseRegisterPortal
import app.documents.core.network.common.Result
import app.documents.core.storage.account.CloudAccount
import kotlinx.coroutines.flow.Flow

sealed class LoginResult {

    data class Success(val cloudAccount: CloudAccount) : LoginResult()
    data class Tfa(val key: String) : LoginResult()
    data class Sms(val phoneNoise: String) : LoginResult()
    data class Error(val exception: Throwable) : LoginResult()
}

interface LoginRepository {

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
}