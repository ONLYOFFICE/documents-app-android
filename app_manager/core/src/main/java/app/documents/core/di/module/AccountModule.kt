package app.documents.core.di.module

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import app.documents.core.account.AccountsDataBase
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class AccountModule(private val roomCallback: RoomDatabase.Callback? = null) {

    @Provides
    @Singleton
    fun providesAccountDao(db: AccountsDataBase) = db.accountDao()

    @Provides
    @Singleton
    fun providesAccountDataBase(context: Context): AccountsDataBase  {
        val builder = Room.databaseBuilder(context, AccountsDataBase::class.java, AccountsDataBase.TAG)
        if (roomCallback != null) {
            builder.addCallback(roomCallback)
        }
        return builder.build()
    }

}