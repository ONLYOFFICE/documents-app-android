package app.documents.core.login

import app.documents.core.account.AccountRepository
import app.documents.core.model.cloud.CloudAccount
import app.documents.core.model.cloud.CloudPortal
import app.documents.core.model.cloud.PortalProvider
import app.documents.core.model.cloud.PortalSettings
import app.documents.core.model.login.response.OnedriveUser
import app.documents.core.network.ONEDRIVE_PORTAL_URL
import app.documents.core.network.common.Result
import app.documents.core.network.common.asResult
import app.documents.core.network.login.OnedriveLoginDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

interface OnedriveLoginRepository : StorageLoginRepository

internal class OnedriveLoginRepositoryImpl(
    private val onedriveLoginDataSource: OnedriveLoginDataSource,
    private val accountRepository: AccountRepository
) : OnedriveLoginRepository {

    override suspend fun signIn(code: String): Flow<Result<*>> {
        return flow {
            val response = onedriveLoginDataSource.signIn(code)
            val user = onedriveLoginDataSource.getUserInfo(response.accessToken)
            accountRepository.addAccount(createCloudAccount(user), response.accessToken, response.refreshToken)
            emit(null)
        }.flowOn(Dispatchers.IO)
            .asResult()
    }

    override suspend fun refreshToken(): Flow<Result<*>> {
        return flow {
            val response = onedriveLoginDataSource.refreshToken(accountRepository.getRefreshToken().orEmpty())
            accountRepository.updateAccount(token = response.accessToken)
            emit(null)
        }.flowOn(Dispatchers.IO)
            .asResult()
    }

    private fun createCloudAccount(user: OnedriveUser): CloudAccount {
        return CloudAccount(
            id = requireNotNull(user.id),
            portalUrl = ONEDRIVE_PORTAL_URL,
            login = user.mail.orEmpty(),
            name = user.displayName,
            portal = CloudPortal(
                url = ONEDRIVE_PORTAL_URL,
                provider = PortalProvider.Onedrive,
                settings = PortalSettings(
                    isSslState = true,
                    isSslCiphers = false
                )
            )
        )
    }
}