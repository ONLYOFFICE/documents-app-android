package app.documents.core.network.login

import app.documents.core.model.login.response.TokenResponse

interface StorageLoginDataSource<U> {

    suspend fun signIn(code: String): TokenResponse

    suspend fun refreshToken(refreshToken: String): TokenResponse

    suspend fun getUserInfo(accessToken: String): U
}