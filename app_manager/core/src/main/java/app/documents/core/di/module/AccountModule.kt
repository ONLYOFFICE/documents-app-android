package app.documents.core.di.module

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import app.documents.core.account.AccountDao
import app.documents.core.account.AccountsDataBase
import app.documents.core.account.CloudAccount
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.runBlocking
import javax.inject.Singleton

@Module
class AccountModule {

    @Provides
    @Singleton
    fun providesAccountDao(db: AccountsDataBase) = db.accountDao()

    @Provides
    @Singleton
    fun providesAccountDataBase(roomCallback: RoomDatabase.Callback, context: Context): AccountsDataBase {
        val builder = Room.databaseBuilder(context, AccountsDataBase::class.java, AccountsDataBase.TAG)
            .addMigrations(AccountsDataBase.MIGRATION_1_2)
            .addMigrations(AccountsDataBase.MIGRATION_2_3)
            .addMigrations(AccountsDataBase.MIGRATION_3_4)
        builder.addCallback(roomCallback)
        return builder.build()
    }

    @Provides
    fun provideAccount(accountDao: AccountDao): CloudAccount? = runBlocking {
        return@runBlocking accountDao.getAccountOnline()
    }

}