package app.documents.core.account

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [CloudAccount::class, Recent::class], version = 2)
abstract class AccountsDataBase: RoomDatabase() {

    companion object {
        val TAG: String = AccountsDataBase::class.java.simpleName

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE CloudAccount ADD COLUMN isOneDrive INTEGER DEFAULT 0 NOT NULL")
            }

        }
    }

    abstract fun accountDao(): AccountDao

    abstract fun recentDao(): RecentDao

}