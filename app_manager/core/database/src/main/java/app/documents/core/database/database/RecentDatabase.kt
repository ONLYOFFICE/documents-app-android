package app.documents.core.database.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import app.documents.core.database.dao.RecentDao
import app.documents.core.database.entity.RecentEntity

@Database(entities = [RecentEntity::class], version = 2)
abstract class RecentDatabase : RoomDatabase() {

    companion object {

        internal const val NAME: String = "recent_database"

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE recent ADD COLUMN token TEXT")
            }
        }
    }

    internal abstract val recentDao: RecentDao
}