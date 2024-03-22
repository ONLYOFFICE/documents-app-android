package app.documents.core.migration

import android.content.Context
import app.documents.core.account.AccountRepository
import app.documents.core.database.migration.MigrationHelper
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

internal class MigrationHelperImpl @Inject constructor(
    private val context: Context,
    private val accountRepository: AccountRepository
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
                accountRepository.addAccount(
                    data.cloudAccount,
                    data.token,
                    data.password
                )
            }
        }
    }
}
