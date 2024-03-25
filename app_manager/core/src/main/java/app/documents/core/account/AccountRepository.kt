package app.documents.core.account

import app.documents.core.database.datasource.CloudDataSource
import app.documents.core.login.CheckLoginResult
import app.documents.core.migration.CloudAccountWithTokenAndPassword
import app.documents.core.model.cloud.CloudAccount
import app.documents.core.model.cloud.PortalProvider
import app.documents.core.model.cloud.Scheme
import lib.toolkit.base.managers.utils.AccountData
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import javax.net.ssl.SSLHandshakeException
import javax.net.ssl.SSLPeerUnverifiedException

interface AccountRepository {

    suspend fun addAccount(
        cloudAccount: CloudAccount,
        accessToken: String,
        password: String = "",
        refreshToken: String = "",
        onOldAccount: suspend (CloudAccount) -> Unit = {}
    )

    suspend fun addAccount(
        cloudAccount: CloudAccount,
        password: String,
        onOldAccount: suspend (CloudAccount) -> Unit = {}
    )

    suspend fun addAccounts(accounts: List<CloudAccountWithTokenAndPassword>)

    suspend fun getOnlineAccount(): CloudAccount?

    suspend fun logOut(accountId: String): CloudAccount

    suspend fun deleteAccounts(accountIds: Array<out String>): List<CloudAccount>

    suspend fun checkLogin(accountId: String): CheckLoginResult

    suspend fun getPortals(): List<String>

    suspend fun handleIOException(exception: IOException): Boolean

    suspend fun getToken(accountName: String): String?

    suspend fun getRefreshToken(accountName: String? = null): String?

    suspend fun updateAccount(
        id: String? = null,
        token: String? = null,
        refreshToken: String? = null,
        block: ((CloudAccount) -> CloudAccount)? = null
    )

    suspend fun updateAccountData(
        accountId: String,
        accountName: String,
        token: String?,
        password: String?,
        isOnline: Boolean
    )
}

