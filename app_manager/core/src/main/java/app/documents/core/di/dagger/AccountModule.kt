package app.documents.core.di.dagger

import android.content.Context
import app.documents.core.account.AccountManager
import app.documents.core.database.datasource.CloudDataSource
import app.documents.core.model.cloud.CloudAccount
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.runBlocking

@Module
object AccountModule {

//    @Provides
//    fun provideAccountPreferences(context: Context): AccountPreferences {
//        return AccountPreferences(context)
//    }

//    @Provides
//    @PortalUrl
//    fun providerPortalUrl(accountPreferences: AccountPreferences, cloudDataSource: CloudDataSource): String? {
//        val portalId = accountPreferences.credential?.portalId
//        return runBlocking { cloudDataSource.getPortal(portalId ?: return@runBlocking null) }?.url
//    }

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