package app.documents.core.login

import app.documents.core.account.AccountManager
import app.documents.core.account.AccountPreferences
import app.documents.core.database.datasource.CloudDataSource
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
import lib.toolkit.base.managers.utils.AccountData

interface GoogleLoginRepository {

    suspend fun signIn(code: String): Flow<Result<*>>
    suspend fun refreshToken(): Flow<Result<*>>
}

internal class GoogleLoginRepositoryImpl(
    private val googleLoginDataSource: GoogleLoginDataSource,
    private val cloudDataSource: CloudDataSource,
    private val accountManager: AccountManager,
    private val accountPreferences: AccountPreferences
) : GoogleLoginRepository {

    override suspend fun signIn(code: String): Flow<Result<*>> {
        return flow {
            val response = googleLoginDataSource.signIn(code)
            val user = googleLoginDataSource.getUserInfo(response.accessToken)
            val account = createCloudAccount(user)

            addAccountToAccountManager(account, response.accessToken, response.refreshToken)
            cloudDataSource.insertOrUpdateAccount(account)
            cloudDataSource.insertOrUpdatePortal(account.portal)
            accountPreferences.onlineAccountId = account.id
            emit(Result.Success(null))
        }.flowOn(Dispatchers.IO)
    }

    override suspend fun refreshToken(): Flow<Result<*>> {
        return flow<Result<*>> {
            val onlineAccount = cloudDataSource.getAccount(accountPreferences.onlineAccountId.orEmpty())
            val refreshToken = accountManager.getAccountData(onlineAccount?.accountName.orEmpty()).refreshToken
            googleLoginDataSource.refreshToken(refreshToken.orEmpty())
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

    private fun addAccountToAccountManager(
        cloudAccount: CloudAccount,
        accessToken: String,
        refreshToken: String
    ) {
        val accountName = cloudAccount.accountName
        val accountData = AccountData(
            portal = cloudAccount.portal.url,
            scheme = cloudAccount.portal.scheme.value,
            displayName = cloudAccount.name,
            userId = cloudAccount.id,
            refreshToken = refreshToken,
            email = cloudAccount.login,
        )
        accountManager.addAccount(accountName, "", accountData)
        accountManager.setAccountData(accountName, accountData)
        accountManager.setToken(accountName, accessToken)
    }
}