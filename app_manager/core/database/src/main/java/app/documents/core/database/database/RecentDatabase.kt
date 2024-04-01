package app.documents.core.database.database

import androidx.room.Database
import androidx.room.RoomDatabase
import app.documents.core.database.dao.RecentDao
import app.documents.core.database.entity.RecentEntity

@Database(entities = [RecentEntity::class], version = 1)
abstract class RecentDatabase : RoomDatabase() {

    companion object {

        internal const val NAME: String = "recent_database"
    }

    internal abstract val recentDao: RecentDao
}