internal class AccountRepositoryImpl(
    private val cloudDataSource: CloudDataSource,
    private val accountManager: AccountManager,
    private val accountPreferences: AccountPreferences
) : AccountRepository {

    override suspend fun updateAccount(
        id: String?,
        token: String?,
        refreshToken: String?,
        block: ((CloudAccount) -> CloudAccount)?
    ) {
        cloudDataSource.getAccount(checkNotNull(id ?: accountPreferences.onlineAccountId))?.let { account ->
            block?.invoke(account)?.let { updated ->
                cloudDataSource.updateAccount(updated)
                cloudDataSource.insertOrUpdatePortal(updated.portal)
            }
            with(accountManager) {
                token?.let { setToken(account.accountName, token) }
                refreshToken?.let { updateAccountData(account.accountName) { it.copy(refreshToken = refreshToken) } }
            }
        }
    }

    override suspend fun updateAccountData(
        accountId: String,
        accountName: String,
        token: String?,
        password: String?,
        isOnline: Boolean
    ) {
        accountManager.setToken(accountName, token)
        accountManager.setPassword(accountName, password)
        if (isOnline) accountPreferences.onlineAccountId = accountId
    }

    override suspend fun checkLogin(accountId: String): CheckLoginResult {
        if (accountId == accountPreferences.onlineAccountId) return CheckLoginResult.AlreadyUse
        val account = cloudDataSource.getAccount(accountId) ?: return CheckLoginResult.NeedLogin
        val accessToken = accountManager.getToken(account.accountName)

        when (account.portal.provider) {
            is PortalProvider.Webdav -> {
                if (accountManager.getPassword(account.accountName).isNullOrEmpty())
                    return CheckLoginResult.NeedLogin
            }
            else -> if (accessToken.isNullOrEmpty()) return CheckLoginResult.NeedLogin
        }

        accountPreferences.onlineAccountId = account.id
        return CheckLoginResult.Success(account.portal.provider, accessToken.orEmpty())
    }

    override suspend fun getPortals(): List<String> {
        return cloudDataSource.getPortals()
    }

    override suspend fun deleteAccounts(accountIds: Array<out String>): List<CloudAccount> {
        accountIds.forEach { accountId ->
            cloudDataSource.getAccount(accountId)?.let { account ->
                if (accountPreferences.onlineAccountId == accountId) accountPreferences.onlineAccountId = null
                accountManager.removeAccount(account.accountName)
                cloudDataSource.deleteAccount(account)
            }
        }
        return cloudDataSource.getAccounts()
    }

    override suspend fun logOut(accountId: String): CloudAccount {
        val account = checkNotNull(cloudDataSource.getAccount(accountId))
        with(accountManager) {
            when (account.portal.provider) {
                is PortalProvider.Webdav -> setPassword(account.accountName, null)
                PortalProvider.Dropbox,
                PortalProvider.GoogleDrive,
                PortalProvider.Onedrive -> setToken(account.accountName, null)
                else -> {
                    setPassword(account.accountName, null)
                    setToken(account.accountName, null)
                }
            }
        }
        accountPreferences.onlineAccountId = null
        return account
    }

    override suspend fun addAccount(
        cloudAccount: CloudAccount,
        accessToken: String,
        password: String,
        refreshToken: String,
        onOldAccount: suspend (CloudAccount) -> Unit
    ) {
        addAccountToAccountManager(cloudAccount.toAccountData(refreshToken), password, accessToken)
        accountPreferences.onlineAccountId = cloudAccount.id
        cloudDataSource.insertOrUpdateAccount(cloudAccount)
        cloudDataSource.insertOrUpdatePortal(cloudAccount.portal)
    }

    override suspend fun addAccount(
        cloudAccount: CloudAccount,
        password: String,
        onOldAccount: suspend (CloudAccount) -> Unit
    ) {
        addAccount(cloudAccount, "", password, "", onOldAccount)
    }

    override suspend fun addAccounts(accounts: List<CloudAccountWithTokenAndPassword>) {
        accounts.forEach { data ->
            addAccountToAccountManager(data.cloudAccount.toAccountData(), data.password.orEmpty(), data.token.orEmpty())
            cloudDataSource.insertOrUpdateAccount(data.cloudAccount)
            cloudDataSource.insertOrUpdatePortal(data.cloudAccount.portal)
        }
    }

    private fun CloudAccount.toAccountData(refreshToken: String = ""): AccountData {
        return AccountData(
            portal = portal.url,
            scheme = portal.scheme.value,
            displayName = name,
            userId = id,
            email = login,
            avatar = avatarUrl,
            refreshToken = refreshToken
        )
    }

    private fun addAccountToAccountManager(
        accountData: AccountData,
        password: String,
        accessToken: String
    ) {
        with(accountManager) {
            val accountName = "${accountData.email}@${accountData.portal}"
            if (!addAccount(accountName, password, accountData)) {
                setAccountData(accountName, accountData)
                setPassword(accountName, password)
            }
            setToken(accountName, accessToken)
        }
    }

    override suspend fun handleIOException(exception: IOException): Boolean {
        val account = cloudDataSource.getAccount(accountPreferences.onlineAccountId.orEmpty())
        if (account != null) {
            val portal = account.portal
            if (exception is SSLHandshakeException && !portal.settings.isSslCiphers && portal.scheme == Scheme.Https) {
                cloudDataSource.insertOrUpdatePortal(portal.copy(settings = portal.settings.copy(isSslCiphers = true)))
                return true
            } else if ((exception is ConnectException ||
                        exception is SocketTimeoutException ||
                        exception is SSLHandshakeException ||
                        exception is SSLPeerUnverifiedException) &&
                portal.scheme == Scheme.Https
            ) {
                cloudDataSource.insertOrUpdatePortal(
                    portal.copy(
                        scheme = Scheme.Http,
                        settings = portal.settings.copy(isSslCiphers = true)
                    )
                )
                return true
            }
        }
        return false
    }

    override suspend fun getToken(accountName: String): String? {
        return accountManager.getToken(accountName)
    }

    override suspend fun getRefreshToken(accountName: String?): String? {
        return accountManager.getAccountData(
            checkNotNull(accountName ?: getOnlineAccount()?.accountName)
        ).refreshToken
    }

    override suspend fun getOnlineAccount(): CloudAccount? {
        return cloudDataSource.getAccount(accountPreferences.onlineAccountId ?: return null)
    }
}