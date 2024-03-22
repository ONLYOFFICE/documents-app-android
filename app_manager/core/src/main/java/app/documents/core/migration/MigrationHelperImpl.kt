package app.documents.core.migration

import android.content.Context
import app.documents.core.account.AccountManager
import app.documents.core.account.AccountPreferences
import app.documents.core.database.datasource.CloudDataSource
import app.documents.core.database.migration.MigrationHelper
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

internal class MigrationHelperImpl @Inject constructor(
    private val context: Context,
    private val cloudDataSource: CloudDataSource,
    private val accountManager: AccountManager,
    private val accountPreferences: AccountPreferences
) : MigrationHelper {

    override fun migrate() {
        if (!context.getDatabasePath(AccountsDataBase.NAME).exists()) return
        val accountDao = AccountsDataBase.newInstance(context).accountDao()
        val networkSettings = NetworkSettings(context)

        runBlocking {
            val accountWithTokenAndPassword = accountDao
                .getAccounts()
                .map { it.toCloudAccountWithTokenAndPassword(networkSettings) }

            accountWithTokenAndPassword.forEach { data ->
                if (!data.token.isNullOrEmpty()) {
                    accountManager.setToken(data.cloudAccount.accountName, data.token)
                }

                if (!data.password.isNullOrEmpty()) {
                    accountManager.setPassword(data.cloudAccount.accountName, data.password)
                }

                cloudDataSource.insertOrUpdateAccount(data.cloudAccount)
                cloudDataSource.insertOrUpdatePortal(data.cloudAccount.portal)

                if (data.online) {
                    accountPreferences.onlineAccountId = data.cloudAccount.id
                }
            }
        }
    }
}
