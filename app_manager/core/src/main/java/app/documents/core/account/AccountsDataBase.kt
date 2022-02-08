package app.documents.core.account

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [CloudAccount::class, Recent::class], version = 3)
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
                database.execSQL("ALTER TABLE CloudAccount ADD COLUMN expires TEXT DEFAULT '' NOT NULL")
            }

        }

        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE Recent ADD COLUMN source TEXT DEFAULT NULL")
            }

        }
        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE CloudAccount ADD COLUMN isGoogleDrive INTEGER DEFAULT 0 NOT NULL")
            }

        }
    }

    abstract fun accountDao(): AccountDao

    abstract fun recentDao(): RecentDao

}