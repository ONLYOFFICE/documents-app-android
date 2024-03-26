package app.documents.core.di.dagger

import android.content.Context
import app.documents.core.account.AccountManager
import app.documents.core.account.AccountPreferences
import app.documents.core.account.AccountRepository
import app.documents.core.account.AccountRepositoryImpl
import app.documents.core.database.datasource.CloudDataSource
import app.documents.core.database.migration.MigrationHelper
import app.documents.core.migration.MigrationHelperImpl
import app.documents.core.model.cloud.CloudAccount
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.runBlocking

@Module
object AccountModule {

    @Provides
    fun provideMigrationHelper(
        context: Context,
        cloudDataSource: CloudDataSource,
        accountManager: AccountManager,
        accountPreferences: AccountPreferences
    ): MigrationHelper {
        return MigrationHelperImpl(context, cloudDataSource, accountManager, accountPreferences)
    }

    @Provides
    fun provideAccountPreferences(context: Context): AccountPreferences {
        return AccountPreferences(context)
    }

    @Provides
    fun provideAccountOnline(
        cloudDataSource: CloudDataSource,
        accountPreferences: AccountPreferences
    ): CloudAccount? = runBlocking {
        val accountId = accountPreferences.onlineAccountId
        if (accountId.isNullOrEmpty()) return@runBlocking null

        return@runBlocking cloudDataSource.getAccount(accountId)
    }

    @Provides
    @Token
    fun provideToken(
        accountManager: AccountManager,
        cloudAccount: CloudAccount?
    ): String = runBlocking {
        val accountName = cloudAccount?.accountName
        if (accountName.isNullOrEmpty()) return@runBlocking ""

        return@runBlocking accountManager.getToken(accountName).orEmpty()
    }

    @Provides
    fun provideAccountManager(context: Context): AccountManager {
        return AccountManager(context)
    }

    @Provides
    fun provideAccountRepository(
        cloudDataSource: CloudDataSource,
        accountManager: AccountManager,
        accountPreferences: AccountPreferences
    ): AccountRepository {
        return AccountRepositoryImpl(cloudDataSource, accountManager, accountPreferences)
    }
}