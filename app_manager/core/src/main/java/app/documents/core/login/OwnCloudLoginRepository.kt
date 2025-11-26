package app.documents.core.login

import app.documents.core.account.AccountRepository
import app.documents.core.model.login.OidcConfiguration
import app.documents.core.model.login.response.TokenResponse
import app.documents.core.network.common.NetworkResult
import app.documents.core.network.login.OwnCloudLoginDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.net.URL

interface OwnCloudLoginRepository {
    suspend fun signIn(code: String, configuration: OidcConfiguration): Flow<NetworkResult<Unit>>
    suspend fun refreshToken(url: String, issuer: String): Flow<NetworkResult<TokenResponse>>
}

internal class OwnCloudLoginRepositoryImpl(
    private val accountRepository: AccountRepository,
    private val ownCloudLoginDataSource: OwnCloudLoginDataSource
) : OwnCloudLoginRepository {

    override suspend fun signIn(
        code: String,
        configuration: OidcConfiguration
    ): Flow<NetworkResult<Unit>> {
        return flow<NetworkResult<Unit>> {
            val response = ownCloudLoginDataSource.signIn(
                url = configuration.tokenEndpoint,
                issuer = configuration.issuer,
                code = code
            )
            val userInfo = ownCloudLoginDataSource.getUserInfo(
                url = configuration.userInfoEndpoint.orEmpty(),
                accessToken = response.accessToken
            )
            accountRepository.addAccount(
                cloudAccount = userInfo.toCloudAccount(URL(configuration.issuer)),
                accessToken = response.accessToken,
                refreshToken = response.refreshToken
            )
            emit(NetworkResult.Success(Unit))
        }
            .flowOn(Dispatchers.IO)
            .catch { cause -> emit(NetworkResult.Error(cause)) }
    }

    override suspend fun refreshToken(
        url: String,
        issuer: String
    ): Flow<NetworkResult<TokenResponse>> {
        TODO("Not yet implemented")
    }
}