package app.documents.core.di.dagger

import android.content.Context
import androidx.room.Room
import app.documents.core.storage.account.AccountDao
import app.documents.core.storage.account.AccountsDataBase
import app.documents.core.storage.account.CloudAccount
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.runBlocking
import javax.inject.Singleton

@Module
object AccountModule {

    @Provides
    @Singleton
    fun providesAccountDao(db: AccountsDataBase) = db.accountDao()

    @Provides
    @Singleton
    fun providesAccountDataBase(context: Context): AccountsDataBase {
        val builder = Room.databaseBuilder(context, AccountsDataBase::class.java, AccountsDataBase.TAG)
            .addMigrations(AccountsDataBase.MIGRATION_1_2)
            .addMigrations(AccountsDataBase.MIGRATION_2_3)
            .addMigrations(AccountsDataBase.MIGRATION_3_4)
            .addMigrations(AccountsDataBase.MIGRATION_4_5)
            .addMigrations(AccountsDataBase.MIGRATION_5_6)
            .addMigrations(AccountsDataBase.MIGRATION_6_7)
        return builder.build()
    }

    @Provides
    fun provideAccount(accountDao: AccountDao): CloudAccount? = runBlocking {
        return@runBlocking accountDao.getAccountOnline()
    }

}