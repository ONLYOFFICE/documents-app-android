package app.documents.core.account

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [CloudAccount::class, Recent::class], version = 4, exportSchema = false)
abstract class AccountsDataBase: RoomDatabase() {

    companion object {
        val TAG: String = AccountsDataBase::class.java.simpleName

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE CloudAccount ADD COLUMN isOneDrive INTEGER DEFAULT 0 NOT NULL")
            }
        }
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE CloudAccount ADD COLUMN isDropbox INTEGER DEFAULT 0 NOT NULL")
            }

        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE CloudAccount ADD COLUMN token TEXT DEFAULT '' NOT NULL")
                database.execSQL("ALTER TABLE CloudAccount ADD COLUMN password TEXT DEFAULT '' NOT NULL")
            }

        }
    }

    abstract fun accountDao(): AccountDao

    abstract fun recentDao(): RecentDao

}