package app.documents.core.login

import app.documents.core.model.login.response.AccessTokenResponse
import app.documents.core.network.common.NetworkResult
import app.documents.core.network.common.asResult
import app.documents.core.network.login.TwitterLoginDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

interface TwitterLoginRepository {
    suspend fun getRequestToken(
        consumerKey: String,
        consumerSecret: String,
        callbackUrl: String
    ): Flow<NetworkResult<String>>

    suspend fun getAccessToken(
        oauthToken: String,
        oauthVerifier: String
    ): Flow<NetworkResult<AccessTokenResponse>>
}

internal class TwitterLoginRepositoryImpl(
    private val twitterLoginDataSource: TwitterLoginDataSource
) : TwitterLoginRepository {

    override suspend fun getRequestToken(
        consumerKey: String,
        consumerSecret: String,
        callbackUrl: String
    ): Flow<NetworkResult<String>> = flow {
        val response = twitterLoginDataSource.getRequestToken(
            consumerKey = consumerKey,
            consumerSecret = consumerSecret,
            callbackUrl = callbackUrl
        )
        if (!response.callbackConfirmed) throw IllegalStateException()
        emit(response.oauthToken)
    }
        .flowOn(Dispatchers.IO)
        .asResult()

    override suspend fun getAccessToken(
        oauthToken: String,
        oauthVerifier: String
    ): Flow<NetworkResult<AccessTokenResponse>> = flow {
        val response = twitterLoginDataSource.getAccessToken(oauthToken, oauthVerifier)
        emit(response)
    }
        .flowOn(Dispatchers.IO)
        .asResult()
}