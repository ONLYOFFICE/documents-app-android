package app.documents.core.login

import app.documents.core.storage.account.CloudAccount
import kotlinx.coroutines.flow.Flow

sealed class LoginResult {

    data class Success(val cloudAccount: CloudAccount) : LoginResult()
    data class Tfa(val key: String) : LoginResult()
    data class Sms(val phoneNoise: String) : LoginResult()
    data class Error(val exception: Throwable) : LoginResult()
}

interface LoginRepository {

    fun signInByEmail(email: String, password: String): Flow<LoginResult>

    fun signInWithProvider(accessToken: String, provider: String): Flow<LoginResult>
}