package app.documents.core.migration

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [OldCloudAccount::class], version = 8)
internal abstract class AccountsDataBase : RoomDatabase() {

    companion object {

        fun newInstance(context: Context): AccountsDataBase {
            return Room.databaseBuilder(context, AccountsDataBase::class.java, NAME)
                .addMigrations(
                    MIGRATION_1_2,
                    MIGRATION_2_3,
                    MIGRATION_3_4,
                    MIGRATION_4_5,
                    MIGRATION_5_6,
                    MIGRATION_6_7,
                    object : Migration(7, 8) {
                        override fun migrate(database: SupportSQLiteDatabase) {

                        }
                    }
                )
                .build()
        }

        const val NAME: String = "AccountsDataBase"

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE CloudAccount ADD COLUMN isOneDrive INTEGER DEFAULT 0 NOT NULL")
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE CloudAccount ADD COLUMN isDropbox INTEGER DEFAULT 0 NOT NULL")
            }

        }

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE CloudAccount ADD COLUMN token TEXT DEFAULT '' NOT NULL")
                database.execSQL("ALTER TABLE CloudAccount ADD COLUMN password TEXT DEFAULT '' NOT NULL")
                database.execSQL("ALTER TABLE CloudAccount ADD COLUMN expires TEXT DEFAULT '' NOT NULL")
            }

        }

        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE Recent ADD COLUMN source TEXT DEFAULT NULL")
            }

        }

        private val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE CloudAccount ADD COLUMN isGoogleDrive INTEGER DEFAULT 0 NOT NULL")
            }

        }

        private val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE CloudAccount ADD COLUMN refreshToken TEXT DEFAULT '' NOT NULL")
            }
        }
    }

    abstract fun accountDao(): OldAccountDao
}