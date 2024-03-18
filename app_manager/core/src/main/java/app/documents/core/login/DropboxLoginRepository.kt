package app.documents.core.login

import app.documents.core.account.AccountRepository
import app.documents.core.model.cloud.CloudAccount
import app.documents.core.model.cloud.CloudPortal
import app.documents.core.model.cloud.PortalProvider
import app.documents.core.model.cloud.PortalSettings
import app.documents.core.model.login.response.DropboxUserResponse
import app.documents.core.network.DROPBOX_PORTAL_URL
import app.documents.core.network.common.Result
import app.documents.core.network.common.asResult
import app.documents.core.network.login.DropboxLoginDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

interface DropboxLoginRepository : StorageLoginRepository

internal class DropboxLoginRepositoryImpl(
    private val dropboxLoginDataSource: DropboxLoginDataSource,
    private val accountRepository: AccountRepository
) : DropboxLoginRepository {

    override suspend fun signIn(code: String): Flow<Result<*>> {
        return flow {
            val response = dropboxLoginDataSource.getAccessToken(code)
            val user = dropboxLoginDataSource.getUserInfo(response.accessToken)
            accountRepository.addAccount(createCloudAccount(user), response.accessToken, response.refreshToken)
            emit(null)
        }.flowOn(Dispatchers.IO)
            .asResult()
    }

    override suspend fun refreshToken(): Flow<Result<*>> {
        return flow {
            val response = dropboxLoginDataSource.refreshToken(accountRepository.getRefreshToken().orEmpty())
            accountRepository.updateAccount(token = response.accessToken)
            emit(null)
        }.flowOn(Dispatchers.IO)
            .asResult()
    }

    private fun createCloudAccount(user: DropboxUserResponse): CloudAccount {
        return CloudAccount(
            id = requireNotNull(user.accountId),
            portalUrl = DROPBOX_PORTAL_URL,
            avatarUrl = user.profilePhotoUrl.orEmpty(),
            login = user.email.orEmpty(),
            name = user.name?.displayName.orEmpty(),
            portal = CloudPortal(
                url = DROPBOX_PORTAL_URL,
                provider = PortalProvider.Dropbox,
                settings = PortalSettings(
                    isSslState = true,
                    isSslCiphers = false
                )
            )
        )
    }
}