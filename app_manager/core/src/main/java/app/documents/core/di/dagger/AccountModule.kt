package app.documents.core.di.dagger

import android.content.Context
import app.documents.core.account.AccountManager
import app.documents.core.database.datasource.CloudDataSource
import app.documents.core.model.cloud.CloudAccount
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.runBlocking
import javax.inject.Qualifier

@Qualifier
internal annotation class AccountType

@Module
object AccountModule {

    @Provides
    fun provideAccount(cloudDataSource: CloudDataSource): CloudAccount? = runBlocking {
        return@runBlocking cloudDataSource.getAccountOnline()
    }

    @Provides
    @Token
    fun provideToken(accountManager: AccountManager, cloudDataSource: CloudDataSource): String = runBlocking {
        val accountName = cloudDataSource.getAccountOnline()?.accountName
        if (accountName.isNullOrEmpty()) {
            return@runBlocking ""
        }
        return@runBlocking accountManager.getToken(accountName).orEmpty()
    }

    @Provides
    fun provideAccountManager(context: Context): AccountManager {
        return AccountManager(context)
    }
}