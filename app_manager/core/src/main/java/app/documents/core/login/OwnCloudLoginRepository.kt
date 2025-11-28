package app.documents.core.login

import app.documents.core.account.AccountRepository
import app.documents.core.model.login.OidcConfiguration
import app.documents.core.network.common.NetworkResult
import app.documents.core.network.login.OwnCloudLoginDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.net.URL

interface OwnCloudLoginRepository {
    fun signIn(code: String, configuration: OidcConfiguration): Flow<NetworkResult<Unit>>
    fun refreshToken(configuration: OidcConfiguration): Flow<NetworkResult<Unit>>
    fun getOpenidConfig(issuer: String): Flow<NetworkResult<OidcConfiguration>>
}

internal class OwnCloudLoginRepositoryImpl(
    private val accountRepository: AccountRepository,
    private val ownCloudLoginDataSource: OwnCloudLoginDataSource
) : OwnCloudLoginRepository {

    override fun signIn(
        code: String,
        configuration: OidcConfiguration
    ): Flow<NetworkResult<Unit>> {
        return apiCall {
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
        }
    }

    override fun refreshToken(configuration: OidcConfiguration): Flow<NetworkResult<Unit>> {
        return apiCall {
            val response = ownCloudLoginDataSource.refreshToken(
                url = configuration.tokenEndpoint,
                issuer = configuration.issuer,
                refreshToken = requireNotNull(accountRepository.getRefreshToken())
            )
            accountRepository.updateAccount(
                token = response.accessToken,
                refreshToken = response.refreshToken
            )
        }
    }

    override fun getOpenidConfig(issuer: String): Flow<NetworkResult<OidcConfiguration>> {
        return apiCall {
            requireNotNull(ownCloudLoginDataSource.openidConfiguration(issuer))
        }
    }

    private fun <T> apiCall(block: suspend () -> T): Flow<NetworkResult<T>> {
        return flow<NetworkResult<T>> {
            val data = block()
            emit(NetworkResult.Success(data))
        }
            .flowOn(Dispatchers.IO)
            .catch { cause -> emit(NetworkResult.Error(cause)) }
    }
}