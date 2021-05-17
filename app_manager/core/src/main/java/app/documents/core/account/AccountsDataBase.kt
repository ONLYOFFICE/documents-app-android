package app.documents.core.account

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [CloudAccount::class, Recent::class], version = 1)
abstract class AccountsDataBase: RoomDatabase() {

    companion object {
        val TAG: String = AccountsDataBase::class.java.simpleName
    }

    abstract fun accountDao(): AccountDao

    abstract fun recentDao(): RecentDao

}