package app.documents.core.login

import app.documents.core.account.AccountRepository
import app.documents.core.model.cloud.CloudAccount
import app.documents.core.model.cloud.CloudPortal
import app.documents.core.model.cloud.PortalProvider
import app.documents.core.model.cloud.PortalSettings
import app.documents.core.model.login.GoogleUser
import app.documents.core.network.GOOGLE_DRIVE_URL
import app.documents.core.network.common.Result
import app.documents.core.network.common.asResult
import app.documents.core.network.login.GoogleLoginDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

interface GoogleLoginRepository {

    suspend fun signIn(code: String): Flow<Result<*>>

    suspend fun refreshToken(): Flow<Result<*>>
}

internal class GoogleLoginRepositoryImpl(
    private val googleLoginDataSource: GoogleLoginDataSource,
    private val accountRepository: AccountRepository
) : GoogleLoginRepository {

    override suspend fun signIn(code: String): Flow<Result<*>> {
        return flow {
            val response = googleLoginDataSource.signIn(code)
            val user = googleLoginDataSource.getUserInfo(response.accessToken)
            accountRepository.addAccount(createCloudAccount(user), response.accessToken)
            emit(null)
        }.flowOn(Dispatchers.IO)
            .asResult()
    }

    override suspend fun refreshToken(): Flow<Result<*>> {
        return flow {
            val response = googleLoginDataSource.refreshToken(accountRepository.getRefreshToken().orEmpty())
            accountRepository.updateAccount(response.accountId, response.accessToken, response.refreshToken)
            emit(null)
        }.flowOn(Dispatchers.IO)
            .asResult()
    }

    private fun createCloudAccount(user: GoogleUser): CloudAccount {
        return CloudAccount(
            id = user.permissionId,
            portalUrl = GOOGLE_DRIVE_URL,
            avatarUrl = user.photoLink,
            login = user.emailAddress,
            name = user.displayName,
            portal = CloudPortal(
                url = GOOGLE_DRIVE_URL,
                provider = PortalProvider.GoogleDrive,
                settings = PortalSettings(
                    isSslState = true,
                    isSslCiphers = false
                )
            )
        )
    }
}